package com.twitter.intellij.pants.components.impl;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants;
import com.intellij.openapi.util.registry.Registry;
import com.twitter.intellij.pants.metrics.PantsMetrics;
import com.twitter.intellij.pants.util.PantsConstants;
import com.twitter.intellij.pants.util.PantsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PantsInitImpl implements AppLifecycleListener {

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        PantsMetrics.initialize();
        final String key = PantsConstants.SYSTEM_ID.getId() + ExternalSystemConstants.USE_IN_PROCESS_COMMUNICATION_REGISTRY_KEY_SUFFIX;
        Registry.get(key).setValue(true);
    }

    @Override
    public void appWillBeClosed(boolean isRestart) {
        PantsUtil.scheduledThreadPool.shutdown();
        PantsMetrics.globalCleanup();
    }
}
