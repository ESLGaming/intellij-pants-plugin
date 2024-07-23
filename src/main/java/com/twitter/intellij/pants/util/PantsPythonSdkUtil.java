// Copyright 2020 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.util;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.python.community.plugin.java.facet.JavaPythonFacet;
import org.jetbrains.annotations.NotNull;

public final class PantsPythonSdkUtil {
  public static boolean hasNoPythonSdk(@NotNull Module module) {
    JavaPythonFacet facet = FacetManager.getInstance(module).getFacetByType(JavaPythonFacet.ID);
    return facet == null || facet.getConfiguration().getSdk() == null;
  }
}
