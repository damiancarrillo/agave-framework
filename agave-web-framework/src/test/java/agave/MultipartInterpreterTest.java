/*
 * Copyright (c) 2005 - 2007 Damian Carrillo.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of the <ORGANIZATION> nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package agave;

import java.io.InputStream;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class MultipartInterpreterTest {
	
	InputStream in = getClass().getClassLoader().getResourceAsStream("teststream.txt");
	String boundary = "---------------------------105548196212704111141517174524";
	
	@Test
	public void testConstructor() throws IOException {
		MultipartInterpreter mi = new MultipartInterpreter(in, boundary, true);
		
		assertNotNull(mi.getProperties());
		assertNotNull(mi.getProperties().get("text1"));
		assertEquals(mi.getProperties().get("text1"), "one");
		assertNotNull(mi.getProperties().get("text2"));
		assertEquals(mi.getProperties().get("text2"), "two");
		
		assertNotNull(mi.getFiles());
		assertNotNull(mi.getFiles().get("image1"));
		assertTrue(mi.getFiles().get("image1").exists());
		assertNotNull(mi.getFiles().get("image2"));
		assertTrue(mi.getFiles().get("image2").exists());
	}

}