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
package co.cdev.agave.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.cdev.agave.CompletesWorkflow;
import co.cdev.agave.HttpMethod;
import co.cdev.agave.InitiatesWorkflow;
import co.cdev.agave.Param;
import co.cdev.agave.ResumesWorkflow;
import co.cdev.agave.URIPattern;
import co.cdev.agave.URIPatternImpl;
import co.cdev.agave.conversion.PassThroughParamConverter;

/**
 * A descriptor that serves as the configuration for the building of handlers
 * and routing requests into them.
 * 
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class HandlerDescriptorImpl implements HandlerDescriptor {

    private static final long serialVersionUID = 1L;
    
    private URIPattern uriPattern;
    private HttpMethod method;
    private List<ParamDescriptor> paramDescriptors; // only applicable when @Param is used
    
    private Class<?> handlerClass;
    private Method handlerMethod;
    private Class<?> formClass;
    private boolean initiatesWorkflow;
    private boolean completesWorkflow;
    private String workflowName;

    public HandlerDescriptorImpl(ScanResult scanResult) throws ClassNotFoundException, InvalidHandlerException {
        uriPattern       = new URIPatternImpl(scanResult.getUri());
        method           = scanResult.getMethod();
        handlerClass     = Class.forName(scanResult.getClassName());
        paramDescriptors = Collections.emptyList();
    }

    /**
     * Locates annotated methods on a handler class. This method will find the
     * annotated method that is identified by the supplied
     * {@code Scanresult} and then proceed to find any workflow-related
     * annotations ({@code InitiatesWorkflow}, {@code ResumesWorkflow},
     * {@code CompletesWorkflow}.
     * 
     * @param scanResult
     *            the {@code HandlerIdentifier} that was created while scanning
     *            for a handler method
     */
    @Override
    public void locateAnnotatedHandlerMethods(ScanResult scanResult) throws InvalidHandlerException {
        for (Method m : handlerClass.getMethods()) {
            
            initiatesWorkflow = false;
            completesWorkflow = false;
            workflowName = null;
            
            // Interpret workflow-related information
            
            if (m.getAnnotation(InitiatesWorkflow.class) != null) {
                initiatesWorkflow = true;
                completesWorkflow = false;
                workflowName = m.getAnnotation(InitiatesWorkflow.class).value();
            } else if (m.getAnnotation(CompletesWorkflow.class) != null) {
                initiatesWorkflow = false;
                completesWorkflow = true;
                workflowName = m.getAnnotation(CompletesWorkflow.class).value();
            } else if (m.getAnnotation(ResumesWorkflow.class) != null) {
                initiatesWorkflow = false;
                completesWorkflow = false;
                workflowName = m.getAnnotation(ResumesWorkflow.class).value();
            }
            
            // Determine if the handler method matches the scan result
            
            Class<?>[] parameterTypes = m.getParameterTypes();
            int expectedParameterCount = scanResult.getParameterTypes().size();
            
            if (scanResult.getMethodName().equals(m.getName()) && expectedParameterCount == parameterTypes.length) {
                if (parameterTypes.length == 1 && !hasAdditionalParams(m)) {
                    handlerMethod = m;
                    return;
                } else if (parameterTypes.length == 2 && !hasNamedParams(m)) {
                    handlerMethod = m;
                    formClass = parameterTypes[1];
                    return;
                } else if (expectedParameterCount == parameterTypes.length) {
                    for (int i = 1; i < parameterTypes.length; i++) {
                        Class<?> expectedType = scanResult.getParameterTypes().get(i);
                        Class<?> actualType = parameterTypes[i];
                        Annotation[] annotations = m.getParameterAnnotations()[i];
                        
                        if (expectedType == actualType) {
                            try {
                                ParamDescriptor parameterDescriptor;
                                parameterDescriptor = createParamDescriptorIfApplicable(actualType, annotations);
                                addParamDescriptor(parameterDescriptor);
                            } catch (InvalidParamException ex) {
                                throw new InvalidHandlerException(ex);
                            }
                        }                        
                    }
                    
                    if (paramDescriptors.size() == expectedParameterCount - 1) {
                        handlerMethod = m;
                        return;
                    } else {
                        paramDescriptors.clear();
                    }
                }
            }
        }
        
        if (handlerMethod == null) {
            throw new InvalidHandlerException(String.format("Unable to find handler method for %s", scanResult.getUri()));
        }
    }
    
    private ParamDescriptor createParamDescriptorIfApplicable(Class<?> paramType, Annotation[] annotations) 
            throws InvalidParamException {
        
        ParamDescriptor descriptor = null;
        
        for (int i = 0; i < annotations.length; i++) {
            Class<?> annotationType = annotations[i].annotationType();
            
            if (annotationType.isAssignableFrom(Param.class)) {
                Param param = (Param) annotations[i];
                String name = param.name();
                
                if (name == null || "".equals(name)) {
                    name = param.value();
                } 
                
                if (name != null) {
                    descriptor = new ParamDescriptorImpl(paramType, name);
                    
                    if (!param.converter().equals(PassThroughParamConverter.class)) {
                        descriptor.setConverter(param.converter());
                    }
                }
                
                if (name == null) {
                    
                }
            }
        }
        
        return descriptor;
    }

    protected void addParamDescriptor(ParamDescriptor paramDescriptor) {
        if (paramDescriptors.isEmpty()) {
            paramDescriptors = new ArrayList<ParamDescriptor>();
        }
        paramDescriptors.add(paramDescriptor);
    }
    
    private boolean hasAdditionalParams(Method m) {
        return m.getParameterTypes().length > 1;
    }
    
    private boolean hasNamedParams(Method m) {
        Annotation[][] annotations = m.getParameterAnnotations();
        
        for (int i = 1; i < annotations.length; i++) {
            for (int j = 0; j < annotations[i].length; j++) {
                if (annotations[i][j].annotationType() == Param.class) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public URIPattern getURIPattern() {
        return uriPattern;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    @Override
    public Class<?> getFormClass() {
        return formClass;
    }

    @Override
    public Method getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public boolean initiatesWorkflow() {
        return initiatesWorkflow;
    }

    @Override
    public boolean completesWorkflow() {
        return completesWorkflow;
    }

    @Override
    public String getWorkflowName() {
        return workflowName;
    }

    @Override
    public int compareTo(HandlerDescriptor that) {
        int result = uriPattern.compareTo(that.getURIPattern());
        
        if (result == 0) {
            result = method.ordinal() - that.getMethod().ordinal();
        }
        
        if (result == 0) {
            result = -(paramDescriptors.size() - that.getParamDescriptors().size());
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (completesWorkflow ? 1231 : 1237);
        result = prime * result + ((handlerMethod == null) ? 0 : handlerMethod.hashCode());
        result = prime * result + (initiatesWorkflow ? 1231 : 1237);
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((paramDescriptors == null) ? 0 : paramDescriptors.hashCode());
        result = prime * result + ((uriPattern == null) ? 0 : uriPattern.hashCode());
        result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HandlerDescriptorImpl other = (HandlerDescriptorImpl) obj;
        if (completesWorkflow != other.completesWorkflow)
            return false;
        if (handlerMethod == null) {
            if (other.handlerMethod != null)
                return false;
        } else if (!handlerMethod.equals(other.handlerMethod))
            return false;
        if (initiatesWorkflow != other.initiatesWorkflow)
            return false;
        if (method != other.method)
            return false;
        if (paramDescriptors == null) {
            if (other.paramDescriptors != null)
                return false;
        } else if (!paramDescriptors.equals(other.paramDescriptors))
            return false;
        if (uriPattern == null) {
            if (other.uriPattern != null)
                return false;
        } else if (!uriPattern.equals(other.uriPattern))
            return false;
        if (workflowName == null) {
            if (other.workflowName != null)
                return false;
        } else if (!workflowName.equals(other.workflowName))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "HandlerMethodDescriptorImpl [pattern=" + uriPattern + ", method=" + method + "]";
    }

    @Override
    public List<ParamDescriptor> getParamDescriptors() {
        return paramDescriptors;
    }

    public boolean isCompletesWorkflow() {
        return completesWorkflow;
    }

    public boolean isInitiatesWorkflow() {
        return initiatesWorkflow;
    }
    
}