package com.rtoth.boilerplate.parameters;

import com.intellij.openapi.ui.ComboBox;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Created by rtoth on 10/24/2016.
 */
public abstract class FixedPointNumericParameterRule implements ParameterRule
{
    protected final JPanel uiComponent;

    protected final ComboBox<NumericConstraint> numericConstraint =
        new ComboBox<>(NumericConstraint.values());

    public FixedPointNumericParameterRule(@NotNull String type, @NotNull String name)
    {
        // TODO: This layout sucks... fix this.
        this.uiComponent = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        uiComponent.add(new JLabel(type + " " + name + ": "), constraints);
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
