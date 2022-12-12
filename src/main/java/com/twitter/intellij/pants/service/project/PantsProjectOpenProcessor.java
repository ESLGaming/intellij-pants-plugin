// Copyright 2020 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.service.project;

import com.intellij.ide.impl.ProjectUtilKt;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectOpenProcessor;
import com.twitter.intellij.pants.PantsBundle;
import icons.PantsIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

final class PantsProjectOpenProcessor extends ProjectOpenProcessor {

    @Override
    public boolean canOpenProject(@NotNull VirtualFile virtualFile) {
        return PantsOpenProjectProvider.getInstance().canOpenProject(virtualFile);
    }

    @Nullable
    @Override
    public Project doOpenProject(@NotNull VirtualFile virtualFile, @Nullable Project projectToClose, boolean forceOpenInNewFrame) {
        return ProjectUtilKt.runUnderModalProgressIfIsEdt((bla, continuation) -> PantsOpenProjectProvider.getInstance().openProject(virtualFile, projectToClose, forceOpenInNewFrame, continuation));
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return PantsBundle.message("pants.name");
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return PantsIcons.Icon;
    }
}
