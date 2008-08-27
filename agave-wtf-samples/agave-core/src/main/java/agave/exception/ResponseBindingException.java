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
package agave.exception;

import java.lang.reflect.InvocationTargetException;

import agave.internal.HandlerDescriptor;

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class ResponseBindingException extends BindingException {

    private static final long serialVersionUID = 1L;

    public ResponseBindingException() {
        super();
    }

    public ResponseBindingException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public ResponseBindingException(String message) {
        super(message);
    }

    public ResponseBindingException(Throwable rootCause) {
        super(rootCause);
    }

    public ResponseBindingException(HandlerDescriptor descriptor, IllegalAccessException ex) {
        this(getErrorMessage(descriptor), ex);
    }

    public ResponseBindingException(HandlerDescriptor descriptor, InvocationTargetException ex) {
        this(getErrorMessage(descriptor), ex);
    }
    
    // TODO syncronize this
    public synchronized static String getErrorMessage(HandlerDescriptor descriptor) {
        return "Unable to bind the response object with " 
            + descriptor.getHandlerClass().getName() + "#" + descriptor.getRequestSetter();
    }
    
}