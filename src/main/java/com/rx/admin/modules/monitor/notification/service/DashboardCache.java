package com.rx.admin.modules.monitor.notification.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class DashboardCache {

    @Getter
    private volatile boolean literatureDirty = true;
    @Getter
    private volatile boolean classicsDirty = true;
    @Getter
    private volatile boolean techblogDirty = true;
    @Getter
    private volatile boolean systemDirty = true;
    @Getter
    private volatile boolean enhancedDirty = true;

    public void markLiteratureDirty() {
        this.literatureDirty = true;
    }

    public void markClassicsDirty() {
        this.classicsDirty = true;
    }

    public void markTechblogDirty() {
        this.techblogDirty = true;
    }

    public void markSystemDirty() {
        this.systemDirty = true;
    }

    public void markEnhancedDirty() {
        this.enhancedDirty = true;
    }

    public void clearLiteratureDirty() {
        this.literatureDirty = false;
    }

    public void clearClassicsDirty() {
        this.classicsDirty = false;
    }

    public void clearTechblogDirty() {
        this.techblogDirty = false;
    }

    public void clearSystemDirty() {
        this.systemDirty = false;
    }

    public void clearEnhancedDirty() {
        this.enhancedDirty = false;
    }

    public boolean hasAnyDirty() {
        return literatureDirty || classicsDirty || techblogDirty || systemDirty || enhancedDirty;
    }

    public void markAllDirty() {
        literatureDirty = true;
        classicsDirty = true;
        techblogDirty = true;
        systemDirty = true;
        enhancedDirty = true;
    }
}
