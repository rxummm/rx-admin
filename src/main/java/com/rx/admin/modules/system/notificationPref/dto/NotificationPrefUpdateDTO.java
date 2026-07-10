package com.rx.admin.modules.system.notificationPref.dto;

import lombok.Data;

@Data
public class NotificationPrefUpdateDTO {
    private String eventType;
    private Integer emailEnabled;
    private Integer websocketEnabled;
    private Integer browserEnabled;
    private String quietStart;
    private String quietEnd;
}
