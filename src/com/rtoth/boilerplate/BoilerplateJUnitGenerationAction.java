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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

/**
 * Defines an {@link AnAction} which generates "boilerplate" JUnit test cases for
 * the Java file selected by the user based on input provided by the user.
 * <p>
 * This action is invoked by the user of this plugin.
 */
// TODO: Figure out why Ctrl+Z doesn't work
// TODO: Figure out why keyboard shortcut doesn't work
public class BoilerplateJUnitGenerationAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent event)
    {
        Project project = event.getData(PlatformDataKeys.PROJECT);

        // TODO: When would project ever be null?
        if (project != null)
        {
            // TODO: Eventually might want to handle multiple selection:
            //          event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)
            PsiFile file = event.getData(LangDataKeys.PSI_FILE);

            // TODO: When would file ever be null?
            if (file != null && file instanceof PsiJavaFile)
            {
                try
                {
                    TestCaseGenerator generator = new TestCaseGenerator(project, (PsiJavaFile) file);

                    generator.createTestCases();
                }
                catch (TestGenerationException tge)
                {
                    Messages.showMessageDialog(project, tge.getMessage(),
                        "Warning", Messages.getWarningIcon());
                }
            }
        }
    }
}
