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
package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import com.rtoth.boilerplate.parameters.IntegerParameterRule;
import com.rtoth.boilerplate.parameters.ObjectParameterRule;
import com.rtoth.boilerplate.parameters.ParameterRule;
import com.rtoth.boilerplate.parameters.StringParameterRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Dialog box which prompts the user for input on which methods to test and how to test them.
 */
class GetTestMethodsDialog extends DialogWrapper
{
    /** Description for this dialog box. */
    private static final String DIALOG_DESCRIPTION = "Select Methods to Test";

    /** ID for the empty card display. */
    private static final String EMPTY_CARD_ID = "EMPTY_CARD";

    /** Message to display when a method has no configurable parameters. */
    private static final JLabel NO_CONFIGURABLE_PARAMETERS =
        new JLabel("No configurable parameters right now.\nThe developers are working on this :)");

    /** Methods available for the user to configure mapped by a check box indicating whether they have been selected. */
    private final ImmutableMap<JCheckBox, PsiMethod> availableMethods;

    /** Configurable parameter rules for each of the {@code availableMethods}. */
    private final ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> parameterRules;

    /** ID of the currently selected card display. */
    private String selectedCardId = EMPTY_CARD_ID;

    /**
     * Create a new {@link GetTestMethodsDialog}.
     *
     * @param sourceClass {@link PsiClass} for which this dialog is configuring test methods. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code sourceClass} is {@code null}.
     */
    GetTestMethodsDialog(@NotNull PsiClass sourceClass)
    {
        super(
            Preconditions.checkNotNull(sourceClass, "sourceClass cannot be null.")
                .getProject()
        );

        ImmutableMap.Builder<JCheckBox, PsiMethod> availableMethodsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<PsiMethod, ImmutableList<ParameterRule>> parameterRulesBuilder =
            ImmutableMap.builder();
        for (PsiMethod method : sourceClass.getMethods())
        {
            // TODO: Update this once non-constructor methods are handled.
            if (PsiUtil.getAccessLevel(method.getModifierList()) != PsiUtil.ACCESS_LEVEL_PRIVATE &&
                method.isConstructor() && method.getParameterList().getParametersCount() > 0)
            {
                JCheckBox checkBox = new JCheckBox(getPresentableMethodSignature(method));
                checkBox.setSelected(false);
                availableMethodsBuilder.put(checkBox, method);
                parameterRulesBuilder.put(method, buildDefaultParameterRules(method));
            }
        }
        this.availableMethods = availableMethodsBuilder.build();
        this.parameterRules = parameterRulesBuilder.build();

        init();
        setTitle(DIALOG_DESCRIPTION);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        // inf rows, 1 column
        // TODO: Lately the cards are showing up squished..
        final JPanel methodSelection = new JPanel(new GridBagLayout());
        final JPanel parameterRuleCards = new JPanel(new CardLayout());
        parameterRuleCards.add(new JLabel("Select a method to configure."), EMPTY_CARD_ID);

        int rowIndex = 0;
        for (Map.Entry<JCheckBox, PsiMethod> methodEntry : availableMethods.entrySet())
        {
            final PsiMethod method = methodEntry.getValue();
            final String methodSignature = getPresentableMethodSignature(method);
            final JCheckBox methodCheckBox = methodEntry.getKey();
            final JButton methodConfigureButton = new JButton("->");
            methodConfigureButton.setEnabled(false);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = rowIndex;
            constraints.anchor = GridBagConstraints.LINE_START;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            methodSelection.add(methodCheckBox, constraints);
            constraints.gridx = 1;
            constraints.gridy = rowIndex;
            constraints.anchor = GridBagConstraints.LINE_END;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            methodSelection.add(methodConfigureButton, constraints);

            // TODO: Better layout here.
            JPanel card = new JPanel(new GridLayout(0, 1));
            ImmutableList<ParameterRule> rules = parameterRules.get(method);
            if (!rules.isEmpty())
            {
                for (ParameterRule rule : rules)
                {
                    card.add(rule.getUiComponent());
                }
            }
            else
            {
                card.add(NO_CONFIGURABLE_PARAMETERS);
            }
            parameterRuleCards.add(card, methodSignature);

            methodCheckBox.addChangeListener(e ->
            {
                // Enable/disable the parameter configuration button based on whether this method
                // is selected (checked)
                methodConfigureButton.setEnabled(methodCheckBox.isSelected());

                // If this method is now selected, and the card view is empty, show the current method's
                // card.
                if (methodCheckBox.isSelected() && selectedCardId.equals(EMPTY_CARD_ID))
                {
                    CardLayout cardLayout = (CardLayout) parameterRuleCards.getLayout();
                    cardLayout.show(parameterRuleCards, methodSignature);
                    selectedCardId = methodSignature;
                }
                // If this method is no longer selected, and this method's card was displayed, make sure
                // we set the card view back to empty
                else if (!methodCheckBox.isSelected() && selectedCardId.equals(methodSignature))
                {
                    CardLayout cardLayout = (CardLayout) parameterRuleCards.getLayout();
                    cardLayout.show(parameterRuleCards, EMPTY_CARD_ID);
                    selectedCardId = EMPTY_CARD_ID;
                }
            });

            methodConfigureButton.addFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                    CardLayout cardLayout = (CardLayout) parameterRuleCards.getLayout();
                    cardLayout.show(parameterRuleCards, methodSignature);
                    selectedCardId = methodSignature;
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                    // Do nothing
                }
            });
            rowIndex++;
        }

        // TODO: Add "show card" method
        CardLayout cardLayout = (CardLayout) parameterRuleCards.getLayout();
        cardLayout.show(parameterRuleCards, EMPTY_CARD_ID);
        selectedCardId = EMPTY_CARD_ID;

        return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            methodSelection, parameterRuleCards);
    }

    @Override
    protected ValidationInfo doValidate()
    {
        boolean valid = true;
        boolean anySelected = false;
        for (Map.Entry<JCheckBox, PsiMethod> entry : availableMethods.entrySet())
        {
            if (entry.getKey().isSelected())
            {
                anySelected = true;
                ImmutableList<ParameterRule> rules = parameterRules.get(entry.getValue());
                if (rules != null)
                {
                    for (ParameterRule rule : rules)
                    {
                        if (!rule.isValid())
                        {
                            valid = false;
                            break;
                        }
                    }
                    if (!valid)
                    {
                        break;
                    }
                }
            }
        }

        if (!anySelected)
        {
            return new ValidationInfo("Must select at least one method!");
        }
        else if (!valid)
        {
            return new ValidationInfo("One or more methods have invalid parameter rules configured!");
        }
        return null;
    }

    /**
     * Get the method rules configured by the user.
     * <p>
     * Note: This method should only be called after this dialog has be displayed and submitted successfully. i.e.
     *       it should only be called after invoking {@link #showAndGet()} returns {@code true}. Failing to do so will
     *       result in this method returning un-initialized data.
     *
     * @return Mapping of {@link PsiMethod}s to test in the source file to {@link ParameterRule}s which indicate how
     *         each of the method's parameters should be tested. Never {@code null} but may be empty. The list of
     *         {@link ParameterRule}s for each method is also guaranteed to contain exactly the number of parameters
     *         defined for the method in the correct order.
     */
    @NotNull
    ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> getSelectedMethodRules()
    {
        ImmutableMap.Builder<PsiMethod, ImmutableList<ParameterRule>> builder = ImmutableMap.builder();
        availableMethods.entrySet().stream().filter(entry -> entry.getKey().isSelected()).forEach(entry ->
            builder.put(entry.getValue(), parameterRules.get(entry.getValue())));

        return builder.build();
    }

    /**
     * Build the list of default {@link ParameterRule}s for the provided method.
     *
     * @param method {@link PsiMethod} for which to build the default parameter rules. Cannot be {@code null}.
     * @return An {@link ImmutableList} of default {@link ParameterRule}s for {@code method}. Never {@code null}, and
     *         will always contain exactly the number of parameters defined for the method in the correct order.
     *
     * @throws NullPointerException if {@code method} is {@code null}.
     */
    @NotNull
    private ImmutableList<ParameterRule> buildDefaultParameterRules(@NotNull PsiMethod method)
    {
        Preconditions.checkNotNull(method, "method cannot be null.");

        ImmutableList.Builder<ParameterRule> rulesBuilder = ImmutableList.builder();
        for (PsiParameter parameter : method.getParameterList().getParameters())
        {
            PsiType type = parameter.getType();
            String name = parameter.getName();
            if (name != null)
            {
                if (type instanceof PsiPrimitiveType)
                {
                    // TODO: Make this handle more primitives!
                    if (type.equals(PsiType.INT))
                    {
                        rulesBuilder.add(new IntegerParameterRule(name));
                    }
                    else
                    {
                        throw new IllegalStateException("This functionality cannot currently be used on a class " +
                            "containing an unsupported parameter type: " + type.getPresentableText());
                    }
                }
                else if (type.getCanonicalText().equals("java.lang.String"))
                {
                    rulesBuilder.add(new StringParameterRule(type, name));
                }
                else
                {
                    // TODO: What if it's an array? or something else?
                    rulesBuilder.add(new ObjectParameterRule(type, name));
                }
            }
            else
            {
                throw new IllegalStateException("Unexpected error retrieving method parameter information. " +
                    "Parameter of type " + type.getPresentableText() + " on method " + method.getName() +
                    " has a null name.");
            }
        }

        return rulesBuilder.build();
    }

    /**
     * Get the presentable method signature for the provided {@link PsiMethod}.
     * <p>
     * The format returned for constructors is (depending on whether there are any parameters):
     *      Constructor: (Type1 name1, Type2 name2, ...)
     *      or
     *      Constructor: (no parameters)
     *
     * The format returned for methods is (depending on whether there are any parameters):
     *      returnType methodName: (Type1 name1, Type2 name2, ...)
     *      or
     *      returnType methodName: (no parameters)
     *
     * @param method Method for which to get the presentable method signature. Cannot be {@code null}.
     * @return The presentable method signature for {@code method}. Never {@code null}.
     *
     * @throws NullPointerException if {@code method} is {@code null}.
     */
    @NotNull
    private String getPresentableMethodSignature(@NotNull PsiMethod method)
    {
        Preconditions.checkNotNull(method, "method cannot be null.");

        StringBuilder signatureBuilder;
        if (method.isConstructor())
        {
            signatureBuilder = new StringBuilder("Constructor: (");
        }
        else
        {
            PsiType returnType = method.getReturnType();
            if (returnType != null)
            {
                signatureBuilder = new StringBuilder(returnType.getPresentableText() + " " + method.getName() + ": (");
            }
            else
            {
                throw new IllegalStateException("Unexpected error retrieving method descriptions. Non-constructor " +
                    "Method " + method.getName() + " has null return type.");
            }
        }

        PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length > 0)
        {
            boolean first = true;
            for (PsiParameter parameter : method.getParameterList().getParameters())
            {
                if (!first)
                {
                    signatureBuilder.append(", ");
                }
                signatureBuilder.append(parameter.getType().getPresentableText());
                signatureBuilder.append(" ");
                signatureBuilder.append(parameter.getName());
                first = false;
            }
        }
        else
        {
            signatureBuilder.append("no parameters");
        }
        signatureBuilder.append(")");

        return signatureBuilder.toString();
    }
}
