// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.ui;

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.project.Project;
import com.twitter.intellij.pants.settings.PantsSettings;
import com.twitter.intellij.pants.util.PantsConstants;
import org.jetbrains.annotations.NotNull;

public class PantsToolWindowFactory extends AbstractExternalSystemToolWindowFactory {
  public PantsToolWindowFactory() {
    super(PantsConstants.SYSTEM_ID);
  }

  @Override
  protected @NotNull AbstractExternalSystemSettings<?, ?, ?> getSettings(@NotNull Project project) {
    return PantsSettings.getInstance(project);
  }
}
