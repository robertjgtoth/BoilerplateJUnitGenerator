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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * {@link ParameterRule} which can be used for any numeric value.
 */
// TODO: Support multiple constraints at once
// TODO: Support boxed primitives
abstract class NumericParameterRule extends AbstractParameterRule
{
    /** UI component that can be used to configure this {@link ObjectParameterRule}. */
    final JPanel uiComponent;

    /** Combo box containing the current {@link NumericConstraint} selected by the user. */
    final ComboBox<NumericConstraint> numericConstraint =
        new ComboBox<>(NumericConstraint.values());

    /**
     * Create a new {@link AbstractParameterRule}.
     *
     * @param type {@link PsiType} of the parameter for which this rule applies. Cannot be {@code null} and must be a
     *             {@link PsiPrimitiveType}.
     * @param name Name of the parameter for which this rule applies. Cannot be {@code null} and must have a length of
     *             at least 1.
     *
     * @throws IllegalArgumentException if {@code type} is not a {@link PsiPrimitiveType} or {@code name}'s length is
     *         &lt; 1.
     * @throws NullPointerException if any parameter is {@code null}.
     */
    NumericParameterRule(@NotNull PsiType type, @NotNull String name)
    {
        super(type, name);
        Preconditions.checkArgument(type instanceof PsiPrimitiveType, "type must be a primitive type.");

        // TODO: This layout sucks... fix this.
        this.uiComponent = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        uiComponent.add(new JLabel(type.getPresentableText() + " " + name + ": "), constraints);
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 2;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        uiComponent.add(numericConstraint, constraints);
    }

    /**
     * Get the currently selected {@link NumericConstraint}.
     *
     * @return The currently selected {@link NumericConstraint}. Never {@code null}.
     */
    @NotNull
    NumericConstraint getConstraint()
    {
        return (NumericConstraint) numericConstraint.getSelectedItem();
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @NotNull
    @Override
    public JComponent getUiComponent()
    {
        return uiComponent;
    }

    /**
     * Represents a constraint on a numeric value.
     */
    enum NumericConstraint
    {
        /** Can be anything. */
        ANY("any"),

        /** Must be less than or equal to some value. */
        LESS_EQUAL("<="),

        /** Must be less than some value. */
        LESS("<"),

        /** Must be equal to some value. */
        EQUAL("=="),

        /** Must be greater than some value. */
        GREATER(">"),

        /** Must be greater than or equal to some value. */
        GREATER_EQUAL(">=");

        /** Human readable representation of this {@link NumericConstraint}. */
        private final String humanReadable;

        /**
         * Create a new {@link NumericConstraint} using the provided human readable string.
         *
         * @param humanReadable Human readable representation of the {@link NumericConstraint}. Cannot be {@code null}.
         *
         * @throws NullPointerException if {@code humanReadable} is {@code null}.
         */
        NumericConstraint(@NotNull String humanReadable)
        {
            this.humanReadable = Preconditions.checkNotNull(humanReadable, "humanReadable cannot be null.");
        }

        @Override
        public String toString()
        {
            return humanReadable;
        }
    }
}
