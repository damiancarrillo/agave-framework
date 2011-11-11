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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import co.cdev.agave.Destination;

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class DestinationImpl implements Destination {
    
    public static final String ENCODED_AMPERSAND = "&amp;";

    private String path;
    private Map<String, List<String>> parameters = new TreeMap<String, List<String>>();
    private Boolean redirect;
    
    /**
     * Create a new {@code Destination} within the deployed context.  The destination will be 
     * redirected to if the HTTP request method was a POST, otherwise it will be forwarded to.
     * @param path
     */
    public DestinationImpl(String path) {
        setPath(path);
    }
    
    public DestinationImpl(String path, boolean redirect) {
        setPath(path);
        setRedirect(Boolean.valueOf(redirect));
    }
    
    @Override
    public void addParameter(String name, String value) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new ArrayList<String>());
        }
        parameters.get(name).add(value);
    }
    
    @Override
    public String getPath() {
        return path;
    }

    @Override
    public final void setPath(String path) {
        if (!path.contains("://") && !path.startsWith("/")) {
            throw new IllegalArgumentException("Relative destination paths should start with a forward slash '/'; "
                    + "got '" + path + "' instead");
        }
        this.path = path;
    }

    @Override
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Boolean getRedirect() {
        return redirect;
    }
    
    @Override
    public final void setRedirect(Boolean redirect) {
        this.redirect = redirect;
    }
    
    @Override
    public String encode(ServletContext context) {
        StringBuilder encodedPath = new StringBuilder();
        if (getPath() != null) {
            encodedPath.append(getPath());
        }
        if (!getParameters().isEmpty()) {
            encodedPath.append("?");
            boolean first = true;
            for (String parameterName : getParameters().keySet()) {
                Collections.sort(getParameters().get(parameterName));
                for (String parameterValue : getParameters().get(parameterName)) {
                    if (!first) {
                        encodedPath.append("&");
                    }
                    if (parameterValue.contains("&")) {
                        parameterValue = parameterValue.replace("&", ENCODED_AMPERSAND);
                    }
                    encodedPath.append(parameterName).append("=").append(parameterValue);
                    first = false;
                }
            }
        }
        return encodedPath.toString();
    }

    @Override
    public String toString() {
        return String.format("Destination[path:%s, redirect: %b]", this.path, this.redirect);
    }
    
}