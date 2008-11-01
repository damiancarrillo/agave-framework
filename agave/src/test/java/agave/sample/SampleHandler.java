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
package agave.sample;

import agave.HandlerContext;
import java.io.IOException;

import javax.servlet.ServletException;

import agave.HandlesRequestsTo;

public class SampleHandler {
    @HandlesRequestsTo("/login")
    public void login(HandlerContext context, LoginForm loginForm) throws ServletException, IOException {
        if ("damian".equals(loginForm.getUsername()) && "password".equals(loginForm.getPassword())) {
            context.getRequest().setAttribute("loggedIn", Boolean.TRUE);
        } else {
            context.getRequest().setAttribute("loggedIn", Boolean.FALSE);
        }
        context.getResponse().setStatus(400);
    }

    @HandlesRequestsTo("/aliased")
    public void aliased(HandlerContext context, AliasedForm aliasedForm) throws ServletException, IOException {
    }

    @HandlesRequestsTo("/uri-params/${username}/${password}/")
    public void uriParams(HandlerContext context, LoginForm loginForm) throws ServletException, IOException {
        context.getRequest().setAttribute("username", loginForm.getUsername());
        context.getRequest().setAttribute("password", loginForm.getPassword());
    }

    @HandlesRequestsTo("/throws/nullPointerException")
    public void throwsNullPointerException(HandlerContext context, LoginForm loginForm) throws ServletException, IOException {
        throw new NullPointerException();
    }

    @HandlesRequestsTo("/throws/ioException")
    public void throwsIOException(HandlerContext context, LoginForm loginForm) throws ServletException, IOException {
        throw new IOException();
    }
    
    @HandlesRequestsTo("/lacks/form")
    public void lacksForm(HandlerContext context) throws ServletException, IOException {
        context.getRequest().setAttribute("noErrors", Boolean.TRUE);
    }

}
