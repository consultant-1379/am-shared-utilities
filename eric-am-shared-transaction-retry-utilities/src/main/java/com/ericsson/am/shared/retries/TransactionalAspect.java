/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.am.shared.retries;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.exception.GenericJDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Aspect
@Order(1)
public class TransactionalAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalAspect.class);
    private static final Set<String> SQL_STATE_CAUSES = Set.of("08003", "08006", "08001", "57P01");
    private final int attempts;
    private final int delay;

    public TransactionalAspect(int attempts, int delay) {
        this.attempts = attempts;
        this.delay = delay;
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || "
            + "@within(org.springframework.transaction.annotation.Transactional)")
    public Object around(final ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        boolean isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean isPropagationRequiredNew = isPropagationRequiredNew(pjp);
        if (isTransactionActive && !isPropagationRequiredNew) {
            return proceed(pjp);
        }
        for (int i = 1; i <= attempts + 1; i++) {
            try {
                result = proceed(pjp);
                break;
            } catch (RuntimeException e) {
                LOGGER.info("Catch transactional exception {}", e.getClass());
                Optional<Throwable> cause = getCause(e);
                if (cause.isPresent() && i <= attempts) {
                    LOGGER.info("Attempt {} from {}, sleep for {} seconds", i, attempts, delay);
                    TimeUnit.SECONDS.sleep(delay);
                } else {
                    LOGGER.error("Cause GenericJDBCException not found", e);
                    throw e;
                }
            }
        }
        return result;
    }

    private Optional<Throwable> getCause(Throwable cause) {
        LOGGER.info("Checking cause for GenericJDBCException {}", cause != null ?
                "[%s]: %s".formatted(cause.getClass(), cause.getMessage()) :
                null);
        if (cause != null && cause != cause.getCause()) {
            return getJdbcCause(cause);
        }
        return Optional.empty();
    }

    private Optional<Throwable> getJdbcCause(Throwable cause) {
        if (cause instanceof GenericJDBCException) {
            String sqlState = ((GenericJDBCException) cause).getSQLState();
            LOGGER.info("Found cause with postgres error code {}", sqlState);
            if (sqlState == null || SQL_STATE_CAUSES.contains(sqlState)) {
                return Optional.of(cause);
            }
        } else if (cause instanceof SQLException) {
            LOGGER.info("Found SQLException: {}", cause.getMessage());
            return Optional.of(cause);
        }
        return getCause(cause.getCause());
    }

    private boolean isPropagationRequiredNew(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Transactional annotation = method.getAnnotation(Transactional.class);
        if (annotation == null) {
            return false;
        }
        Propagation propagation = annotation.propagation();
        return propagation == Propagation.REQUIRES_NEW;
    }

    private Object proceed(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        return args != null && args.length > 0 ? pjp.proceed(pjp.getArgs()) : pjp.proceed();
    }

}
