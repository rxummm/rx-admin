package com.rx.admin.common.metrics;

import com.ibm.as400.access.AS400;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("null")
public class As400HealthIndicator implements HealthIndicator {

    @Value("${as400.host:}")
    private String host;

    @Value("${as400.username:}")
    private String username;

    @Value("${as400.password:}")
    private String password;

    @Override
    public Health health() {
        if (host.isBlank()) {
            return Health.unknown().withDetail("message", "AS400 not configured").build();
        }
        try (AS400 as400 = new AS400(host, username)) {
            as400.setPassword(password.toCharArray());
            boolean connected = as400.isConnected();
            as400.disconnectAllServices();
            if (connected) {
                return Health.up().withDetail("host", host).build();
            }
            return Health.down().withDetail("host", host).build();
        } catch (Exception e) {
            return Health.down(e).withDetail("host", host).build();
        }
    }
}
