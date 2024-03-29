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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import co.cdev.agave.Converter;
import co.cdev.agave.conversion.AgaveConversionException;
import co.cdev.agave.conversion.Converters;
import co.cdev.agave.conversion.ParamConverter;
import co.cdev.agave.util.CallChain;
import co.cdev.agave.util.CallChainImpl;

/**
 * @author <a href="mailto:damianarrillo@gmail.com">Damian Carrillo</a>
 */
public abstract class AbstractFormPopulator extends AbstractPopulator implements FormPopulator {

    private static final String ILLEGAL_ARGUMENT_EXCEPTION_MSG =
            "Mutator {0}#{1}(...) is expecting argument of type {2} and recieved {3}";
    
    protected final SortedMap<String, List<Object>> params = new TreeMap<String, List<Object>>();

    protected AbstractFormPopulator(Locale locale) {
        super(locale);
    }

    @Override
    public SortedMap<String, List<Object>> getParams() {
        return params;
    }

    @Override
    public void populate(Object formInstance)
            throws NoSuchMethodException,
            SecurityException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            InstantiationException,
            AgaveConversionException {
        CallChain callChain = null;
        for (String parameterName : params.keySet()) {
            List<Object> parameterValues = params.get(parameterName);
            boolean unique = true;
            if (parameterValues != null && parameterValues.size() > 1) {
                unique = false;
            }
            callChain = new CallChainImpl(parameterName, unique);
            populateProperty(formInstance, callChain, parameterValues);
        }
    }

    private void populateProperty(Object formInstance, CallChain callChain, List<Object> paramValues)
            throws NoSuchMethodException,
            SecurityException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            InstantiationException,
            AgaveConversionException {
        Object targetInstance = formInstance;
        Class<?> targetClass = targetInstance.getClass();

        for (String accessorName : callChain.getAccessorNames()) {
            try {
                Method accessor = targetClass.getMethod(accessorName);
                targetInstance = accessor.invoke(targetInstance);
                targetClass = targetInstance.getClass();
            } catch (NoSuchMethodException ex) {
                throw new NoSuchMethodException("Missing accessor \"" + accessorName + "\" on " + targetClass.getName()
                        + " invoked through request parameter \"" + callChain.getParameterName() + "\"");
            }
        }

        for (Method mutator : targetClass.getMethods()) {
            if (mutator.getName().equals(callChain.getMutatorName())) {
                Object parameterValue = null;
                switch (callChain.getMutatorType()) {
                    case SETTING:
                        if (paramValues != null && !paramValues.isEmpty()) {
                            parameterValue = paramValues.get(0);
                        }
                        setOrAppendProperty(mutator, targetInstance, parameterValue);
                        break;
                    case APPENDING:
                        for (Object param : paramValues) {
                            setOrAppendProperty(mutator, targetInstance, param);
                        }
                        break;
                    case INSERTING:
                        if (paramValues != null && !paramValues.isEmpty()) {
                            parameterValue = paramValues.get(0);
                        }
                        insertProperty(mutator, targetInstance, callChain.getIndex(), parameterValue);
                        break;
                    case PUTTING:
                        if (paramValues != null && !paramValues.isEmpty()) {
                            parameterValue = paramValues.get(0);
                        }
                        putProperty(mutator, targetInstance, callChain.getKey(), parameterValue);
                        break;
                }
            }
        }
    }

    private void setOrAppendProperty(Method mutator, Object targetInstance, Object paramValue)
            throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            InstantiationException,
            AgaveConversionException {
        try {
            mutator.invoke(targetInstance, convertIfNecessary(mutator, paramValue));
        } catch (IllegalArgumentException ex) {
            String errorMessage = MessageFormat.format(ILLEGAL_ARGUMENT_EXCEPTION_MSG,
                    mutator.getDeclaringClass().getName(),
                    mutator.getName(),
                    mutator.getParameterTypes()[0].getName(),
                    paramValue.getClass().getName());
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void insertProperty(Method mutator, Object targetInstance, int index, Object paramValue)
            throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            InstantiationException,
            AgaveConversionException {
        mutator.invoke(targetInstance, index, convertIfNecessary(mutator, paramValue));
    }

    private void putProperty(Method mutator, Object targetInstance, String key, Object paramValue)
            throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            InstantiationException,
            AgaveConversionException {
        mutator.invoke(targetInstance, key, convertIfNecessary(mutator, paramValue));
    }

    @SuppressWarnings("unchecked")
	private Object convertIfNecessary(Method mutator, Object paramValue)
            throws AgaveConversionException,
            InstantiationException,
            IllegalAccessException {
        Class<?>[] parameterTypes = mutator.getParameterTypes();

        if (parameterTypes != null) {
            int parameterOffset = (parameterTypes.length == 1) ? 0 : 1;
            Class<?> paramType = parameterTypes[parameterOffset];
            
            @SuppressWarnings("rawtypes")
			ParamConverter converter = null; // keep this vague
            
            // First look for a Converter annotation
            
            for (Annotation annotation : mutator.getParameterAnnotations()[parameterOffset]) {
                if (annotation instanceof Converter) {
                    converter = ((Converter) annotation).value().newInstance();
                    break;
                }
            }

            // Try to look up a converter for common types
            
            if (converter == null) {
                converter = Converters.getMostAppropriateFor(paramType);
            }

            if (converter != null) {
                try {
                    return converter.convert(paramValue, locale);
                } catch (Throwable ex) {
                    throw new AgaveConversionException(ex);
                }
            }
        }

        return paramValue;
    }
}
