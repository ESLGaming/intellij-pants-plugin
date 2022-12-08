// Copyright 2015 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.components.impl;

import com.intellij.ProjectTopics;
import com.intellij.codeInspection.magicConstant.MagicConstantInspection;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.ExternalSystemManager;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.*;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.twitter.intellij.pants.PantsBundle;
import com.twitter.intellij.pants.execution.PantsMakeBeforeRun;
import com.twitter.intellij.pants.file.FileChangeTracker;
import com.twitter.intellij.pants.metrics.PantsExternalMetricsListenerManager;
import com.twitter.intellij.pants.metrics.PantsMetrics;
import com.twitter.intellij.pants.model.PantsOptions;
import com.twitter.intellij.pants.service.project.PantsResolver;
import com.twitter.intellij.pants.settings.PantsProjectSettings;
import com.twitter.intellij.pants.settings.PantsSettings;
import com.twitter.intellij.pants.util.PantsConstants;
import com.twitter.intellij.pants.util.PantsSdkUtil;
import com.twitter.intellij.pants.util.PantsUtil;
import icons.PantsIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PantsProjectComponentImpl implements ProjectManagerListener {

  @Override
  public void projectClosed(@NotNull Project project) {
    PantsMetrics.report();
    FileChangeTracker.unregisterProject(project);
  }

  @Override
  public void projectOpened(@NotNull Project project) {
    if (PantsUtil.isPantsProject(project)) {
      // projectOpened() is called on the dispatch thread, while
      // addPantsProjectIgnoreDirs() calls an external process,
      // so it cannot be run on the dispatch thread.
      ApplicationManager.getApplication().executeOnPooledThread(() -> addPantsProjectIgnoredDirs(project));
    }

    if (project.isDefault()) {
      return;
    }

    StartupManager.getInstance(project).runAfterOpened(
      new Runnable() {
        @Override
        public void run() {
          FastpassUpdater.initialize(project);
          if (PantsUtil.isSeedPantsProject(project)) {
            convertToPantsProject();
            // projectOpened() is called on the dispatch thread, while
            // addPantsProjectIgnoreDirs() calls an external process,
            // so it cannot be run on the dispatch thread.
            ApplicationManager.getApplication().executeOnPooledThread(() -> addPantsProjectIgnoredDirs(project));
          }

          subscribeToRunConfigurationAddition();
          registerVfsListener(project);
          final AbstractExternalSystemSettings pantsSettings = ExternalSystemApiUtil.getSettings(project, PantsConstants.SYSTEM_ID);
          final boolean resolverVersionMismatch =
            pantsSettings instanceof PantsSettings && ((PantsSettings) pantsSettings).getResolverVersion() != PantsResolver.VERSION;
          if (resolverVersionMismatch && PantsUtil.isPantsProject(project)) {
              final int answer = Messages.showYesNoDialog(
                project,
              PantsBundle.message("pants.project.generated.with.old.version", project.getName()),
              PantsBundle.message("pants.name"),
              PantsIcons.Icon
            );
            if (answer == Messages.YES) {
              PantsUtil.refreshAllProjects(project);
            }
          }
        }

        /**
         * To convert a seed Pants project to a full bloom pants project:
         * 1. Obtain the targets and project_path generated by `pants idea-plugin` from
         * workspace file `project.iws` via `PropertiesComponent` API.
         * 2. Generate a refresh spec based on the info above.
         * 3. Explicitly call {@link PantsUtil#refreshAllProjects}.
         */
        private void convertToPantsProject() {
          PantsExternalMetricsListenerManager.getInstance().logIsGUIImport(false);
          final String serializedTargets = PropertiesComponent.getInstance(project).getValue("targets");
          final String projectPath = PropertiesComponent.getInstance(project).getValue("project_path");
          if (serializedTargets == null || projectPath == null) {
            return;
          }

          final boolean enableExportDepAsJar =
            Boolean.parseBoolean(Optional.ofNullable(PropertiesComponent.getInstance(project).getValue("dep_as_jar")).orElse("false"));

          /*
           * Generate the import spec for the next refresh.
           */
          final List<String> targetSpecs = PantsUtil.gson.fromJson(serializedTargets, PantsUtil.TYPE_LIST_STRING);
          final boolean loadLibsAndSources = true;
          final PantsProjectSettings pantsProjectSettings =
            new PantsProjectSettings(targetSpecs, targetSpecs, projectPath, loadLibsAndSources, enableExportDepAsJar);

          /*
           * Following procedures in {@link com.intellij.openapi.externalSystem.util.ExternalSystemUtil#refreshProjects}:
           * Make sure the setting is injected into the project for refresh.
           */
          ExternalSystemManager<?, ?, ?, ?, ?> manager = ExternalSystemApiUtil.getManager(PantsConstants.SYSTEM_ID);
          if (manager == null) {
            return;
          }
          AbstractExternalSystemSettings settings = manager.getSettingsProvider().fun(project);
          settings.setLinkedProjectsSettings(Collections.singleton(pantsProjectSettings));
          PantsUtil.refreshAllProjects(project);

          prepareGuiComponents();

          project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
              applyProjectSdk(project);
            }
          });
        }

        /**
         * Ensure GUI is set correctly because empty IntelliJ project (seed project in this case)
         * does not have these set by default.
         * 1. Make sure the project view is opened so view switch will follow.
         * 2. Pants tool window is initialized; otherwise no message can be shown when invoking `PantsCompile`.
         */
        private void prepareGuiComponents() {
          if (!ApplicationManager.getApplication().isUnitTestMode()) {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Project");
            if (toolWindow != null) {
              toolWindow.show(null);
            }
            ExternalSystemUtil.ensureToolWindowContentInitialized(project, PantsConstants.SYSTEM_ID);
          }
        }


        /**
         * VSF tracker implementation requires PantsOptions object, that cannot be constructed in
         * the Event Dispatch Thread. What is more, it can't be constructed if the project contains
         * no modules, and cannot be registered twice. Hence, there are two cases when VFS tracker is
         * needed:
         * 1. When the Pants project is opened and it already contains modules - then we can register tracker immediately
         * 2. When the Pants project is opened, but modules are not loaded yet - then we have to wait until a module is loaded
         */
        private void registerVfsListener(Project project) {
          if (ModuleManager.getInstance(project).getModules().length > 0) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
              Optional<PantsOptions> pantsOptions = PantsOptions.getPantsOptions(project);
              pantsOptions.ifPresent(po -> FileChangeTracker.registerProject(project, po));
            });
          }
          else {
            project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
              boolean done = false;

              @Override
              public void moduleAdded(@NotNull Project p, @NotNull Module m) {
                PantsUtil
                  .findPantsExecutable(project)
                  .ifPresent(pantsExecutable ->
                             {
                               if (!done) {
                                 ApplicationManager.getApplication().executeOnPooledThread(() -> {
                                   PantsOptions pantsOptions = PantsOptions.getPantsOptions(pantsExecutable.getPath());
                                   FileChangeTracker.registerProject(project, pantsOptions);
                                 });
                                 done = true;
                               }
                             }
                  );
              }
            });
          }
        }

        private void subscribeToRunConfigurationAddition() {
          project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            @Override
            public void runConfigurationAdded(@NotNull RunnerAndConfigurationSettings settings) {
              if (!PantsUtil.isPantsProject(project) && !PantsUtil.isSeedPantsProject(project)) {
                return;
              }
              PantsMakeBeforeRun.replaceDefaultMakeWithPantsMake(settings.getConfiguration());
              PantsMakeBeforeRun.setRunConfigurationWorkingDirectory(settings.getConfiguration());
            }
          });
        }
      }
    );
  }

  /**
   * This will add buildroot/.idea, buildroot/.pants.d to Version Control -> Ignored Files.
   * put project file in a temp dir unrelated to where the repo resides.
   * TODO: make sure it reflects on GUI immediately without a project reload.
   */
  private void addPantsProjectIgnoredDirs(Project project) {
    PantsUtil.findBuildRoot(project).ifPresent(
      buildRoot -> {
        ChangeListManagerImpl clm = ChangeListManagerImpl.getInstanceImpl(project);

        String pathToIgnore = buildRoot.getPath() + File.separator + ".idea";
        clm.addDirectoryToIgnoreImplicitly(pathToIgnore);

        PantsOptions.getPantsOptions(project)
          .flatMap(optionObj -> optionObj.get(PantsConstants.PANTS_OPTION_PANTS_WORKDIR))
          .ifPresent(clm::addDirectoryToIgnoreImplicitly);
      }
    );
  }

  private void applyProjectSdk(Project project) {
    Optional<VirtualFile> pantsExecutable = PantsUtil.findPantsExecutable(project);
    if (pantsExecutable.isEmpty()) {
      return;
    }

    Optional<Sdk> sdk = PantsSdkUtil.getDefaultJavaSdk(pantsExecutable.get().getPath(), project);
    if (sdk.isEmpty()) {
      return;
    }

    ApplicationManager.getApplication().runWriteAction(() -> {
      JavaSdkUtil.applyJdkToProject(project, sdk.get());
    });

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      DumbService.getInstance(project).smartInvokeLater(() -> {
        Runnable fix = MagicConstantInspection.getAttachAnnotationsJarFix(project);
        Optional.ofNullable(fix).ifPresent(Runnable::run);
      });
    });
  }
}
