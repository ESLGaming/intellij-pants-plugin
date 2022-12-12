// Copyright 2020 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.service.project;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.ide.util.newProjectWizard.AddModuleWizard;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.twitter.intellij.pants.service.project.wizard.PantsProjectImportProvider;
import com.twitter.intellij.pants.util.PantsConstants;
import com.twitter.intellij.pants.util.PantsUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class PantsOpenProjectProvider extends AbstractOpenProjectProvider {

  private static AbstractOpenProjectProvider instance = new PantsOpenProjectProvider();

  static AbstractOpenProjectProvider getInstance() { return instance; }

  @NotNull
  @Override
  public ProjectSystemId getSystemId() {
    return PantsConstants.SYSTEM_ID;
  }

  @Override
  protected boolean isProjectFile(@NotNull VirtualFile virtualFile) {
    if (PantsUtil.findBuildRoot(virtualFile).isEmpty()) return false;
    return virtualFile.isDirectory() || PantsUtil.isBUILDFileName(virtualFile.getName());
  }

  @Override
  public void linkToExistingProject(@NotNull VirtualFile projectFile, @NotNull Project project) {
    var dialog = openNewProjectWizard(projectFile);
    link(projectFile, project, dialog);
    refresh(projectFile, project);
  }

  private void link(@NotNull VirtualFile projectFile, @NotNull Project project, AddModuleWizard dialog) {
    if (dialog == null) return;

    ProjectBuilder builder = dialog.getBuilder(project);
    if (builder == null) return;

    try {
      ApplicationManager.getApplication().runWriteAction(() -> {
        Optional.ofNullable(dialog.getNewProjectJdk())
          .ifPresent(jdk -> JavaSdkUtil.applyJdkToProject(project, jdk));

        var projectDir = projectFile.isDirectory() ? Paths.get(projectFile.getPath()) : Paths.get(projectFile.getParent().getPath());
        URI output = projectDir.resolve(".out").toUri();
        Optional.ofNullable(CompilerProjectExtension.getInstance(project))
          .ifPresent(ext -> ext.setCompilerOutputUrl(output.toString()));
      });

      builder.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
      project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, Boolean.TRUE);

      project.save();
    }
    finally {
      builder.cleanup();
    }
  }

  private void refresh(VirtualFile file, Project project) {
    ExternalSystemUtil.refreshProject(
      file.getPath(),
      new ImportSpecBuilder(project, PantsConstants.SYSTEM_ID).usePreviewMode().use(ProgressExecutionMode.MODAL_SYNC)
    );
    ExternalSystemUtil.refreshProject(
      file.getPath(),
      new ImportSpecBuilder(project, PantsConstants.SYSTEM_ID)
    );
  }

  private AddModuleWizard openNewProjectWizard(VirtualFile projectFile) {
    PantsProjectImportProvider provider = new PantsProjectImportProvider();
    AddModuleWizard dialog = new AddModuleWizard(null, projectFile.getPath(), provider);

    var builder = provider.getBuilder();
    builder.setUpdate(false);
    dialog.getWizardContext().setProjectBuilder(builder);

    // dialog can only be shown in a non-headless environment
    Application application = ApplicationManager.getApplication();
    if (application.isHeadlessEnvironment() || dialog.showAndGet()) {
      return dialog;
    }
    else {
      return null;
    }
  }
}
