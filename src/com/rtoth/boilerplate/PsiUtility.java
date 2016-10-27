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
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Contains common utilities to interact with {@link com.intellij.psi.PsiElement}s.
 */
final class PsiUtility
{
    /**
     * Private constructor for utility class.
     */
    private PsiUtility()
    {
        // Nothing to see here.
    }

    /**
     * Get the single {@link PsiClass} contained in the provided {@link PsiFile}, if possible.
     *
     * @param file {@link PsiFile} for which to get the single {@link PsiClass}. Cannot be {@code null}.
     * @return {@link Optional} containing the single {@link PsiClass} contained in {@code file} or
     *         {@link Optional#empty()} if {@code file} does not contain exactly one class. Never {@code null}.
     *
     * @throws NullPointerException if {@code file} is {@code null}.
     */
    @NotNull
    static Optional<PsiClass> getSingleClass(@NotNull PsiJavaFile file) throws TestGenerationException
    {
        Preconditions.checkNotNull(file, "file cannot be null.");

        PsiClass singleClass = null;

        PsiClass[] classes = file.getClasses();
        if (classes.length == 1)
        {
            singleClass = classes[0];
        }

        return Optional.ofNullable(singleClass);
    }

    /**
     * Find or create the test class associated with the provided source class.
     * <p>
     * As implemented, this only works for directory layouts in the standard maven format:
     * <pre>
     *     src/main/java/com/package/name/File.java
     *     src/test/java/com/package/name/FileTest.java
     * </pre>
     * <p>
     * TODO: Make this more robust -- handle more formats
     *
     * @param sourceClass Source class for which to create or find the test class. Cannot be {@code null} and must be
     *                    defined in a valid {@link PsiJavaFile}.
     * @return {@link Optional} containing the {@link PsiClass} pointing to the test class associated with
     *         {@code sourceClass} or {@link Optional#empty()} if {@code sourceClass} is not a in a known directory
     *         structure, or there was a problem creating the test class. Never {@code null}.
     *
     * @throws IllegalArgumentException if {@code sourceFile} is not defined in a valid {@link PsiJavaFile}.
     * @throws NullPointerException if {@code sourceFile} is {@code null}.
     */
    @NotNull
    static Optional<PsiClass> findOrCreateTestClass(@NotNull PsiClass sourceClass)
    {
        Preconditions.checkNotNull(sourceClass, "sourceClass cannot be null.");
        PsiFile sourceFile = sourceClass.getContainingFile();
        Preconditions.checkArgument(sourceFile != null && sourceFile instanceof PsiJavaFile,
            "sourceClass must be defined in a valid java file.");
        final Project project = sourceClass.getProject();

        PsiClass testClass = null;

        Optional<PsiDirectory> optionalTestDirectory = findOrCreateTestDirectory((PsiJavaFile) sourceFile);
        if (optionalTestDirectory.isPresent())
        {
            final PsiDirectory testDirectory = optionalTestDirectory.get();
            final String testClassName = sourceClass.getName() + "Test";
            PsiFile testFile = testDirectory.findFile(testClassName + ".java");
            if (testFile != null && testFile instanceof PsiJavaFile)
            {
                PsiClass[] classes = ((PsiJavaFile) testFile).getClasses();
                if (classes.length == 1)
                {
                    testClass = classes[0];
                }
            }
            else
            {
                final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
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
        }

        return Optional.ofNullable(testClass);
    }

    /**
     * Find or create the test directory associated with the provided source file.
     * <p>
     * As implemented, this only works for directory layouts in the standard maven format:
     * <pre>
     *     src/main/java/com/package/name/File.java
     *     src/test/java/com/package/name/FileTest.java
     * </pre>
     * <p>
     * TODO: Make this more robust -- handle more formats
     *
     * @param sourceFile Source file for which to locate the associated test directory. Cannot be {@code null} and must
     *                   be a source file.
     * @return {@link Optional} containing the {@link PsiDirectory} pointing to the test directory associated with
     *         {@code sourceFile} or {@link Optional#empty()} if {@code sourceFile} is not a source file in a known
     *         directory structure, or there was a problem creating the test directory. Never {@code null}.
     *
     * @throws NullPointerException if {@code sourceFile} is {@code null}.
     */
    @NotNull
    private static Optional<PsiDirectory> findOrCreateTestDirectory(@NotNull PsiJavaFile sourceFile)
    {
        Preconditions.checkNotNull(sourceFile, "sourceFile cannot be null.");

        Stack<String> directoriesInOrder = new Stack<>();
        directoriesInOrder.add("src");
        directoriesInOrder.add("main");
        directoriesInOrder.add("java");
        String packageName = sourceFile.getPackageName();
        final List<String> packageNameDirectories = Arrays.asList(packageName.split("\\."));
        directoriesInOrder.addAll(packageNameDirectories);

        // TODO: Can this be simplified/cleaned up?

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

        return Optional.ofNullable(result);
    }

    /**
     * Add the provided elements to the provided root element.
     *
     * @param root {@link PsiElement} to which elements should be added. Cannot be {@code null}.
     * @param toAdd {@link PsiElement}s to add to {@code root}. Cannot be {@code null}.
     * @param after Optional {@link PsiElement} after which all elements should be added. Can be {@code null} if none
     *              is desired.
     *
     * @throws IllegalArgumentException if {@code after} is not a child of {@code root}.
     * @throws NullPointerException if {@code root} or {@code toAdd} is {@code null}.
     */
    static void addElements(@NotNull PsiElement root, @NotNull ImmutableList<PsiElement> toAdd, PsiElement after)
    {
        Preconditions.checkNotNull(root, "root cannot be null.");
        Preconditions.checkNotNull(toAdd, "toAdd cannot be null.");
        if (after != null)
        {
            Preconditions.checkArgument(Arrays.asList(root.getChildren()).contains(after),
                "after must be a child of root.");
        }

        final Project project = root.getProject();
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
