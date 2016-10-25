package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 * Created by rtoth on 10/23/2016.
 */
public class BoilerplateJUnitGenerator
{
    private final Project project;

    private final PsiFile file;

    private final PsiElementFactory psiElementFactory;

    public BoilerplateJUnitGenerator(@NotNull Project project, @NotNull PsiFile file)
    {
        this.project = Preconditions.checkNotNull(project, "project cannot be null.");
        this.file = Preconditions.checkNotNull(file, "file cannot be null.");
        this.psiElementFactory =  JavaPsiFacade.getElementFactory(project);
    }

    public void createTestCases() throws TestGenerationException
    {
        if (file != null && file instanceof PsiJavaFile)
        {
            PsiJavaFile sourceFile = (PsiJavaFile) file;
            PsiDirectory testDirectory = findOrCreateTestDirectory(sourceFile);
            if (testDirectory != null)
            {
                PsiClass[] classes = sourceFile.getClasses();
                if (classes.length != 1)
                {
                    throw new TestGenerationException("File must contain only 1 class!");
                }
                else
                {
                    PsiClass sourceClass = classes[0];
                    PsiClass testClass = findOrCreateTestClass(testDirectory, sourceClass);
                    if (testClass != null)
                    {
                        GetTestMethodsDialog dialog = new GetTestMethodsDialog(project, sourceClass);
                        if (dialog.showAndGet())
                        {
                            ImmutableList<PsiMethod> testMethods = dialog.getSelectedMethods();
                            PsiJavaFile testFile = (PsiJavaFile) testClass.getContainingFile();
                            addElements(testFile, getImports(), testFile.getPackageStatement());
                            addElements(testClass, generateTestCases(testMethods), null);
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
        else
        {
            throw new TestGenerationException("non-java files are not supported.");
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

    /**
     * TODO: Make this more robust -- tolerate src/java/com/... and src/com/..
     */
    private PsiDirectory findOrCreateTestDirectory(@NotNull PsiJavaFile sourceFile)
    {
        Preconditions.checkNotNull(sourceFile, "sourceFile cannot be null.");

        Stack<String> directoriesInOrder = new Stack<>();
        directoriesInOrder.add("src");
        directoriesInOrder.add("main");
        directoriesInOrder.add("java");
        String packageName = sourceFile.getPackageName();
        final List<String> packageNameDirectories = Arrays.asList(packageName.split("\\."));
        directoriesInOrder.addAll(packageNameDirectories);

        PsiDirectory sourceRoot = null;
        PsiDirectory directory = sourceFile.getContainingDirectory();
        if (directory != null)
        {
            while (directory != null)
            {
                try
                {
                    String expected = directoriesInOrder.pop();
                    if (!directory.getName().equals(expected))
                    {
                        break;
                    }
                    else if (directoriesInOrder.isEmpty())
                    {
                        sourceRoot = directory;
                        break;
                    }
                    directory = directory.getParentDirectory();
                }
                catch (EmptyStackException ese)
                {
                    break;
                }
            }
        }

        PsiDirectory result = null;
        if (sourceRoot != null)
        {
            final PsiDirectory root = sourceRoot;
            result = ApplicationManager.getApplication().runWriteAction(
                (Computable<PsiDirectory>) () ->
                {
                    PsiDirectory testDirectory = root;

                    List<String> testDirectoryTree = Lists.newArrayList("test", "java");
                    testDirectoryTree.addAll(packageNameDirectories);
                    for (String subDirectoryName : testDirectoryTree)
                    {
                        PsiDirectory subDirectory = testDirectory.findSubdirectory(subDirectoryName);
                        if (subDirectory != null)
                        {
                            testDirectory = subDirectory;
                        }
                        else
                        {
                            testDirectory = testDirectory.createSubdirectory(subDirectoryName);
                        }
                    }

                    return testDirectory;
                }
            );
        }
        return result;
    }

    /**
     * All methods are assumed to be non-private constructors that have at least 1 parameter
     */
    private List<PsiElement> generateTestCases(@NotNull ImmutableList<PsiMethod> methodsToTest)
        throws TestGenerationException
    {
        Preconditions.checkNotNull(methodsToTest, "methodsToTest cannot be null.");

        List<PsiElement> testCases = Lists.newArrayList();

        for (PsiMethod method : methodsToTest)
        {
            // TODO: Update this to handle non-constructor methods
            String methodName = "constructor";
            PsiParameterList parameterList = method.getParameterList();

            for (PsiParameter parameterToTest : parameterList.getParameters())
            {
                if (parameterToTest.getType() instanceof PsiPrimitiveType)
                {
                    PsiMethod testCase = psiElementFactory.createMethod(
                        methodName + "_" + parameterToTest.getName() + "_primitive",
                        PsiType.VOID
                    );

                    // TODO: Iterate through primitive types to determine bounds checking and whatnot

                    testCases.add(testCase);
                }
                else
                {
                    String name = parameterToTest.getName();
                    if (name.length() >= 1)
                    {
                        name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
                    }

                    PsiMethod testCase = psiElementFactory.createMethod(
                        methodName + "_null" + name + "_throwsNullPointerException",
                        PsiType.VOID
                    );

                    PsiCodeBlock body = testCase.getBody();
                    if (body != null)
                    {
                        // TODO: Do something else with final classes
                        List<PsiElement> declaredVariables = Lists.newArrayList();
                        for (PsiParameter parameter : parameterList.getParameters())
                        {
                            if (parameter.equals(parameterToTest))
                            {
                                PsiDeclarationStatement declarationStatement =
                                    psiElementFactory.createVariableDeclarationStatement(parameter.getName(),
                                        parameter.getType(),
                                        psiElementFactory.createExpressionFromText(
                                            "null", null));
                                declaredVariables.addAll(Arrays.asList(declarationStatement.getDeclaredElements()));
                                body.add(declarationStatement);
                            }
                            else
                            {
                                PsiDeclarationStatement declarationStatement =
                                    psiElementFactory.createVariableDeclarationStatement(parameter.getName(),
                                        parameter.getType(),
                                        psiElementFactory.createExpressionFromText(
                                            "mock(" + parameter.getType().getPresentableText() + ".class)", null));
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
                    }
                    else
                    {
                        throw new TestGenerationException("Unable to retrieve body for " + testCase.getName());
                    }

                    // TODO: Add contents here.

                    testCases.add(testCase);
                }
            }
        }
        return testCases;
    }

    /**
     * TODO: Only add imports that aren't already present
     */
    private List<PsiElement> getImports() throws TestGenerationException
    {
        PsiClass mockito = JavaPsiFacade.getInstance(project)
            .findClass("org.mockito.Mockito", GlobalSearchScope.allScope(project));
        if (mockito != null)
        {
            return Lists.newArrayList(
                psiElementFactory.createImportStaticStatement(mockito, "mock")
            );
        }
        else
        {
            throw new TestGenerationException("Please add mockito to the project's classpath before proceeding.");
        }
    }

    private void addElements(@NotNull PsiElement root, @NotNull List<PsiElement> toAdd, @NotNull PsiElement after)
    {
        WriteCommandAction.runWriteCommandAction(
            project,
            () -> {
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
