package com.restaurant.app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    @Around("execution(* com.restaurant.app.sevice.impl..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            LOGGER.info("Executed {} in {} ms", joinPoint.getSignature().toShortString(), durationMs);
        }
    }
}
