package pl.peth.hsbo_spring_demo.aspect;

import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.service.MetricsService;

@Aspect
@Component
public class MetricsAspect {
    private final MetricsService metricsService;

    public MetricsAspect(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Aspect to measure the execution time of API calls in classes annotated with @RestController.
     * It uses Micrometer Timer to record the duration and count of API calls.
     *
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution
     * @throws Throwable if any error occurs during method execution
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object measureApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Timer.Sample sample = metricsService.startApiCallTimer();

        try {
            Object result = joinPoint.proceed();
            metricsService.incrementApiCallCount(methodName);

            return result;
        } finally {
            metricsService.stopApiCallTimer(sample, methodName);
        }
    }
}
