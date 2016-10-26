/**
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
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiDirectory;
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
 * Generates JUnit test cases for the provided Java file based on input from the user.
 */
public class TestCaseGenerator
{
    /**
     * {@link Project} for which test cases will be generated.
     */
    private final Project project;

    /**
     * Source {@link PsiJavaFile} for which test cases will be generated.
     */
    private final PsiJavaFile sourceFile;

    /**
     * Used to create new {@link PsiElement}s (e.g. test methods).
     */
    private final PsiElementFactory psiElementFactory;

    /**
     * Create a new {@link TestCaseGenerator} for the provided {@link Project} and {@link PsiFile}.
     *
     * @param project {@link Project} for which test cases will be generated. Cannot be {@code null}.
     * @param sourceFile {@link PsiJavaFile} for which to generated test cases. Cannot be {@code null}.
     * @throws NullPointerException if any parameter is {@code null}.
     */
    public TestCaseGenerator(@NotNull Project project, @NotNull PsiJavaFile sourceFile)
    {
        this.project = Preconditions.checkNotNull(project, "project cannot be null.");
        this.sourceFile = Preconditions.checkNotNull(sourceFile, "sourceFile cannot be null.");

        this.psiElementFactory = JavaPsiFacade.getElementFactory(project);
    }

    /**
     * FIXME: docs
     */
    public void createTestCases() throws TestGenerationException
    {
        Optional<PsiDirectory> testDirectory = PsiUtility.findOrCreateTestDirectory(sourceFile);
        if (testDirectory.isPresent())
        {
            PsiClass[] classes = sourceFile.getClasses();
            if (classes.length != 1)
            {
                throw new TestGenerationException("File must contain only 1 class!");
            }
            else
            {
                // TODO: If we have to create the directory or file, it just exits without displaying the dialog...
                PsiClass sourceClass = classes[0];
                PsiClass testClass = findOrCreateTestClass(testDirectory.get(), sourceClass);
                if (testClass != null)
                {
                    GetTestMethodsDialog dialog = new GetTestMethodsDialog(project, sourceClass);
                    if (dialog.showAndGet())
                    {
                        PsiJavaFile testFile = (PsiJavaFile) testClass.getContainingFile();
                        // TODO: These are all getting appending without any newline breaks
                        addElements(testFile, getImports(), testFile.getPackageStatement());
                        ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> methodRules =
                            dialog.getSelectedMethodRules();
                        addElements(testClass, generateTestCases(methodRules), null);
                    }
                }
                else
                {
                    throw new TestGenerationException("Error generating test class for " + sourceClass.getName());
                }
            }
        }
        else
        {
            throw new TestGenerationException("Can only generate test cases for source files.");
        }
    }

    private PsiClass findOrCreateTestClass(final @NotNull PsiDirectory testDirectory, @NotNull PsiClass sourceClass)
        throws TestGenerationException
    {
        Preconditions.checkNotNull(testDirectory, "testDirectory cannot be null.");
        Preconditions.checkNotNull(sourceClass, "sourceClass cannot be null.");

        final String testClassName = sourceClass.getName() + "Test";
        PsiFile testFile = testDirectory.findFile(testClassName + ".java");
        PsiClass testClass = null;
        if (testFile != null)
        {
            if (testFile instanceof PsiJavaFile)
            {
                PsiClass[] classes = ((PsiJavaFile) testFile).getClasses();
                if (classes.length != 1)
                {
                    throw new TestGenerationException(testFile.getName() + " exists but contains more than 1 class.");
                }
                else
                {
                    testClass = classes[0];
                }
            }
            else
            {
                throw new TestGenerationException(testFile.getName() + " exists but is not a valid java file.");
            }
        }
        else
        {
            testClass = WriteCommandAction.runWriteCommandAction(
                project,
                (Computable<PsiClass>) () ->
                {
                    PsiClass newClass = psiElementFactory.createClass(testClassName);
                    testDirectory.add(newClass);
                    return newClass;
                }
            );
        }

        return testClass;
    }

    private List<PsiElement> generateTestCases(
        @NotNull ImmutableMap<PsiMethod, ImmutableList<ParameterRule>> methodRules)
        throws TestGenerationException
    {
        Preconditions.checkNotNull(methodRules, "methodRules cannot be null.");

        List<PsiElement> testCases = Lists.newArrayList();

        for (Map.Entry<PsiMethod, ImmutableList<ParameterRule>> methodEntry : methodRules.entrySet())
        {
            PsiMethod method = methodEntry.getKey();
            ImmutableList<ParameterRule> parameterRules = methodEntry.getValue();

            // TODO: Update this to handle non-constructor methods
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
                    //       of the method
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
        return testCases;
    }

    /**
     * TODO: Only add imports that aren't already present
     */
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

    private void addElements(@NotNull PsiElement root, @NotNull List<PsiElement> toAdd, PsiElement after)
    {
        WriteCommandAction.runWriteCommandAction(
            project,
            () ->
            {
                for (PsiElement element : toAdd)
                {
                    if (after != null)
                    {
                        root.addAfter(element, after);
                    }
                    else
                    {
                        root.add(element);
                    }
                }
            }
        );
    }
}
