/*
 * Copyright (c) 2008, Damian Carrillo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of 
 *     conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
 *     conditions and the following disclaimer in the documentation and/or other materials 
 *     provided with the distribution.
 *   * Neither the name of the copyright holder's organization nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software without specific 
 *     prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package agave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.objectweb.asm.ClassReader;

import agave.exception.AgaveException;
import agave.exception.DestinationException;
import agave.exception.FormException;
import agave.exception.HandlerException;
import agave.internal.DestinationImpl;
import agave.internal.FormPopulator;
import agave.internal.HandlerDescriptor;
import agave.internal.HandlerDescriptorImpl;
import agave.internal.HandlerIdentifier;
import agave.internal.HandlerRegistry;
import agave.internal.HandlerRegistryImpl;
import agave.internal.HandlerScanner;
import agave.internal.MultipartRequestImpl;
import agave.internal.ReflectionInstanceCreator;
import agave.internal.RequestParameterFormPopulator;
import agave.internal.RequestPartFormPopulator;
import agave.internal.URIParameterFormPopulator;


/**
 * <p>
 * Scans the {@code /WEB-INF/classes} directory of a deployed context for any configured 
 * handlers, builds an internal representation of all handler methods, and then 
 * forwards HTTP requests to the handlers if they match the requested URI.  See the
 * {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} method for a 
 * description of the primary function of this filter.
 * </p>
 *
 * <p>
 * This filter supports two {@code init-param}s:
 * 
 * <ul>
 *   <li id="lifecycleHooks">{@code lifecycleHooks} - See {@link LifecycleHooks} for more information.</li>
 *   <li id="instanceCreator">{@code instanceCreator} - See {@link InstanceCreator} for more information.</li>
 * </ul>
 * </p>
 *
 *
 * <p>
 * This filter supports a single system property (supplied via a -Dproperty):
 * <ul>
 *	 <li id="classesDirectory">{@code classesDirectory} - The directory to use when scanning for handler
 *		classes.  Typically this is {@code /WEB-INF/classes} when running within a Servlet container,
 *		but it is desirable to override this in testing situations, so that classes can be automatically
 *		loaded and scanned without having to redeploy the whole Web application.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 * @see <a href="#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)"><code>AgaveFilter.doFilter()</code></a>
 * @see HandlerDescriptor
 */
