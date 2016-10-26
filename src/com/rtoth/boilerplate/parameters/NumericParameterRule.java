/**
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

import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * FIXME: docs
 */
public abstract class NumericParameterRule extends AbstractParameterRule
{
    protected final JPanel uiComponent;

    protected final ComboBox<NumericConstraint> numericConstraint =
        new ComboBox<>(NumericConstraint.values());

    public NumericParameterRule(@NotNull PsiType type, @NotNull String name)
    {
        super(type, name);

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

    public NumericConstraint getConstraint()
    {
        return (NumericConstraint) numericConstraint.getSelectedItem();
    }

    @Override
    public boolean isValid()
    {
        return getConstraint() != null;
    }

    @Override
    public JComponent getUiComponent()
    {
        return uiComponent;
    }

    public enum NumericConstraint
    {
        ANY("any"),

        LESS_EQUAL("<="),

        LESS("<"),

        EQUAL("=="),

        GREATER(">"),

        GREATER_EQUAL(">=");

        private final String humanReadable;

        NumericConstraint(@NotNull String humanReadable)
        {
            this.humanReadable = humanReadable;
        }

        @Override
        public String toString()
        {
            return humanReadable;
        }
    }
}
