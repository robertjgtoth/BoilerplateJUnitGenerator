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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

/**
 * Contains a configurable testing rule set for a single method parameter.
 */
public interface ParameterRule
{
    /**
     * Get whether the current configuration (made through the UI component) is valid.
     *
     * @return {@code true} if the current configuration is valid, {@code false} otherwise.
     */
    boolean isValid();

    /**
     * Get the UI component that can be used to configure this {@link ParameterRule}.
     *
     * @return The UI component that can be used to configure this {@link ParameterRule}. Never {@code null}.
     */
    @NotNull
    JComponent getUiComponent();

    /**
     * Get the {@link PsiType} of the parameter for which this rule applies.
     *
     * @return The {@link PsiType} of the parameter for which this rule applies. Never {@code null}.
     */
    @NotNull
    PsiType getType();

    /**
     * Get the name of the parameter for which this rule applies.
     *
     * @return The name of the parameter for which this rule applies. Never {@code null}.
     */
    @NotNull
    String getName();

    /**
     * Get the list of {@link ParameterInitializer}s that can be used to initialize this parameter such that the
     * resulting value is considered valid.
     *
     * @return An {@link ImmutableList} of {@link ParameterInitializer}s that can be used to initialize this parameter
     *         such that the resulting value is considered valid. Never {@code null} and always contains at least 1
     *         element.
     */
    @NotNull
    ImmutableList<ParameterInitializer> getValidInitializers();

    /**
     * Get a mapping of {@link ParameterInitializer}s that can be used to initialize this parameter such that the
     * resulting value is considered invalid, to the {@link Exception}s expected to be thrown if the parameter is used
     * in its invalid state.
     *
     * @return An {@link ImmutableMap} of {@link ParameterInitializer}s that can be used to initialize this parameter
     *         such that the resulting value is considered invalid, to the {@link Exception}s expected to be thrown if
     *         the parameter is used in its invalid state. Never {@code null}, but may be empty indicating that this
     *         parameter is always valid.
     */
    @NotNull
    ImmutableMap<ParameterInitializer, Class<? extends Exception>> getInvalidInitializers();
}
