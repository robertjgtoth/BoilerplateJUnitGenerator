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
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.rtoth.boilerplate.parameters.ParameterInitializer;
import com.rtoth.boilerplate.parameters.ParameterRule;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates JUnit test cases for the provided Java class based on input from the user.
 */
class TestCaseGenerator
{
    /**
     * {@link Project} for which test cases will be generated.
     */
    private final Project project;

    /**
     * Used to create new {@link PsiElement}s (e.g. test methods).
     */
    private final PsiElementFactory psiElementFactory;

    /**
     * Create a new {@link TestCaseGenerator} for the provided {@link Project}.
     *
     * @param project {@link Project} for which test cases will be generated. Cannot be {@code null}.
     *
     * @throws NullPointerException if any parameter is {@code null}.
     */
    TestCaseGenerator(@NotNull Project project)
    {
        this.project = Preconditions.checkNotNull(project, "project cannot be null.");

        this.psiElementFactory = JavaPsiFacade.getElementFactory(project);
    }

    /**
     * Create test cases in the provided {@code testClass} based on the provided {@code methodRules}.
     *
     * @param testClass {@link PsiClass} to which test cases should be added. Cannot be {@code null} and must be
     *                  contained in a valid {@link PsiJavaFile}.
     * @param methodRules Mapping of {@link PsiMethod}s to test in the source class to {@link ParameterRule}s which
     *                    indicate how each of the method's parameters should be tested. Cannot be {@code null}, and
     *                    the list of {@link ParameterRule}s for each method is expected to contain exactly the number
     *                    of parameters defined for the method in the correct order. Providing the parameters out of
     *                    order will result in test code which does not compile.
     *
     * @throws IllegalArgumentException if any method's list of {@link ParameterRule}s does not contain the same number
     *                                  of rules as there are parameters in the method.
     * @throws NullPointerException if any parameter is {@code null}.
     * @throws TestGenerationException If there is a problem creating test cases.
     */
    void createTestCases(@NotNull PsiClass testClass,
                         @NotNull ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> methodRules)
        throws TestGenerationException
    {
        Preconditions.checkNotNull(testClass, "testClass cannot be null.");
        Preconditions.checkNotNull(methodRules, "methodRules cannot be null.");
        for (Map.Entry<PsiMethod, ImmutableList<ParameterRule>> entry : methodRules.entrySet())
        {
            Preconditions.checkArgument(
                entry.getKey().getParameterList().getParametersCount() ==
                entry.getValue().size(), "Invalid map of method rules. At least one method's list of parameter rules " +
                    "does not match the method signature for that method.");
        }

        PsiJavaFile testFile = (PsiJavaFile) testClass.getContainingFile();
        // TODO: These are all getting appended without any newline breaks
        PsiUtility.addElements(testFile, getImports(), testFile.getPackageStatement());
        // TODO: Only define variables once at the class level instead of in each and every method.
        PsiUtility.addElements(testClass, generateTestCases(methodRules), null);
    }

