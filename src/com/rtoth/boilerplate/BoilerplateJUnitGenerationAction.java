package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.swing.Icon;

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
//        VirtualFile vFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
//
//        PsiManager manager = PsiManager.getInstance(project);
//        PsiFile file = manager.findFile(vFile);
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
