package com.rtoth.boilerplate;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

/**
 * Created by rtoth on 10/22/2016.
 */
public class BoilerplateJUnitGenerationAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent event)
    {
        Project project = event.getData(PlatformDataKeys.PROJECT);

        // TODO: Eventually might want to handle multiple selection:
        //          event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)
        PsiFile file = event.getData(LangDataKeys.PSI_FILE);

        if (file != null)
        {
            BoilerplateJUnitGenerator generator = new BoilerplateJUnitGenerator(project, file);
            try
            {
                generator.createTestCases();
            }
            catch (TestGenerationException tge)
            {
                Messages.showMessageDialog(project, tge.getMessage(),
                    "Warning", Messages.getWarningIcon());
            }
        }
        // TODO: When would this ever be null?
    }
}
