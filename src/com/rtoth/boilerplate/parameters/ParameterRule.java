package com.rtoth.boilerplate.parameters;

import javax.swing.JComponent;

/**
 * Created by rtoth on 10/24/2016.
 */
public interface ParameterRule
{
    boolean isValid();

    JComponent getUiComponent();
}
