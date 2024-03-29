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
package co.cdev.agave.web;

import co.cdev.agave.configuration.HandlerDescriptor;

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class FormException extends AgaveWebException {
    
    private static final long serialVersionUID = 1L;

    public FormException() {
        super();
    }

    public FormException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public FormException(String message) {
        super(message);
    }

    public FormException(Throwable rootCause) {
        super(rootCause);
    }

    // TODO internationalize this
    public FormException(HandlerDescriptor descriptor, InstantiationException rootCause) {
        this(getErrorMessage(descriptor), rootCause);
            
    }
    
    // TODO internationalize this
    public FormException(HandlerDescriptor descriptor, IllegalAccessException rootCause) {
        this(getErrorMessage(descriptor), rootCause);
    }
    
    private synchronized static String getErrorMessage(HandlerDescriptor descriptor) {
       return  "Unable to create an instance of form " + descriptor.getFormClass() + " for "
           + descriptor.getHandlerClass().getName() + "#" + descriptor.getHandlerMethod().getName();
    }
    
}
