package com.rtoth.boilerplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.Ring;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.psi.impl.cache.impl.id.FileTypeIdIndexer;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.tree.java.PsiWhileStatementImpl;

import org.jetbrains.annotations.NotNull;

import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
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
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        VirtualFile vFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);

        PsiManager manager = PsiManager.getInstance(project);
        PsiFile file = manager.findFile(vFile);

        if (file != null && file instanceof PsiJavaFile)
        {
            final PsiJavaFile javaFile = (PsiJavaFile) file;

            int numTestCases = -1;
            while (numTestCases <= 0)
            {
                String numTestCasesStr =
                    Messages.showInputDialog(project, "How many test cases?", "Number of test cases:",
                        Messages.getQuestionIcon());
                try
                {
                    numTestCases = Integer.parseInt(numTestCasesStr);

                    if (numTestCases > 0)
                    {
                        List<PsiElement> testCases = Lists.newArrayList();
                        for (int i = 0; i < numTestCases; i++)
                        {
                            testCases.add(getStubTestCase(project, "test" + (i+1)));
                        }
                        addElements(project, javaFile, testCases);
                    }
                    else
                    {
                        showMessage(project, MessageSeverity.WARN, "Must input a positive value!");
                    }
                }
                catch (NumberFormatException nfe)
                {
                    showMessage(project, MessageSeverity.WARN, "Must input a numeric value!");
                }
            }
        }
        else
        {
            showMessage(project, MessageSeverity.WARN, "Found non java class!");
        }
    }

    private static PsiMethod getStubTestCase(Project project, String name)
    {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

        return factory.createMethod(name, PsiType.VOID);
    }

    private static void addElements(Project project, PsiJavaFile javaFile, List<PsiElement> elements)
    {
        WriteCommandAction.runWriteCommandAction(
            project, () ->
            {
                PsiClass[] classes = javaFile.getClasses();
                if (classes.length != 1)
                {
                    showMessage(project, MessageSeverity.ERROR, "File must contain only 1 class!");
                }
                else
                {
                    PsiElement javaClass = classes[0];
                    for (PsiElement element : elements)
                    {
                        javaClass.add(element);
                    }
                }
            });
    }

    private static void showMessage(Project project, MessageSeverity severity, String msg)
    {
        Messages.showMessageDialog(project, msg, severity.getTitle(), severity.getIcon());
    }

    private enum MessageSeverity
    {
        INFO("Info", Messages.getInformationIcon()),

        WARN("Warning", Messages.getWarningIcon()),

        ERROR("Error", Messages.getErrorIcon());

        private final String title;

        private final Icon icon;

        MessageSeverity(String title, Icon icon)
        {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle()
        {
            return title;
        }

        public Icon getIcon()
        {
            return icon;
        }
    }

}
