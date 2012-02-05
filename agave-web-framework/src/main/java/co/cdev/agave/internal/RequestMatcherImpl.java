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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import co.cdev.agave.HttpMethod;
import co.cdev.agave.URIParamExtractor;
import co.cdev.agave.URIParamExtractorImpl;
import co.cdev.agave.URIPatternMatcher;
import co.cdev.agave.URIPatternMatcherImpl;
import co.cdev.agave.configuration.Config;
import co.cdev.agave.configuration.HandlerDescriptor;
import co.cdev.agave.configuration.ParamDescriptor;

/**
 * A repository used to group all registered handlers. Handlers are registered
 * by means of scanning the classpath for classes that have methods annotated
 * with the {@code HandlesRequestsTo} annotation.
 * 
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public final class RequestMatcherImpl implements RequestMatcher {

    private Config config;
    
    public RequestMatcherImpl(Config config) {
        this.config = config;
    }

    @Override
    public HandlerDescriptor findMatch(HttpServletRequest request) {
        for (HandlerDescriptor descriptor : config) {
            URIPatternMatcher patternMatcher = new URIPatternMatcherImpl(descriptor.getURIPattern());
            boolean matches = request != null && request.getMethod() != null && patternMatcher.matches(request);
            
            if (matches) {
                HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
                
                matches &= descriptor.getMethod().matches(method);
                
                if (!descriptor.getParamDescriptors().isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> requestParams = request.getParameterMap();
                    
                    URIParamExtractor extractor = new URIParamExtractorImpl(descriptor.getURIPattern());
                    Map<String, String> uriParams = extractor.extractParams(request);
                    
                    for (ParamDescriptor param : descriptor.getParamDescriptors()) {
                        String paramName = param.getName();
                        matches &= requestParams.containsKey(paramName) || uriParams.containsKey(paramName);
                    }
                }
            }
            
            if (matches) {
                return descriptor;
            }
        }
        return null;
    }
    
}