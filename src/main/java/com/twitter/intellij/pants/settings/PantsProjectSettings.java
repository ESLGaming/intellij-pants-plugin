// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.settings;

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings;
import com.twitter.intellij.pants.model.PantsCompileOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PantsProjectSettings extends ExternalProjectSettings implements PantsCompileOptions {
  private String projectName;
  private List<String> mySelectedTargetSpecs = new ArrayList<>();
  private List<String> myAllAvailableTargetSpecs = new ArrayList<>();
  public boolean libsWithSources = true;
  public boolean importSourceDepsAsJars;


  /**
   * @param allAvailableTargetSpecs   targets explicted listed from `pants idea-plugin` goal.
   * @param selectedTargetSpecs       targets selected by the user to import
   * @param externalProjectPath       path to the Pants project.
   * @param libsWithSources           whether to import sources and docs when resolving for jars.
   * @param isImportSourceDepsAsJars  whether to import source dependencies as frozen jars.
   */
  public PantsProjectSettings(
    List<String> allAvailableTargetSpecs,
    List<String> selectedTargetSpecs,
    String externalProjectPath,
    boolean libsWithSources,
    boolean isImportSourceDepsAsJars
  ) {
    setExternalProjectPath(externalProjectPath);
    mySelectedTargetSpecs = selectedTargetSpecs;
    myAllAvailableTargetSpecs = allAvailableTargetSpecs;
    this.libsWithSources = libsWithSources;
    importSourceDepsAsJars = isImportSourceDepsAsJars;
  }

  public PantsProjectSettings() {
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }
    PantsProjectSettings other = (PantsProjectSettings) obj;
    return Objects.equals(projectName, other.projectName)
           && Objects.equals(libsWithSources, other.libsWithSources)
           && Objects.equals(myAllAvailableTargetSpecs, other.myAllAvailableTargetSpecs)
           && Objects.equals(mySelectedTargetSpecs, other.mySelectedTargetSpecs)
           && Objects.equals(importSourceDepsAsJars, other.importSourceDepsAsJars);
  }

  @NotNull
  @Override
  public PantsProjectSettings clone() {
    final PantsProjectSettings pantsProjectSettings = new PantsProjectSettings();
    copyTo(pantsProjectSettings);
    return pantsProjectSettings;
  }

  @Override
  protected void copyTo(@NotNull ExternalProjectSettings receiver) {
    super.copyTo(receiver);
    if (receiver instanceof PantsProjectSettings) {
      ((PantsProjectSettings) receiver).setProjectName(getProjectName());
      ((PantsProjectSettings) receiver).setSelectedTargetSpecs(getSelectedTargetSpecs());
      ((PantsProjectSettings) receiver).setAllAvailableTargetSpecs(getAllAvailableTargetSpecs());
      ((PantsProjectSettings) receiver).libsWithSources = libsWithSources;
      ((PantsProjectSettings) receiver).importSourceDepsAsJars = importSourceDepsAsJars;
    }
  }



  public List<String> getAllAvailableTargetSpecs() {
    return myAllAvailableTargetSpecs;
  }

  public void setAllAvailableTargetSpecs(List<String> allAvailableTargetSpecs) {
    this.myAllAvailableTargetSpecs = allAvailableTargetSpecs;
  }

  /**
   * Get the target specs used to launched `pants idea-plugin`.
   */
  @NotNull
  public List<String> getSelectedTargetSpecs() {
    return mySelectedTargetSpecs;
  }

  public void setSelectedTargetSpecs(List<String> selectedTargetSpecs) {
    mySelectedTargetSpecs = selectedTargetSpecs;
  }

  @Override
  public boolean isImportSourceDepsAsJars() {
    return this.importSourceDepsAsJars;
  }

  void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  @Nullable
  public String getProjectName() {
    return projectName;
  }
}
