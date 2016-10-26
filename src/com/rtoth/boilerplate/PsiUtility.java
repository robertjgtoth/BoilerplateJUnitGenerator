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
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * Contains common utilities to interact with {@link com.intellij.psi.PsiElement}s.
 */
public final class PsiUtility
{
    /**
     * Private constructor for utility class.
     */
    private PsiUtility()
    {
        // Nothing to see here.
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
     * be a source file.
     * @return An {@link Optional} containing the {@link PsiDirectory} pointing to the test directory associated with
     * {@code sourceFile}; {@link Optional#empty()} if {@code sourceFile} is not a source file in a known
     * directory structure, or there was a problem creating the test directory.
     *
     * @throws NullPointerException if {@code sourceFile} is {@code null}.
     */
    public static Optional<PsiDirectory> findOrCreateTestDirectory(@NotNull PsiJavaFile sourceFile)
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
}