public class AgaveFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AgaveFilter.class.getName());
    private static final String WORKFLOW_HANDLER_SUFFIX = "-handler";
    private static final String WORKFLOW_FORM_SUFFIX = "-form";
    
    private FilterConfig config;
    private LifecycleHooks lifecycleHooks;
    private File classesDirectory;
    private InstanceCreator instanceFactory;
    private HandlerRegistry handlerRegistry;
    private boolean classesDirectoryProvided;

    /**
     * An alternate way of providing the {@link LifecycleHooks} implementation is to override this method.  
     * The default behavior is to read the <a href="#lifecycleHooks">lifecycleHooks</a> {@code init-param} and use the supplied 
     * value to instantiate a {@link LifecycleHooks} instance, or use a stub implementation if this parameter
     * is not supplied.
     * 
     * @param config the configuration delivered to this filter
     * @return a {@link LifecycleHooks} instance
     * 
     * @throws ClassNotFoundException if the class named by the {@code lifecycleHooks} initialization parameter can not be found
     * @throws InstantiationException if the class named by the {@code lifecycleHooks} initialization parameter can not be instantiated
     * @throws IllegalAccessException if this filter is denied access to the class named by the {@code lifecycleHooks} initialization parameter value
     */
    protected LifecycleHooks provideLifecycleHooks(FilterConfig config) 
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        LifecycleHooks hooks = null;
        
        String lifecycleHooksParameter = config.getInitParameter("lifecycleHooks");
        if (lifecycleHooksParameter != null) {
            hooks = (LifecycleHooks)Class.forName(lifecycleHooksParameter).newInstance();
            LOGGER.info("Using lifecycle hooks: " + hooks.getClass().getName());
        } else {
            hooks = new DefaultLifecycleHooks();
        }
        
        return hooks;
    }
    
    /**
     * An alternate way of providing an {@link InstanceCreator} implementation is to override this method.
     * The default behavior is to read the <a href="#instanceCreator">instanceCreator</a> {@code init-param} and use the supplied
     * value to instantiate a {@link InstanceCreator} instance, or use a {@link ReflectionInstanceCreator} instance
     * if this parameter is not supplied.
     * 
     * @param config the configuration delivered to this filter
     * @return a {@link ReflectionInstanceCreator} instance
     * 
     * @throws ClassNotFoundException if the class named by the {@code instanceCreator} initialization parameter can not be found
     * @throws InstantiationException if the class named by the {@code instanceCreator} initialization parameter can not be instantiated
     * @throws IllegalAccessException if this filter is denied access to the class named by the {@code classesDir} initialization parameter value
     */
    protected InstanceCreator provideInstanceCreator(FilterConfig config) 
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        InstanceCreator creator = null;
        
        String instanceFactoryParameter = config.getInitParameter("instanceCreator");
        if (instanceFactoryParameter != null) {
            creator = 
                (InstanceCreator)Class.forName(instanceFactoryParameter).newInstance();
            LOGGER.info("Using instance creator: " + creator.getClass().getName());
        } else {
            creator = new ReflectionInstanceCreator();
        }
        
        return creator;
    }
    
    /**
     * An alternate way of providing a class directory to scan for handlers is to override this method.
     * This is primarily here for testing situations (especially when running mvn jetty:run).  The default
     * behavior is to read the <a href="#classesDirectory">classesDirectory</a> system property
     * (-DclassesDirectory=xxx) and use the value supplied to create a directory {@code File} that will
     * be scanned for handlers.  If this system property is absent, the default value will be
     * {@code /WEB-INF/classes}.
     * 
     * @param config the configuration delivered to this filter
     * @return a {@code File} representing the named class directory
     * 
     * @throws ClassNotFoundException if the class named by the {@code classesDir} initialization
     *							      parameter can not be found
     * @throws InstantiationException if the class named by the {@code classesDir} initialization
     *                                parameter can not be instantiated
     * @throws IllegalAccessException if this filter is denied access to the class named by the
     *                                {@code instanceCreator} initialization parameter value
     */
    protected File provideClassesDirectory(FilterConfig config)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        File classesDir = null;

		if (System.getProperty("classesDirectory") != null) {
            classesDir = new File(System.getProperty("classesDirectory"));
            classesDirectoryProvided = true;
        } else {
            classesDir = new File(config.getServletContext().getRealPath("/WEB-INF/classes"));
        }
        
        return classesDir;
    }
    
    /**
     * Initializes the {@code AgaveFilter} by scanning for handler classes and
     * populating a {@link agave.internal.HandlerRegistry HandlerRegistry} with
     * them. Then, this initializes the dependency injection container (if any)
     * by instantiating a {@link agave.InstanceCreator}.
     * 
     * @param config  the supplied filter configuration object
     * @throws ServletException
     */
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        
        try {
            lifecycleHooks = provideLifecycleHooks(config);
            setHandlerRegistry(new HandlerRegistryImpl());
            classesDirectory = provideClassesDirectory(config);
            scanClassesDirForHandlers(classesDirectory);
            instanceFactory = provideInstanceCreator(config);
            instanceFactory.initialize();
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        
        if (!handlerRegistry.getDescriptors().isEmpty()) {
            for (HandlerDescriptor descriptor : handlerRegistry.getDescriptors()) {
                LOGGER.fine(descriptor.getHandlerClass().getName() + "#" 
                        + descriptor.getHandlerMethod().getName() + "() registered -> "
                        + descriptor.getPattern());
            }
        } else {
            StringBuilder message = new StringBuilder("No handlers have been registered.");
            if (classesDirectoryProvided) {

            } else if (config.getServletContext().getServerInfo().toLowerCase().contains("jetty")) {
                message.append("  If you are running this webapp with 'mvn jetty:run', no" +
                    " handlers will ever be found.  Try 'mvn jetty:run-war' instead.");
            }
        	  throw new IllegalStateException(message.toString());
        }
        LOGGER.info("AgaveFilter successfully initialized");
    }

    /**
     * Destroys this filter.
     */
    public void destroy() {
        config = null;
        classesDirectory = null;
        handlerRegistry = null;
        instanceFactory = null;
    }

    /**
     * <p>
     *   Handles the routing of HTTP requests through the framework. The algorithm
     *   used internally is as follows:
     * </p>
     * 
     * <ol>
     *   <li>
     *     {@link agave.InstanceCreator#createFormInstance Instantiates a
     *     form if necessary}.
     *     <ol>
     *       <li>
     *         {@link RequestParameterFormPopulator Populates 
     *         request parameters} if necessary, leveraging any {@link agave.conversion.Converter}s named
     *         with the {@link ConvertWith} annotation on mutator arguments.  Note that URI parameters
     *         will override request parameters if they are similarly named. 
     *       </li>
     *       <li>
     *         {@link URIParameterFormPopulator Populates 
     *         URI parameters} if necessary, leveraging any {@link agave.conversion.Converter}s named
     *         with the {@link ConvertWith} annotation on mutator arguments.
     *       </li>
     *     </ol>
     *   </li>
     *   <li>
     *     {@link agave.InstanceCreator#createHandlerInstance
     *      Instantiates a handler}.
     *   </li>
     *   <li>
     *     Populates a new {@link HandlerContext}, or look up a previously created
     *     one if this handler method participates in a workflow.
     *   </li>
     *   <li>
     *     Invokes the handler method with the populated {@link HandlerContext} and the 
     *     form instance (if applicable).
     *   </li>
     * </ol>
	 *
	 * <p>
	 *   Note that at any point any of the lifecycle hooks can prevent further execution of this
	 *   filter.
	 * </p>
     * 
     * <p>
     *   When one of the two supported encoding methods is selected, then this
     *   filter will field the HTTP request that was made and will prevent future
     *   execution of the filter chain. If an unsupported encoding type is
     *   requested or if a handler is not configured for the requested URI, this
     *   filter will simply continue with execution of the filter chain. The two
     *   encoding types that are supported by this method are:
     * </p>
     * 
     * <ul>
     *   <li><a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">
     *     application/x-www-form-urlencoded</a></li>
     *   <li><a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2">
     *     multipart/form-data</a></li>
     * </ul>
     * 
     * @param req the Servlet request object; it will be cast to an {@code HttpServletRequest}
     * @param resp the Servlet response object; it will be cast to an {@code HttpServletResponse}
     * @param chain the filter chain this filter is a member of
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a Servlet error occurs
     * 
     * @see <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4">W3C Form Encoding Types</a>
     * @see agave.HandlesRequestsTo
     * @see agave.ConvertWith
     */
    public final void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        ServletContext servletContext = config.getServletContext();
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HandlerDescriptor descriptor = handlerRegistry.findMatch(request);
        if (descriptor != null) {
            HttpSession session = request.getSession(true);
            HandlerContext handlerContext =
                new HandlerContext(servletContext, request, response, session);

            if (lifecycleHooks.beforeFilteringRequest(descriptor, handlerContext)) {
				return;
			}
            
            LOGGER.fine(request.getServletPath() + " -> " + descriptor.getHandlerClass().getName() + "#" + 
                    descriptor.getHandlerMethod() + "()");

			// wraps the request if necessary so that the uploaded content can be accessed like
			// regular string param eters
            if (MultipartRequestImpl.isMultipart(request)) {
                request = new MultipartRequestImpl(request);
            }

            Object formInstance = null;

			// attempts to pull a form intance out of the session, stored from a previous workflow phase
            if (descriptor.getWorkflowName() != null && !descriptor.initiatesWorkflow()) {
                formInstance = session.getAttribute(descriptor.getWorkflowName() + WORKFLOW_FORM_SUFFIX);
            }

			// creates a form instance
            if (formInstance == null) {
                formInstance = instanceFactory.createFormInstance(descriptor);
            }

			// populates the form if necessary
            if (formInstance != null) {
                if (descriptor.initiatesWorkflow()) {
                    session.setAttribute(descriptor.getWorkflowName() + WORKFLOW_FORM_SUFFIX, formInstance);
                }
                
				if (lifecycleHooks.beforeHandlingRequest(descriptor, formInstance, handlerContext)) {
					return;
				}
                
                try {
					// populates a form and convert into the target types if they can be described
					// by the standard suite of converters out of the agave.conversion package
                    FormPopulator formPopulator = new RequestParameterFormPopulator(request);
                    formPopulator.populate(formInstance);
                    if (MultipartRequestImpl.isMultipart(request)) {
                        formPopulator = new RequestPartFormPopulator((MultipartRequest)request);
                        formPopulator.populate(formInstance);
                    }
                    formPopulator = new URIParameterFormPopulator(request, descriptor);
                    formPopulator.populate(formInstance);
                } catch (NoSuchMethodException ex) {
                    throw new FormException(ex);
                } catch (IllegalAccessException ex) {
                    throw new FormException(ex);
                } catch (InvocationTargetException ex) {
                    throw new FormException(ex.getCause());
                } catch (InstantiationException ex) {
                    throw new FormException(ex);
                }
                
				if (lifecycleHooks.afterInitializingForm(descriptor, formInstance, handlerContext)) {
					return;
				}
            }

            Object handlerInstance = null;

			// attempts to pull a handler out of the session, from a previous workflow phase
            if (descriptor.getWorkflowName() != null && !descriptor.initiatesWorkflow()) {
                handlerInstance = session.getAttribute(descriptor.getWorkflowName() + WORKFLOW_HANDLER_SUFFIX);
            }

			// creates a handler
            if (handlerInstance == null) {
                handlerInstance = instanceFactory.createHandlerInstance(descriptor);
            }

			// initiates a new workflow if necessary
            if (descriptor.initiatesWorkflow()) {
                session.setAttribute(descriptor.getWorkflowName() + WORKFLOW_HANDLER_SUFFIX, handlerInstance);
            }
            
			if (lifecycleHooks.beforeHandlingRequest(descriptor, handlerInstance, handlerContext)) {
				return;
			}
			
            Object result = null;

			// invoke the handler method, supplying a context and a form instance
            try {
                if (formInstance != null) {
                    if (descriptor.getHandlerMethod().getReturnType() != null) {
                        result = descriptor.getHandlerMethod().invoke(handlerInstance, handlerContext, formInstance);
                    } else {
                        descriptor.getHandlerMethod().invoke(handlerInstance, handlerContext, formInstance);
                    }
                } else {
                    if (descriptor.getHandlerMethod().getReturnType() != null) {
                        result = descriptor.getHandlerMethod().invoke(handlerInstance, handlerContext);
                    } else {
                        descriptor.getHandlerMethod().invoke(handlerInstance, handlerContext);
                    }
                }
            } catch (InvocationTargetException ex) {
                if (ex.getCause() instanceof AgaveException) {
                    throw (AgaveException) ex.getCause();
                } else if (ex.getCause() instanceof IOException) {
                    throw (IOException) ex.getCause();
                } else if (ex.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) ex.getCause();
                } else {
                    throw new HandlerException(ex.getMessage(), ex.getCause());
                }
            } catch (IllegalAccessException ex) {
                throw new HandlerException(descriptor, ex);
            }

			// complets a workflow and flushes the referenced attributes from the session
            if (descriptor.completesWorkflow()) {
                session.removeAttribute(descriptor.getWorkflowName() + WORKFLOW_HANDLER_SUFFIX);
                session.removeAttribute(descriptor.getWorkflowName() + WORKFLOW_FORM_SUFFIX);
            }

			// determines a destination
            if (result != null && !response.isCommitted()) {
                URI uri = null;
                boolean redirect = false;
                
                if (result instanceof DestinationImpl) {
                    Destination destination = (Destination)result;
                    
					if (lifecycleHooks.afterHandlingRequest(descriptor, handlerInstance, destination, handlerContext)) {
						return;
					}
                    
                    try {
                        uri = new URI(null, destination.encode(config.getServletContext()), null);
                        if (destination.getRedirect() == null) {
                            if ("POST".equalsIgnoreCase(request.getMethod())) {
                                redirect = true;
                            }
                        } else {
                            redirect = destination.getRedirect();
                        }
                    } catch (URISyntaxException ex) {
                        throw new DestinationException(ex.getMessage(), ex.getCause());
                    }
                } else if (result instanceof URI) {
                    uri = (URI)result;
                    
					if (lifecycleHooks.afterHandlingRequest(descriptor, handlerInstance, uri, handlerContext)) {
						return;
					}
                    
                    redirect = true;
                } else {
                    throw new DestinationException(descriptor);
                }
                
                if (redirect) {
                    String location = uri.toASCIIString();
                    if (location.startsWith("/")) { // absolute URI
                        location = request.getContextPath() + location;
                    }
                    response.sendRedirect(location);
                } else {
                    request.getRequestDispatcher(uri.toASCIIString()).forward(request, response);
                }
            } else {
				if (lifecycleHooks.afterHandlingRequest(descriptor, handlerInstance, handlerContext)) {
					return;
				}
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    /**
     * Scans the supplied directory for handlers. Handlers in turn are inspected
     * and have a {@link agave.internal.HandlerDescriptor HandlerDescriptor}
     * generated for them which then gets registered in the
     * {@link agave.internal.HandlerRegistry HandlerRegistry} as handlers are
     * found.
     * 
     * @param root the root directory to scan files for, typically {@code /WEB-INF/classes}
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws ServletException
     */
    protected void scanClassesDirForHandlers(File root) 
    throws FileNotFoundException, IOException, ClassNotFoundException, ServletException {
        if (root != null && root.canRead()) {
            for (File node : root.listFiles()) {
                if (node.isDirectory()) {
                    scanClassesDirForHandlers(node);
                } else if (node.isFile() && node.getName().endsWith(".class")) {
					if (lifecycleHooks.beforeHandlerIsDiscovered(node)) {
						return;
					}
                   
                    FileInputStream nodeIn = new FileInputStream(node); 
                    try {
                        ClassReader classReader = new ClassReader(nodeIn);
                        Collection<HandlerIdentifier> handlerIdentifiers = new ArrayList<HandlerIdentifier>();
                        classReader.accept(new HandlerScanner(handlerIdentifiers), ClassReader.SKIP_CODE);
                        
                        for (HandlerIdentifier handlerIdentifier : handlerIdentifiers) {
                            HandlerDescriptor descriptor = new HandlerDescriptorImpl(handlerIdentifier);
                            descriptor.locateAnnotatedHandlerMethods(handlerIdentifier);
                            handlerRegistry.addDescriptor(descriptor);
                            
							if (lifecycleHooks.afterHandlerIsDiscovered(descriptor, config.getServletContext())) {
								return;
							}
                        }
                    } finally {
                        nodeIn.close();
                    }
                }
            }
        }
    }

    /**
     * Sets the configuration object for this filter.
     * 
     * @param config the configuration object
     */
    protected void setConfig(FilterConfig config) {
        this.config = config;
    }

    /**
     * Gets the configuration object that was supplied to this filter.
     * 
     * @return the configuration object
     */
    protected FilterConfig getConfig() {
        return config;
    }

    /**
     * Sets the {@link HandlerRegistry} that this filter will use for mapping requests to handlers.
     * 
     * @param handlerRegistry the registry
     */
    protected void setHandlerRegistry(HandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Gets the {@link HandlerRegistry} that houses the generated {@link HandlerDescriptor}s.
     * @return the {@link HandlerRegistry}
     */
    protected HandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }

    /**
     * Gets the {@link InstanceCreator} that is used to create form and handler instances.
     * @return the hanler instance
     */
    public InstanceCreator getInstanceCreator() {
        return instanceFactory;
    }

    /**
     * Gets the directory that will be scanned for any handler classes.
     * @return
     */
    public File getClassesDirectory() {
            return classesDirectory;
    }
    
}
