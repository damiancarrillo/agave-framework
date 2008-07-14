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

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class SimpleClassEnvironment implements ClassEnvironment {

    public void initializeEnvironment() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
        InstantiationException {
        // sit there and look sexy
    }

    /**
     * Creates a new instance of a form object for the form class specified in the supplied descriptor.
     * @param descriptor the handler descriptor that describes which form to instantiate
     * @throws FormError when a form instance failed to be instantiated
     */
    public Object createFormInstance(HandlerDescriptor descriptor) throws FormError {
        Object formInstance = null;
        if (descriptor.getFormClass() != null) {
            try {
                formInstance = descriptor.getFormClass().newInstance();
            } catch (InstantiationException ex) {
                throw new FormError("Unable to instantiate a form instance for: " + 
                    descriptor.getFormClass().getName(), ex);
            } catch (IllegalAccessException ex) {
                throw new FormError("Unable to instantiate a form instance for: " + 
                    descriptor.getFormClass().getName(), ex);
            }
        }
        return formInstance;
    }


    /**
     * Creates a new instance of a handler object for the handler class specified in the supplied descriptor.
     * @param descriptor the handler descriptor that describes which form to instantiate.
     * @throws FormError when a handler instance failed to be instantiated
     */
    public Object createHandlerInstance(HandlerDescriptor descriptor) throws HandlerError {
        Object handlerInstance = null;
        if (descriptor.getHandlerClass() != null) {
            try {
                handlerInstance = descriptor.getHandlerClass().newInstance();
            } catch (InstantiationException ex) {
                throw new HandlerError("Unable to instantiate a handler instance for: " + 
                    descriptor.getHandlerClass().getName(), ex);
            } catch (IllegalAccessException ex) {
                throw new HandlerError("Unable to instantiate a handler instance for: " + 
                    descriptor.getClass().getName(), ex);
            }
        }
        return handlerInstance;
    }

}