    /**
     * Generate test cases based on the provided {@code methodRules}.
     * <p>
     * Note: This method currently only supports constructors. Providing non-constructor methods here will result in
     *       undefined behavior.
     *
     * TODO: Support non-constructor methods.
     *
     * @param methodRules Mapping of {@link PsiMethod}s to test in the source class to {@link ParameterRule}s which
     *                    indicate how each of the method's parameters should be tested. Cannot be {@code null}, and
     *                    the list of {@link ParameterRule}s for each method is assumed to contain exactly the number
     *                    of parameters defined for the method in the correct order. Providing the parameters out of
     *                    order will result in test code which does not compile.
     * @return A list of {@link PsiMethod}s which define the generated test cases. Never {@code null}, but may be empty.
     *
     * @throws NullPointerException if {@code methodRules} is {@code null}.
     * @throws TestGenerationException If there is a problem creating test cases.
     */
    @NotNull
    private ImmutableList<PsiElement> generateTestCases(
        @NotNull ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> methodRules)
        throws TestGenerationException
    {
        Preconditions.checkNotNull(methodRules, "methodRules cannot be null.");

        ImmutableList.Builder<PsiElement> testCases = ImmutableList.builder();

        for (Map.Entry<PsiMethod, ImmutableList<ParameterRule>> methodEntry : methodRules.entrySet())
        {
            PsiMethod method = methodEntry.getKey();
            ImmutableList<ParameterRule> parameterRules = methodEntry.getValue();

            String methodNameBase = "constructor";

            for (ParameterRule ruleToTest : parameterRules)
            {
                for (Map.Entry<ParameterInitializer, Class<? extends Exception>> invalidInitializerEntry :
                    ruleToTest.getInvalidInitializers().entrySet())
                {
                    ParameterInitializer invalidInitializer = invalidInitializerEntry.getKey();
                    Class<? extends Exception> expectedException = invalidInitializerEntry.getValue();

                    PsiMethod testCase = psiElementFactory.createMethod(
                        methodNameBase + "_" + invalidInitializer.getDescription() +
                            "_throws" + expectedException.getSimpleName(),
                        PsiType.VOID
                    );
                    // TODO: Fix how this annotation is formatted (it's not putting a newline before the start
                    //       of the method)
                    testCase.addBefore(psiElementFactory.createAnnotationFromText(
                        "@Test(expected = " + expectedException.getSimpleName() + ".class)", null),
                        testCase.getModifierList());

                    PsiCodeBlock body = testCase.getBody();
                    if (body != null)
                    {
                        List<PsiElement> declaredVariables = Lists.newArrayList();
                        for (ParameterRule rule : parameterRules)
                        {
                            if (ruleToTest.equals(rule))
                            {
                                // TODO: For parameters which are using null, just pass null directly to the method
                                //       invocation instead of creating a new variable.
                                PsiDeclarationStatement declarationStatement =
                                    psiElementFactory.createVariableDeclarationStatement(ruleToTest.getName(),
                                        ruleToTest.getType(),
                                        psiElementFactory.createExpressionFromText(
                                            invalidInitializer.getInitializerText(), null));
                                declaredVariables.addAll(Arrays.asList(declarationStatement.getDeclaredElements()));
                                body.add(declarationStatement);
                            }
                            else
                            {
                                PsiDeclarationStatement declarationStatement =
                                    psiElementFactory.createVariableDeclarationStatement(rule.getName(),
                                        rule.getType(),
                                        psiElementFactory.createExpressionFromText(
                                            // TODO: Will this always have at least 1 element?
                                            rule.getValidInitializers().get(0).getInitializerText(), null));
                                declaredVariables.addAll(Arrays.asList(declarationStatement.getDeclaredElements()));
                                body.add(declarationStatement);
                            }
                        }

                        StringBuilder invocationBuilder = new StringBuilder("new " + method.getName() + "(");
                        boolean first = true;
                        for (PsiElement element : declaredVariables)
                        {
                            if (element instanceof PsiLocalVariable)
                            {
                                PsiLocalVariable variable = (PsiLocalVariable) element;
                                if (!first)
                                {
                                    invocationBuilder.append(", ");
                                }
                                invocationBuilder.append(variable.getName());
                                first = false;
                            }
                        }
                        invocationBuilder.append(");");
                        body.add(
                            psiElementFactory.createStatementFromText(invocationBuilder.toString(), null)
                        );
                        testCases.add(testCase);
                    }
                    else
                    {
                        throw new TestGenerationException("Unable to retrieve body for " + testCase.getName());
                    }
                }
            }
        }
        return testCases.build();
    }

    /**
     * Get the list of imports needed for our generated test cases.
     * <p>
     * This is currently just a static list containing {@code org.mockito.Mockito.mock} and {@code org.junit.Test}.
     *
     * TODO: Only add imports that aren't already present, and only add the ones that are needed.
     *
     * @return An {@link ImmutableList} containing the imports needed to support testing. Never {@code null}.
     *
     * @throws TestGenerationException if any required module is not added to the project's classpath.
     */
    @NotNull
    private ImmutableList<PsiElement> getImports() throws TestGenerationException
    {
        ImmutableList.Builder<PsiElement> imports = ImmutableList.builder();

        PsiClass mockito = JavaPsiFacade.getInstance(project)
            .findClass("org.mockito.Mockito", GlobalSearchScope.allScope(project));
        if (mockito != null)
        {
            imports.add(
                psiElementFactory.createImportStaticStatement(mockito, "mock")
            );
        }
        else
        {
            throw new TestGenerationException("Please add Mockito to the project's classpath before proceeding.");
        }

        PsiClass test = JavaPsiFacade.getInstance(project)
            .findClass("org.junit.Test", GlobalSearchScope.allScope(project));
        if (test != null)
        {
            imports.add(
                psiElementFactory.createImportStatement(test)
            );
        }
        else
        {
            throw new TestGenerationException("Please add JUnit to the project's classpath before proceeding.");
        }

        return imports.build();
    }
}
