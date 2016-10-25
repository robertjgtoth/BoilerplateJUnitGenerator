package com.rtoth.boilerplate.parameters;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

/**
 * Created by rtoth on 10/24/2016.
 */
public class StringParameterRule extends ObjectParameterRule
{
    private final JCheckBox disallowBlank = new JCheckBox("Disallow blank");

    public StringParameterRule(@NotNull String name)
    {
        super("String", name);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 2;
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.uiComponent.add(disallowBlank, constraints);
    }

    public boolean disallowBlank()
    {
        return disallowBlank.isSelected();
    }
}
