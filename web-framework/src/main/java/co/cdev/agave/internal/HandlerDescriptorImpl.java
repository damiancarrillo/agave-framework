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
package co.cdev.agave.internal;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import co.cdev.agave.CompletesWorkflow;
import co.cdev.agave.Converter;
import co.cdev.agave.HttpMethod;
import co.cdev.agave.InitiatesWorkflow;
import co.cdev.agave.Param;
import co.cdev.agave.ResumesWorkflow;
import co.cdev.agave.conversion.StringParamConverter;
import co.cdev.agave.exception.InvalidHandlerException;
import java.lang.annotation.Annotation;

/**
 * A descriptor that serves as the configuration for the building of handlers
 * and routing requests into them.
 * 
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public final class HandlerDescriptorImpl implements HandlerDescriptor {

    private URIPattern pattern;
    private HttpMethod method;
    private Class<?> handlerClass;
    private Method handlerMethod;
    private String[] paramNames;
    private Class<StringParamConverter<?>>[] converterClasses;
    private Class<?> formClass;
    private boolean initiatesWorkflow;
    private boolean completesWorkflow;
    private String workflowName;

    public HandlerDescriptorImpl(HandlerIdentifier identifier) throws ClassNotFoundException, InvalidHandlerException {
        pattern = new URIPatternImpl(identifier.getUri());
        method = identifier.getMethod();
        handlerClass = Class.forName(identifier.getClassName());
        locateAnnotatedHandlerMethods(identifier);
    }

    /**
     * Locates annotated methods on a handler class. This method will find the
     * annotated method that is identified by the supplied
     * {@code HandlerIdentifier} and then proceed to find any workflow-related
     * annotations ({@code InitiatesWorkflow}, {@code ResumesWorkflow},
     * {@code CompletesWorkflow}.
     * 
     * @param identifier
     *            the {@code HandlerIdentifier} that was created while scanning
     *            for a handler method
     */
    @Override
    public void locateAnnotatedHandlerMethods(HandlerIdentifier identifier) throws InvalidHandlerException {
        for (Method m : handlerClass.getMethods()) {
            if (identifier.getMethodName().equals(m.getName())) {
                handlerMethod = m;
                
                int parameterCount = handlerMethod.getParameterTypes().length;
                
                // Account for the HandlerContext as the 0th argument
                
                if (parameterCount > 2) {
                    int annotationCount = handlerMethod.getParameterAnnotations().length;
                    
                    paramNames = new String[annotationCount];
                    converterClasses = new Class[annotationCount];
                    
                    for (int i = 1; i < annotationCount; i++) {
                        Annotation[] parameterAnnotations = handlerMethod.getParameterAnnotations()[i];
                        
                        for (int j = 0; j < parameterAnnotations.length; i++) {
                            if (Param.class.equals(parameterAnnotations[j].annotationType())) {
                                Param annotation = (Param) parameterAnnotations[j];
                                paramNames[i - 1] = annotation.value();
                            } else if (Converter.class.equals(parameterAnnotations[j].annotationType())) {
                                Converter annotation = (Converter) parameterAnnotations[j];
                                
                                if (annotation.value().isAssignableFrom(StringParamConverter.class)) {
                                    converterClasses[i - 1] = (Class) annotation.value();
                                } else {
                                    String format = "Parameter %d with type %s of %s has an an "
                                            + "invalid Converter specified.  It should be a subclass "
                                            + "of StringParamConverter.";
                                    Class<?> parameterType = handlerMethod.getParameterTypes()[i];
                                    throw new InvalidHandlerException(String.format(format, i, parameterType.getName(), handlerMethod));
                                }
                            }
                            
                            if (paramNames[i - 1] == null) {
                                String format = "Paramater %d with type %s of %s lacks a @Param annotation";
                                Class<?> parameterType = handlerMethod.getParameterTypes()[i];
                                throw new InvalidHandlerException(String.format(format, i, parameterType.getName(), handlerMethod));
                            }
                        }
                    }
                } else if (parameterCount == 2) {
                    formClass = handlerMethod.getParameterTypes()[1];
                }

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
            }
        }
    }

    @Override
    public URIPattern getPattern() {
        return pattern;
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
        int result = pattern.compareTo(that.getPattern());
        if (result == 0) {
            result = method.ordinal() - that.getMethod().ordinal();
        }
        return result;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof HandlerDescriptor)) {
            return false;
        }
        HandlerDescriptor desc = (HandlerDescriptor) that;
        boolean equal = pattern.equals(desc.getPattern()) && method == desc.getMethod();
        return equal;
    }

    @Override
    public int hashCode() {
        return pattern.hashCode() + method.name().hashCode();
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return request != null && method.matches(request)
                && pattern.matches(request);
    }
}
