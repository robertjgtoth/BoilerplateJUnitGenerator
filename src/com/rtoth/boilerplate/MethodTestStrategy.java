package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.tree.IElementType;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by rtoth on 10/23/2016.
 */
public class MethodTestStrategy
{
    private final PsiMethod method;

    private final Map<PsiParameter, ParameterCreationStrategy> parameterStragies;

    public MethodTestStrategy(@NotNull PsiMethod method,
                              @NotNull Map<PsiParameter, ParameterCreationStrategy> parameterStrategies)
    {
        this.method = Preconditions.checkNotNull(method, "method cannot be null.");
        this.parameterStragies = Preconditions.checkNotNull(parameterStrategies, "parameterStrategies cannot be null.");
    }
}
