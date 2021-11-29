// Copyright 2020 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.components.impl;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.impl.DynamicActionConfigurationCustomizer;
import com.twitter.intellij.pants.compiler.actions.PantsCompileAllTargetsAction;
import com.twitter.intellij.pants.compiler.actions.PantsCompileAllTargetsInModuleAction;
import com.twitter.intellij.pants.compiler.actions.PantsRebuildAction;
import com.twitter.intellij.pants.ui.PantsOverrideAction;
import com.twitter.intellij.pants.util.PantsConstants;
import icons.PantsIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

final class PantsActionConfigurationCustomizer implements DynamicActionConfigurationCustomizer {
  private static final String PANTS_REBUILD_ACTION_NAME = IdeActions.ACTION_COMPILE_PROJECT + "Pants";
  private static final String PANTS_COMPILE_MODULE_ACTION_NAME = IdeActions.ACTION_MAKE_MODULE + "Pants";
  private static final String PANTS_COMPILE_PROJECT_ACTION_NAME = PantsConstants.ACTION_MAKE_PROJECT_ID + "Pants";

  @Override
  public void registerActions(@NotNull ActionManager actionManager) {
    //  Registers the rebuild action to Pants rebuild action.
    //  Registers Make module action to 'Make all targets in module' action.
    //  Disables compile action
    AnAction pantsCompileAllTargetAction = new PantsOverrideAction(
            PantsConstants.ACTION_MAKE_PROJECT_DESCRIPTION,
            new PantsCompileAllTargetsAction(),
            actionManager.getAction(PantsConstants.ACTION_MAKE_PROJECT_ID),
            PantsIcons.Icon
    );

    AnAction pantsMakeModuleAction = new PantsOverrideAction(
            new PantsCompileAllTargetsInModuleAction(Optional.empty()),
            actionManager.getAction(PANTS_COMPILE_MODULE_ACTION_NAME)
    );

    AnAction pantsRebuildAction = new PantsOverrideAction(
            PantsConstants.REBUILD_PROJECT_DESCRIPTION,
            new PantsRebuildAction(),
            actionManager.getAction(PANTS_REBUILD_ACTION_NAME)
    );

    actionManager.registerAction(PANTS_COMPILE_PROJECT_ACTION_NAME, pantsCompileAllTargetAction);
    actionManager.registerAction(PANTS_COMPILE_MODULE_ACTION_NAME, pantsMakeModuleAction);
    actionManager.registerAction(PANTS_REBUILD_ACTION_NAME, pantsRebuildAction);
  }

  @Override
  public void unregisterActions(@NotNull ActionManager actionManager) {
    actionManager.unregisterAction(PANTS_COMPILE_PROJECT_ACTION_NAME);
    actionManager.unregisterAction(PANTS_COMPILE_MODULE_ACTION_NAME);
    actionManager.unregisterAction(PANTS_REBUILD_ACTION_NAME);
  }
}
