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
package co.cdev.agave.web;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;

import co.cdev.agave.util.LoggerUtils;
import co.cdev.agave.web.AgaveFilter;

/**
 * @author <a href="mailto:damianarrillo@gmail.com">Damian Carrillo</a>
 * @version $Rev$ $Date$
 */
public abstract class AbstractFunctionalTest {

    protected Mockery context = new Mockery();
    protected FilterChain filterChain;
    protected FilterConfig filterConfig;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletContext servletContext;
    protected RequestDispatcher requestDispatcher;
    protected HttpSession session;

    @Before
    public void setup() throws Exception {
        filterChain = context.mock(FilterChain.class);
        filterConfig = context.mock(FilterConfig.class);
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        servletContext = context.mock(ServletContext.class);
        requestDispatcher = context.mock(RequestDispatcher.class);
        session = context.mock(HttpSession.class);
    }
    
    protected void emulateServletContainer(final Map<String, String[]> parameters)
        throws URISyntaxException {

        final URL rootURL = getClass().getResource("/");

        // When the filter scans for handlers, this will redirect it to use the test class path
        
        final String realPath = new File(rootURL.toURI()).getAbsolutePath();

        context.checking(new Expectations() {{
            allowing(servletContext).getRealPath("/WEB-INF/classes"); will(returnValue(realPath));
            allowing(filterConfig).getServletContext(); will(returnValue(servletContext));
            allowing(filterConfig).getInitParameter(with(any(String.class))); will(returnValue(null));
            allowing(request).getLocale(); will(returnValue(Locale.ENGLISH));
            allowing(request).getSession(true); will(returnValue(session));
            allowing(request).getParameterMap(); will(returnValue(parameters));
            allowing(request).getParameterNames(); will(returnValue(new Vector<String>(parameters.keySet()).elements()));
            for (String parameter : parameters.keySet()) {
                allowing(request).getParameterValues(parameter); will(returnValue(parameters.get(parameter)));
            }
        }});
    }
    
    protected AgaveFilter scanForHandlerMethods() throws Exception {
        AgaveFilter filter = new AgaveFilter();
        
        Logger agaveFilterLogger = LogManager.getLogManager().getLogger(AgaveFilter.class.getName());
        if (agaveFilterLogger != null) {
            agaveFilterLogger.setLevel(Level.OFF);
        }
        
        emulateServletContainer(new HashMap<String, String[]>());
        
        filter.init(filterConfig);
        
        return filter;
    }
    
    protected AgaveFilter createSilentAgaveFilter() throws Exception {
        AgaveFilter filter = new AgaveFilter();
        
        LoggerUtils.silenceLoggers();
        
        return filter;
    }
    
    protected void expectDiagnosticInformationOnException() {
        context.checking(new Expectations() {{
            allowing(request).getRemoteHost();
            allowing(request).getRemoteAddr();
            allowing(request).getRemotePort();
            allowing(request).getRemoteUser();
        }});
    }
    
}
