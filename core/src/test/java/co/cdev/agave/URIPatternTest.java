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
package co.cdev.agave;

import org.junit.Assert;
import org.junit.Test;

import co.cdev.agave.URIPattern;
import co.cdev.agave.URIPatternImpl;

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class URIPatternTest {

    @Test // transitively checks testNormalizePattern
    public void testConstructor() throws Exception {
        Assert.assertEquals("/", new URIPatternImpl("/").getPattern());
        Assert.assertEquals("/", new URIPatternImpl("/").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one/").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one/.").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one/./").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one/two/..").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one/two/../").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one/two/../three/four/../../").getPattern());
        Assert.assertEquals("/one", new URIPatternImpl("/one").getPattern());
        Assert.assertEquals("/one/*", new URIPatternImpl("/one/*").getPattern());
        Assert.assertEquals("/one/*", new URIPatternImpl("/one/*/").getPattern());
        Assert.assertEquals("/one/**", new URIPatternImpl("/one/**").getPattern());
        Assert.assertEquals("/one/**", new URIPatternImpl("/one/**/").getPattern());
        Assert.assertEquals("/one/**", new URIPatternImpl("/one/**/*").getPattern());
        Assert.assertEquals("/one/**", new URIPatternImpl("/one/**/**").getPattern());
        Assert.assertEquals("/one/**", new URIPatternImpl("/one/*/**/*/").getPattern());
    }
    
    @Test // for posterity
    public void testNormalizeURI() throws Exception {
        // widen visibility of normalizeURI so we can call it directly
        URIPattern pattern = new URIPatternImpl("/") {
            public String normalizeURI(String uri) {
                return super.normalizeURI(uri);
            }
        };
        Assert.assertEquals("/", pattern.normalizeURI("/"));
        Assert.assertEquals("/one", pattern.normalizeURI("/one"));
        Assert.assertEquals("/one", pattern.normalizeURI("/one/"));
        Assert.assertEquals("/one", pattern.normalizeURI("/one/."));
        Assert.assertEquals("/one", pattern.normalizeURI("/one/./"));
        Assert.assertEquals("/one", pattern.normalizeURI("/one/two/.."));
        Assert.assertEquals("/one", pattern.normalizeURI("/one/two/../"));
        Assert.assertEquals("/one", pattern.normalizeURI("/one/two/../three/four/../../"));
        Assert.assertEquals("/one", pattern.normalizeURI("/one"));
    }
    
    @Test // just making sure this works as expected
    public void testSplit() throws Exception {
        Assert.assertArrayEquals(new String[] {}, "/".split("/"));
    }
    
    @Test(expected = IllegalArgumentException.class) 
    public void testMatchesWithIllegalURI() throws Exception {
        new URIPatternImpl("one/two"); // this is a malformed URI
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMatchesWithNondeterministicURI() throws Exception {
        new URIPatternImpl("/one/two/**/${var}/seven"); // this is a nondeterministic URI
    }
    
    @Test
    public void testCompareTo() throws Exception {
        Assert.assertTrue(new URIPatternImpl("/").compareTo(new URIPatternImpl("/")) == 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/three").compareTo(new URIPatternImpl("/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/").compareTo(new URIPatternImpl("/one/two/three")) > 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/${var}").compareTo(new URIPatternImpl("/one/${var}/three")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/${var}").compareTo(new URIPatternImpl("/one/${var}/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/*").compareTo(new URIPatternImpl("/one/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/*").compareTo(new URIPatternImpl("/one/two/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/*").compareTo(new URIPatternImpl("/one/two/three")) > 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/*/three").compareTo(new URIPatternImpl("/one/two/three/*")) > 0);
        Assert.assertTrue(new URIPatternImpl("/one/two/three/*").compareTo(new URIPatternImpl("/one/two/*/three")) < 0);
        Assert.assertTrue(new URIPatternImpl("/").compareTo(new URIPatternImpl("/*")) > 0);
        Assert.assertTrue(new URIPatternImpl("/*").compareTo(new URIPatternImpl("/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/*").compareTo(new URIPatternImpl("/blah/*")) > 0);
        Assert.assertTrue(new URIPatternImpl("/**").compareTo(new URIPatternImpl("/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/**").compareTo(new URIPatternImpl("/one/")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/**").compareTo(new URIPatternImpl("/one/two")) > 0);
        Assert.assertTrue(new URIPatternImpl("/one/**").compareTo(new URIPatternImpl("/blah/**")) > 0);
        Assert.assertTrue(new URIPatternImpl("/one/**/two/three/*").compareTo(new URIPatternImpl("/blah/**")) < 0);
        Assert.assertTrue(new URIPatternImpl("/one/**/two/*").compareTo(new URIPatternImpl("/blah/**/blah")) < 0);
        Assert.assertTrue(new URIPatternImpl("/a").compareTo(new URIPatternImpl("/b")) < 0);
        Assert.assertTrue(new URIPatternImpl("/b").compareTo(new URIPatternImpl("/a")) > 0);
        Assert.assertTrue(new URIPatternImpl("/A").compareTo(new URIPatternImpl("/a")) == 0);
        Assert.assertTrue(new URIPatternImpl("/init").compareTo(new URIPatternImpl("/${uniqueId}")) < 0);
        Assert.assertTrue(new URIPatternImpl("/${uniqueId}").compareTo(new URIPatternImpl("/init")) > 0);
        Assert.assertTrue(new URIPatternImpl("/${uniqueId1}").compareTo(new URIPatternImpl("/${uniqueId2}")) == 0);
    }

    @Test
    public void testEquals() throws Exception {
        Assert.assertTrue(new URIPatternImpl("/").equals(new URIPatternImpl("/")));
        Assert.assertTrue(new URIPatternImpl("/test1").equals(new URIPatternImpl("/test1")));
        Assert.assertTrue(!new URIPatternImpl("/test1").equals(new URIPatternImpl("/test2")));
    }
    
}

