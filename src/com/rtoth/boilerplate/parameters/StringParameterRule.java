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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

/**
 * {@link ParameterRule} which can be used for {@link String}s.
 */
public class StringParameterRule extends ObjectParameterRule
{
    /** Canonical text for {@link String} {@link PsiType}. */
    private static final String STRING_CANONICAL_TEXT = "java.lang.String";

    /** Check box containing whether blank values should be allowed for this parameter. */
    private final JCheckBox disallowBlank = new JCheckBox("Disallow blank");

    /**
     * Create a new {@link AbstractParameterRule}.
     *
     * @param type {@link PsiType} of the parameter for which this rule applies. Cannot be {@code null} and must be
     *             {@value #STRING_CANONICAL_TEXT}.
     * @param name Name of the parameter for which this rule applies. Cannot be {@code null} and must have a length of
     *             at least 1.
     *
     * @throws IllegalArgumentException if {@code type} is not {@value #STRING_CANONICAL_TEXT} or {@code name}'s
     *         length is &lt; 1.
     * @throws NullPointerException if any parameter is {@code null}.
     */
    public StringParameterRule(@NotNull PsiType type, @NotNull String name)
    {
        super(type, name);
        Preconditions.checkArgument(type.getCanonicalText().equals(STRING_CANONICAL_TEXT),
            "type must be String.");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 2;
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.uiComponent.add(disallowBlank, constraints);
    }

    @NotNull
    @Override
    public ImmutableList<ParameterInitializer> getValidInitializers()
    {
        return ImmutableList.of(
            new ParameterInitializer(
                "valid" + getCapitalizedName(),
                "\"test\""
            )
        );
    }

    @NotNull
    @Override
    public ImmutableMap<ParameterInitializer, Class<? extends Exception>> getInvalidInitializers()
    {
        ImmutableMap.Builder<ParameterInitializer, Class<? extends Exception>> initializers = ImmutableMap.builder();
        initializers.putAll(super.getInvalidInitializers());
        if (disallowBlank.isSelected())
        {
            initializers.put(
                new ParameterInitializer(
                    "blank" + getCapitalizedName(),
                    "\"\\n\\n  \\t \""
                ),
                IllegalArgumentException.class
            );
        }
        return initializers.build();
    }
}
