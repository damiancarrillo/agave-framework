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
package agave.internal;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import agave.HttpMethod;

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class HandlerDescriptorTest {
    private static final String cls = "agave.sample.SampleHandler";
    private static final String met = "login";

    Mockery context = new Mockery();
    HttpServletRequest request;
    
    @Before
    public void setUp() {
    	request = context.mock(HttpServletRequest.class);
    }
    
    @Test
    public void testConstructor() throws Exception {
        HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
        Assert.assertNotNull(a);
        Assert.assertNotNull(a.getPattern());
        Assert.assertNotNull(a.getHandlerClass());
        Assert.assertNotNull(a.getFormClass());
        Assert.assertNotNull(a.getHandlerMethod());
    }

    @Test
    public void testEquals() throws Exception {
        HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
        HandlerDescriptor b = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
        HandlerDescriptor c = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/notLogin", cls, met));
        Assert.assertEquals(a, b);
        Assert.assertFalse(a.equals(c));
    }

    @Test
    public void testCompareTo() throws Exception {
        HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
        HandlerDescriptor b = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
        Assert.assertEquals(0, a.compareTo(b));
        
        a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/a", cls, met));
        b = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/b", cls, met));
        Assert.assertEquals(-1, a.compareTo(b));
        Assert.assertEquals(1, b.compareTo(a));
    }
    
    @Test
    public void testMatches_withNullRequest() throws Exception {
    	HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
    	Assert.assertFalse(a.matches(null));
    }
    
    @Test
    public void testMatches_withAnyHttpMethodAndMatchingURI() throws Exception {
    	context.checking(new Expectations() {{
    		allowing(request).getMethod(); will(returnValue("GET"));
    		allowing(request).getServletPath(); will(returnValue("/login"));
    	}});
    	
    	HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", cls, met));
    	Assert.assertTrue(a.matches(request));
    }
    
    @Test
    public void testMatches_withMatchingMethodAndMatchingURI() throws Exception {
    	context.checking(new Expectations() {{
    		allowing(request).getMethod(); will(returnValue("GET"));
    		allowing(request).getServletPath(); will(returnValue("/login"));
    	}});
    	
    	HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", HttpMethod.GET, cls, met));
    	Assert.assertTrue(a.matches(request));
    }
    
    @Test
    public void testMatches_withNonMethodAndMatchingURI() throws Exception {
    	context.checking(new Expectations() {{
    		allowing(request).getMethod(); will(returnValue("GET"));
    		allowing(request).getServletPath(); will(returnValue("/login"));
    	}});
    	
    	HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", HttpMethod.POST, cls, met));
    	Assert.assertFalse(a.matches(request));
    }

    @Test
    public void testMatches_withMatchingMethodAndNonMatchingURI() throws Exception {
    	context.checking(new Expectations() {{
    		allowing(request).getMethod(); will(returnValue("GET"));
    		allowing(request).getServletPath(); will(returnValue("/logout"));
    	}});
    	
    	HandlerDescriptor a = new HandlerDescriptorImpl(new HandlerIdentifierImpl("/login", HttpMethod.GET, cls, met));
    	Assert.assertFalse(a.matches(request));
    }
    
}

