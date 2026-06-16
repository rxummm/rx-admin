package com.rx.admin.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class CustomMetricsService {

    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter operationCounter;
    private final Timer apiTimer;

    public CustomMetricsService(MeterRegistry registry) {
        this.loginSuccessCounter = Counter.builder("rx.login.success")
                .description("Successful logins")
                .register(registry);
        this.loginFailureCounter = Counter.builder("rx.login.failure")
                .description("Failed login attempts")
                .register(registry);
        this.operationCounter = Counter.builder("rx.operation.total")
                .description("Total write operations")
                .register(registry);
        this.apiTimer = Timer.builder("rx.api.duration")
                .description("API call duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void recordLoginFailure() {
        loginFailureCounter.increment();
    }

    public void recordOperation() {
        operationCounter.increment();
    }

    public <T> T timeApi(Callable<T> callable) {
        return apiTimer.record(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FunctionalInterface
    public interface Callable<T> {
        T call() throws Exception;
    }
}
