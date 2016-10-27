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
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * {@link ParameterRule} which can be used for any single, non-primitive {@link Object} or subclass.
 */
public class ObjectParameterRule extends AbstractParameterRule
{
    /** UI component that can be used to configure this {@link ObjectParameterRule}. */
    final JPanel uiComponent;

    /** Check box containing whether null values should be allowed for this parameter. */
    private final JCheckBox disallowNull = new JCheckBox("Disallow null");

    /**
     * Create a new {@link AbstractParameterRule}.
     *
     * @param type {@link PsiType} of the parameter for which this rule applies. Cannot be {@code null} and must not
     *             be a {@link PsiPrimitiveType}.
     * @param name Name of the parameter for which this rule applies. Cannot be {@code null} and must have a length of
     *             at least 1.
     *
     * @throws IllegalArgumentException if {@code type} is a {@link PsiPrimitiveType} or {@code name}'s length is
     *         &lt; 1.
     * @throws NullPointerException if any parameter is {@code null}.
     */
    public ObjectParameterRule(@NotNull PsiType type, @NotNull String name)
    {
        super(type, name);
        Preconditions.checkArgument(!(type instanceof PsiPrimitiveType), "type cannot be a primitive type.");

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
        uiComponent.add(disallowNull, constraints);
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

    @NotNull
    @Override
    public ImmutableList<ParameterInitializer> getValidInitializers()
    {
        // TODO: Do something else with final classes
        return ImmutableList.of(
            new ParameterInitializer(
                "valid" + getCapitalizedName(),
                "mock(" + getType().getPresentableText() + ".class)"
            )
        );
    }

    @NotNull
    @Override
    public ImmutableMap<ParameterInitializer, Class<? extends Exception>> getInvalidInitializers()
    {
        if (disallowNull.isSelected())
        {
            return ImmutableMap.of(
                new ParameterInitializer(
                    "null" + getCapitalizedName(),
                    "null"
                ),
                NullPointerException.class
            );
        }
        return ImmutableMap.of();
    }
}
