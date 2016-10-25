package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.project.Project;
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
 * Created by rtoth on 10/23/2016.
 */
public class GetTestMethodsDialog extends DialogWrapper
{
    private static final String EMPTY_CARD_ID = "EMPTY_CARD";

    private static final JLabel NO_CONFIGURABLE_PARAMETERS =
        new JLabel("No configurable parameters right now.\nThe developers are working on this :)");

    private final PsiClass sourceClass;

    private final ImmutableMap<JCheckBox, PsiMethod> availableMethods;

    private final ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> parameterRules;

    private String selectedCardId = EMPTY_CARD_ID;

    public GetTestMethodsDialog(@NotNull Project project, @NotNull PsiClass sourceClass)
    {
        super(project);

        this.sourceClass = Preconditions.checkNotNull(sourceClass, "sourceClass cannot be null.");
        ImmutableMap.Builder<JCheckBox, PsiMethod> availableMethodsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<PsiMethod, ImmutableList<ParameterRule>> parameterRulesBuilder =
            ImmutableMap.builder();
        for (PsiMethod method : sourceClass.getMethods())
        {
            // TODO: Update this once non-constructor methods are handled.
            if (PsiUtil.getAccessLevel(method.getModifierList()) != PsiUtil.ACCESS_LEVEL_PRIVATE &&
                method.isConstructor() && method.getParameterList().getParametersCount() > 0)
            {
                JCheckBox checkBox = new JCheckBox(getMethodSignature(method));
                checkBox.setSelected(false);
                availableMethodsBuilder.put(checkBox, method);
                parameterRulesBuilder.put(method, getParameterRules(method));
            }
        }
        this.availableMethods = availableMethodsBuilder.build();
        this.parameterRules = parameterRulesBuilder.build();

        init();
        setTitle("Select non-private methods to test.");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        // inf rows, 1 column
        final JPanel methodSelection = new JPanel(new GridBagLayout());
        final JPanel parameterRuleCards = new JPanel(new CardLayout());
        parameterRuleCards.add(new JLabel("Select a method to configure."), EMPTY_CARD_ID);

        int rowIndex = 0;
        for (Map.Entry<JCheckBox, PsiMethod> methodEntry : availableMethods.entrySet())
        {
            final PsiMethod method = methodEntry.getValue();
            final String methodSignature = getMethodSignature(method);
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

    public ImmutableList<PsiMethod> getSelectedMethods()
    {
        ImmutableList.Builder<PsiMethod> builder = ImmutableList.builder();
        availableMethods.entrySet().stream().filter(entry -> entry.getKey().isSelected()).forEach(entry ->
            builder.add(entry.getValue()));

        return builder.build();
    }

    // TODO: Use this method instead of the one above to generate tests!!
    public ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> getSelectedMethodRules()
    {
        ImmutableMap.Builder<PsiMethod, ImmutableList<ParameterRule>> builder = ImmutableMap.builder();
        availableMethods.entrySet().stream().filter(entry -> entry.getKey().isSelected()).forEach(entry ->
            builder.put(entry.getValue(), parameterRules.get(entry.getValue())));

        return builder.build();
    }

    private ImmutableList<ParameterRule> getParameterRules(@NotNull PsiMethod method)
    {
        ImmutableList.Builder<ParameterRule> rulesBuilder = ImmutableList.builder();
        for (PsiParameter parameter : method.getParameterList().getParameters())
        {
            if (parameter.getType() instanceof PsiPrimitiveType)
            {
                if (parameter.getType().equals(PsiType.INT))
                {
                    rulesBuilder.add(new IntegerParameterRule(parameter.getName()));
                }
                // TODO: Make this handle more primitives!
            }
            else if (parameter.getType().getCanonicalText().equals("java.lang.String"))
            {
                rulesBuilder.add(new StringParameterRule(parameter.getName()));
            }
            else
            {
                // TODO: What if it's an array?
                rulesBuilder.add(new ObjectParameterRule(parameter.getType().getPresentableText(), parameter.getName()));
            }
        }

        return rulesBuilder.build();
    }

    private String getMethodSignature(@NotNull PsiMethod method)
    {
        Preconditions.checkNotNull(method, "method cannot be null.");

        StringBuilder signatureBuilder = new StringBuilder(
            method.isConstructor() ? "Constructor: (" :
                method.getReturnType().getPresentableText() + " " + method.getName() + ": (");

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
                signatureBuilder.append(parameter.getType().getPresentableText() + " " + parameter.getName());
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
