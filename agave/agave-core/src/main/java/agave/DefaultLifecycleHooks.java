/**
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

import agave.internal.HandlerDescriptor;
import java.io.File;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stub implementations of all the lifecycle hooks.
 * @author <a href="mailto:damianarrillo@gmail.com">Damian Carrillo</a>
 */
public class DefaultLifecycleHooks implements LifecycleHooks {

    public void beforeHandlerIsDiscovered(File potentalHandlerClassFile) {
    }

    public void afterHandlerIsDiscovered(HandlerDescriptor descriptor, ServletContext servletContext) {
    }

    public void beforeFilteringRequest(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }
    
    public void beforeInitializingForm(HandlerDescriptor descriptor, Object formInstance, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void afterInitializingForm(HandlerDescriptor descriptor, Object formInstance, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void beforeSettingServletContext(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void afterSettingServletContext(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void beforeSettingRequest(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void afterSettingRequest(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void beforeSettingResponse(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void afterSettingResponse(HandlerDescriptor descriptor, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext) {
    }

    public void beforeHandlingRequest(HandlerDescriptor descriptor, Object handlerInstance,
        HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    }

    public void afterHandlingRequest(HandlerDescriptor descriptor, Object handlerInstance,
        HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    }
    
    public void afterHandlingRequest(HandlerDescriptor descriptor, Object handlerInstance, Destination destination,
        HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    }

    public void afterHandlingRequest(HandlerDescriptor descriptor, Object handlerInstance, URI destination,
        HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    }
}