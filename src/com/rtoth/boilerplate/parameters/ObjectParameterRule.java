package com.rtoth.boilerplate.parameters;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Created by rtoth on 10/24/2016.
 */
public class ObjectParameterRule implements ParameterRule
{
    private final JCheckBox disallowNull = new JCheckBox("Disallow null");

    protected final JPanel uiComponent;

    public ObjectParameterRule(@NotNull String type, @NotNull String name)
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
        uiComponent.add(disallowNull, constraints);
    }

    public boolean disallowNull()
    {
        return disallowNull.isSelected();
    }

    public boolean isValid()
    {
        return true;
    }

    @Override
    public JComponent getUiComponent()
    {
        return uiComponent;
    }
}
