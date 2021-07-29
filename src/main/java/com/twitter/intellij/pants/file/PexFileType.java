package com.twitter.intellij.pants.file;

import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class PexFileType implements FileType {

  @Override
  @NotNull
  public String getName() {
    return "PEX";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "PEX file";
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return "pex";
  }

  @Override
  @Nullable
  public Icon getIcon() {
    return FileTypes.Archive;
  }

  @Override
  public boolean isBinary() {
    return false;
  }
}
