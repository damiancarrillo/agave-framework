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
import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import agave.internal.HandlerDescriptor;

/**
 * Hooks into Agave's lifecycle.  The default implementation of this is 
 * {@link DefaultLifecycleHooks}, but you can override it by specifying an
 * initialization param to the {@link AgaveFilter}.  An example of this is:
 * 
 * <pre>&lt;web-app&gt;
 * ...
 * &lt;filter&gt;
 *   &lt;filter-name>AgaveFilter&lt;/filter-name&gt;
 *   &lt;filter-class>agave.AgaveFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;lifecycleHooks&lt;/param-name&gt;
 *     &lt;param-value&gt;com.domain.package.LifecycleHooks&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * ...
 * &lt;/web-app&gt;</pre>
 * 
 * Note that only a single value is supported, so there is no way to have multiple {@code InstanceCreator}s, unless
 * the value named by the parameter fronts multiple others.  This is intentional, and was designed to be this way so
 * that the conceptual overhead of using Agave is shallow.
 * 
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public interface LifecycleHooks {

	public void beforeHandlerIsDiscovered(File potentalHandlerClassFile)
			throws ServletException, IOException;

	public void afterHandlerIsDiscovered(HandlerDescriptor descriptor,
			ServletContext servletContext) throws ServletException, IOException;

	public void beforeFilteringRequest(HandlerDescriptor descriptor,
			HandlerContext context) throws ServletException, IOException;

	public void beforeInitializingForm(HandlerDescriptor descriptor,
			Object formInstance, HandlerContext context)
			throws ServletException, IOException;

	public void afterInitializingForm(HandlerDescriptor descriptor,
			Object formInstance, HandlerContext context)
			throws ServletException, IOException;

	public void beforeHandlingRequest(HandlerDescriptor descriptor,
			Object handlerInstance, HandlerContext context)
			throws ServletException, IOException;

	public void afterHandlingRequest(HandlerDescriptor descriptor,
			Object handlerInstance, HandlerContext context)
			throws ServletException, IOException;

	public void afterHandlingRequest(HandlerDescriptor descriptor,
			Object handlerInstance, Destination destination,
			HandlerContext context) throws ServletException, IOException;

	public void afterHandlingRequest(HandlerDescriptor descriptor,
			Object handlerInstance, URI destination, HandlerContext context)
			throws ServletException, IOException;

}
