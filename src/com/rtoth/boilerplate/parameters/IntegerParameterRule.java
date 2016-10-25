package com.rtoth.boilerplate.parameters;

import com.intellij.ui.JBColor;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.text.Format;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

/**
 * Created by rtoth on 10/24/2016.
 */
public class IntegerParameterRule extends FixedPointNumericParameterRule
{
    private static final Format INTEGER_FORMAT = NumberFormat.getIntegerInstance();

    private final JFormattedTextField value = new JFormattedTextField(INTEGER_FORMAT);

    public IntegerParameterRule(@NotNull String name)
    {
        super("int", name);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 2;
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        value.setMinimumSize(new Dimension(100, (int) value.getPreferredSize().getHeight()));
        this.uiComponent.add(value, constraints);

        numericConstraint.setSelectedItem(NumericConstraint.ANY);
        value.setEnabled(false);
        value.setEditable(false);
        value.setBackground(JBColor.LIGHT_GRAY);

        numericConstraint.addActionListener(e ->
        {
            if (getConstraint().equals(NumericConstraint.ANY))
            {
                value.setEnabled(false);
                value.setEditable(false);
                value.setBackground(JBColor.LIGHT_GRAY);
            }
            else
            {
                value.setEnabled(true);
                value.setEditable(true);
                value.setBackground(JBColor.background());
            }
        });
    }

    public boolean isValid()
    {
        return super.isValid() &&
            (getConstraint().equals(NumericConstraint.ANY) || checkedGetValue() != null);
    }

    private Integer checkedGetValue()
    {
        Integer value;
        try
        {
            value = getValue();
        }
        catch (NumberFormatException nfe)
        {
            value = null;
        }
        return value;
    }

    public int getValue()
    {
        // This should never throw cuz of the formatter!
        return Integer.parseInt(value.getText());
    }
}
