package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.CollapsiblePanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Created by rtoth on 10/23/2016.
 */
public class GetTestMethodsDialog extends DialogWrapper
{
    private final PsiClass sourceClass;

    private final ImmutableMap<JCheckBox, PsiMethod> availableMethods;

    private final ImmutableMap<PsiMethod, ImmutableMap<PsiParameter, ComboBox<ParameterCreationStrategy>>> parameterStrategiesByMethod;

    public GetTestMethodsDialog(@NotNull Project project, @NotNull PsiClass sourceClass)
    {
        super(project);

        this.sourceClass = Preconditions.checkNotNull(sourceClass, "sourceClass cannot be null.");
        ImmutableMap.Builder<JCheckBox, PsiMethod> availableMethodsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<PsiMethod, ImmutableMap<PsiParameter, ComboBox<ParameterCreationStrategy>>> parameterStrategiesBuilder =
            ImmutableMap.builder();
        for (PsiMethod method : sourceClass.getMethods())
        {
            // TODO: Update this once non-constructor methods are handled.
            if (PsiUtil.getAccessLevel(method.getModifierList()) != PsiUtil.ACCESS_LEVEL_PRIVATE &&
                method.isConstructor() && method.getParameterList().getParametersCount() > 0)
            {
                JCheckBox checkBox = new JCheckBox(getMethodSignature(method));
                availableMethodsBuilder.put(checkBox, method);
                parameterStrategiesBuilder.put(method, getParameterStrategies(method));
            }
        }
        this.availableMethods = availableMethodsBuilder.build();
        this.parameterStrategiesByMethod = parameterStrategiesBuilder.build();

        init();
        setTitle("Select non-private methods to test.");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        // inf rows, 1 column
        JPanel mainPanel = new JPanel(new GridLayout(0, 1));

        for (Map.Entry<JCheckBox, PsiMethod> methodEntry : availableMethods.entrySet())
        {
            mainPanel.add(methodEntry.getKey());
            final ImmutableMap<PsiParameter, ComboBox<ParameterCreationStrategy>> parameterStrategies =
                parameterStrategiesByMethod.get(methodEntry.getValue());
            CollapsiblePanel collapsiblePanel = null;
            if (parameterStrategies != null)
            {
                JPanel parameterPanel = new JPanel(new GridLayout(0, 2));
                for (Map.Entry<PsiParameter, ComboBox<ParameterCreationStrategy>> parameterEntry :
                    parameterStrategies.entrySet())
                {
                    parameterPanel.add(new JLabel(parameterEntry.getKey().getName()));
                    parameterPanel.add(parameterEntry.getValue());
                }

                collapsiblePanel = new CollapsiblePanel(parameterPanel, true);
                collapsiblePanel.setEnabled(false);
                collapsiblePanel.collapse();
                mainPanel.add(collapsiblePanel);
            }
            if (collapsiblePanel != null)
            {
                CollapsiblePanel finalCollapsiblePanel = collapsiblePanel;
                methodEntry.getKey().addChangeListener(evt ->
                {
                    JCheckBox checkBox = (JCheckBox) evt.getSource();
                    if (checkBox.isSelected())
                    {
                        finalCollapsiblePanel.expand();
                    }
                    else
                    {
                        finalCollapsiblePanel.collapse();
                    }
                    for (Map.Entry<PsiParameter, ComboBox<ParameterCreationStrategy>> entry :
                        parameterStrategies.entrySet())
                    {
                        entry.getValue().setEnabled(checkBox.isSelected());
                    }
                });
            }
        }

        return mainPanel;
    }

    @Override
    protected ValidationInfo doValidate()
    {
        for (Map.Entry<JCheckBox, PsiMethod> entry : availableMethods.entrySet())
        {
            if (entry.getKey().isSelected())
            {
                return null;
            }
        }

        return new ValidationInfo("Must select at least one method!");
    }

    public ImmutableList<PsiMethod> getSelectedMethods()
    {
        ImmutableList.Builder<PsiMethod> builder = ImmutableList.builder();
        availableMethods.entrySet().stream().filter(entry -> entry.getKey().isSelected()).forEach(entry ->
            builder.add(entry.getValue()));

        return builder.build();
    }

    public ImmutableList<MethodTestStrategy> getSelectedMethodStrategies()
    {
        ImmutableList.Builder<MethodTestStrategy> builder = ImmutableList.builder();
        for (Map.Entry<JCheckBox, PsiMethod> entry : availableMethods.entrySet())
        {
            if (entry.getKey().isSelected())
            {
                PsiMethod method = entry.getValue();
                Map<PsiParameter, ParameterCreationStrategy> parameterStrategies = Maps.newHashMap();
                for (PsiParameter parameter : method.getParameterList().getParameters())
                {
                    if (parameter.getType() instanceof PsiPrimitiveType)
                    {
                        parameterStrategies.put(parameter, ParameterCreationStrategy.PRIMITIVE_VALUE);
                    }
                    else
                    {
                        parameterStrategies.put(parameter, ParameterCreationStrategy.MOCK);
                    }
                }
            }
        }

        return builder.build();
    }

    private ImmutableMap<PsiParameter, ComboBox<ParameterCreationStrategy>> getParameterStrategies(PsiMethod method)
    {
        ImmutableMap.Builder<PsiParameter, ComboBox<ParameterCreationStrategy>> parameterStrategies = ImmutableMap.builder();
        for (PsiParameter parameter : method.getParameterList().getParameters())
        {
            ComboBox<ParameterCreationStrategy> comboBox;
            if (parameter.getType() instanceof PsiPrimitiveType)
            {
                comboBox = new ComboBox<>(new ParameterCreationStrategy[]{
                    ParameterCreationStrategy.PRIMITIVE_VALUE
                });
            }
            else
            {
                comboBox = new ComboBox<>(new ParameterCreationStrategy[]{
                    ParameterCreationStrategy.MOCK,
                    ParameterCreationStrategy.NULL
                });
            }
            comboBox.setEnabled(false);
            parameterStrategies.put(parameter, comboBox);
        }

        return parameterStrategies.build();
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
