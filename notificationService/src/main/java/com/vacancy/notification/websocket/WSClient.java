package com.vacancy.notification.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WSClient {
    private Long id;
    private boolean isOrganization;
}
