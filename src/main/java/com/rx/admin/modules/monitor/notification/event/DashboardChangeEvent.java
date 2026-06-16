package com.rx.admin.modules.monitor.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DashboardChangeEvent {

    private final String section;

    public static final String SECTION_SYSTEM = "system";
    public static final String SECTION_ENHANCED = "enhanced";
    public static final String SECTION_LITERATURE = "literature";
    public static final String SECTION_CLASSICS = "classics";
    public static final String SECTION_TECHBLOG = "techblog";
    public static final String SECTION_ALL = "all";
}
