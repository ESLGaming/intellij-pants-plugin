package com.twitter.intellij.pants.execution;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.twitter.intellij.pants.model.PantsTargetAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.runner.BaseScalaApplicationConfigurationProducer;
import org.jetbrains.plugins.scala.runner.ScalaApplicationConfigurationProducer;

public class PantsMainRunConfigurationProducer extends BaseScalaApplicationConfigurationProducer<ApplicationConfiguration> {

    public PantsMainRunConfigurationProducer() {
        // same as org.jetbrains.plugins.scala.runner.ScalaApplicationConfigurationProducer
        super(ApplicationConfigurationType.getInstance());
    }

    @Override
    public boolean shouldReplace(@NotNull ConfigurationFromContext self, @NotNull ConfigurationFromContext other) {
        return other.isProducedBy(ScalaApplicationConfigurationProducer.class);
    }

    @Override
    public boolean setupConfigurationFromContext(@NotNull ApplicationConfiguration configuration,
                                                 @NotNull ConfigurationContext context,
                                                 @NotNull Ref<PsiElement> sourceElement) {
        boolean isContext = super.setupConfigurationFromContext(configuration, context, sourceElement);

        Module currentModule = configuration.getConfigurationModule().getModule();
        if (currentModule == null) {
            return isContext;
        }

        PantsTargetAddress currentPath = getModulePath(currentModule);
        if (currentPath == null) {
            return isContext;
        }

        for (Module module: ModuleManager.getInstance(configuration.getProject()).getModules()) {
            PantsTargetAddress modulePath = getModulePath(module);
            if (modulePath == null) {
                continue;
            }

            if (modulePath.getPath().equals(currentPath.getPath()) && modulePath.getTargetName().equals("main")) {
                configuration.setModule(module);
                break;
            }
        }

        return isContext;
    }

    private PantsTargetAddress getModulePath(@NotNull Module module) {
        String path = ExternalSystemModulePropertyManager.getInstance(module).getLinkedProjectId();
        if (path == null) {
            return null;
        }

        return PantsTargetAddress.fromString(path);
    }

}
