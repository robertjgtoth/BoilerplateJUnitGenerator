/*
 * Copyright (c) 2016 Robert Toth
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.rtoth.boilerplate.parameters;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates a parameter initializer.
 */
public class ParameterInitializer
{
    /** Description of the initializer used for test case names. e.g. 'validObj' or 'negativeValue' */
    private final String description;

    /** Text used as code for the initializer. */
    private final String initializerText;

    /**
     * Create a new {@link ParameterInitializer}.
     *
     * @param description Description of the initializer used for test case names, e.g. 'validObj' or 'negativeValue'.
     *                    Cannot be {@code null}.
     * @param initializerText Text used as code for a parameter initializer. This is expected to be only the RHS of a
     *                        variable initialization statement without the semi-colon, e.g. 'mock(Object.class)' or
     *                        '"test value"'. Cannot be {@code null}.
     *
     * @throws NullPointerException if any parameter is {@code null}.
     */
    ParameterInitializer(@NotNull String description, @NotNull String initializerText)
    {
        this.description = Preconditions.checkNotNull(description, "description cannot be null.");
        this.initializerText = Preconditions.checkNotNull(initializerText, "initializerText cannot be null.");
    }

    /**
     * Get the description of the initializer used for test case names.
     *
     * @return Description of the initializer used for test case names, e.g. 'validObj' or 'negativeValue'. Never
     *         {@code null}.
     */
    @NotNull
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the text used as code for a parameter initializer.
     *
     * @return Text used as code for a parameter initializer. This returns only the RHS of a variable initialization
     *         statement without the semi-colon, e.g. 'mock(Object.class)' or '"test value"'. Never {@code null}.
     */
    @NotNull
    public String getInitializerText()
    {
        return initializerText;
    }
}
