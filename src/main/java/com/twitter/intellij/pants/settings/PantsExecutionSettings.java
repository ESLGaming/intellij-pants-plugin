// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.settings;

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings;

import com.twitter.intellij.pants.model.PantsExecutionOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PantsExecutionSettings extends ExternalSystemExecutionSettings implements PantsExecutionOptions {
  private final String myName;
  private final boolean myLibsWithSourcesAndDocs;
  private final boolean myImportSourceDepsAsJars;
  private final List<String> myTargetSpecs;

  private static final String DEFAULT_PROJECT_NAME = null;
  private static final List<String> DEFAULT_TARGET_SPECS = Collections.emptyList();
  private static final boolean DEFAULT_WITH_SOURCES_AND_DOCS = true;
  private static final boolean DEFAULT_IMPORT_SOURCE_DEPS_AS_JARS = false;

  public static PantsExecutionSettings createDefault() {
    return new PantsExecutionSettings(
      DEFAULT_PROJECT_NAME,
      DEFAULT_TARGET_SPECS,
      DEFAULT_WITH_SOURCES_AND_DOCS,
      DEFAULT_IMPORT_SOURCE_DEPS_AS_JARS
    );
  }

  public PantsExecutionSettings(
    String name,
    List<String> targetSpecs,
    boolean libsWithSourcesAndDocs,
    boolean importSourceDepsAsJars
  ){
    myName = name;
    myTargetSpecs = targetSpecs;
    myLibsWithSourcesAndDocs = libsWithSourcesAndDocs;
    myImportSourceDepsAsJars = importSourceDepsAsJars;
  }

  /**
   * @param targetSpecs             targets explicitly listed from `pants idea-plugin` goal.
   * @param libsWithSourcesAndDocs  whether to import sources and docs when resolving for jars.
   */
  public PantsExecutionSettings(
    List<String> targetSpecs,
    boolean libsWithSourcesAndDocs,
    boolean importSourceDepsAsJars
  ) {
    this(DEFAULT_PROJECT_NAME, targetSpecs, libsWithSourcesAndDocs, importSourceDepsAsJars);
  }

  public Optional<String> getProjectName(){
    return Optional.ofNullable(myName)
      .filter(name -> !name.isEmpty());
  }

  @NotNull
  public List<String> getSelectedTargetSpecs() {
    return myTargetSpecs;
  }

  public boolean isLibsWithSourcesAndDocs() {
    return myLibsWithSourcesAndDocs;
  }

  @Override
  public boolean isImportSourceDepsAsJars() {
    return myImportSourceDepsAsJars;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    PantsExecutionSettings settings = (PantsExecutionSettings) o;
    return Objects.equals(myLibsWithSourcesAndDocs, settings.myLibsWithSourcesAndDocs) &&
           Objects.equals(myTargetSpecs, settings.myTargetSpecs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myTargetSpecs, myLibsWithSourcesAndDocs);
  }
}
