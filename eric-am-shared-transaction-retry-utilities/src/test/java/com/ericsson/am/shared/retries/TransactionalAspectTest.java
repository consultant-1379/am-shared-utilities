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
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.TransactionException;
import org.hibernate.exception.GenericJDBCException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionalAspectTest {

    private TransactionalAspect transactionalAspect;

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private MethodSignature methodSignature;

    private static final int ATTEMPTS = 2;
    private static final int DELAY = 1;

    private static final String TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME = "doSomeMagicInNewTransaction";
    private static final String TRANSACTIONAL_ON_CLASS_FUNC_NAME = "doSomeMagicOnClassTransaction";
    private static final String TRANSACTIONAL_ON_METHOD_FUNC_NAME = "doSomeMagicInTransaction";

    @BeforeEach
    void setUp() {
        transactionalAspect = new TransactionalAspect(ATTEMPTS, DELAY);
        when(pjp.getSignature()).thenReturn(methodSignature);
    }

    @Test
    void testAroundShouldProceedWithoutAttemptIfActualTransactionIsActiveAndPropagationIsRequired() throws Throwable {
        // given
        when(pjp.proceed()).thenReturn(new TransactionTestData());
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(true);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp).proceed();
    }

    @Test
    void testAroundShouldDoRetryIfActualTransactionIsActiveAndPropagationIsRequiredNew() throws Throwable {
        // given
        when(pjp.proceed()).thenReturn(new TransactionTestData());
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME));


        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(true);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp).proceed();
    }

    @Test
    void testAroundShouldProceedWithoutAttemptIfActualTransactionIsActiveAndTransactionalClass() throws Throwable {
        // given
        when(pjp.proceed()).thenReturn(new TransactionTestData());
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_CLASS_FUNC_NAME));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(true);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp).proceed();
    }

    @Test
    void testAroundShouldDoRetryIfActualTransactionIsNotActiveAndPropagationIsRequired() throws Throwable {
        // given
        when(pjp.proceed()).thenReturn(new TransactionTestData());
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp).proceed();
    }

    @Test
    void testAroundShouldDoRetryIfActualTransactionIsNotActiveAndPropagationIsRequiredNew() throws Throwable {
        // given
        when(pjp.proceed()).thenReturn(new TransactionTestData());
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp).proceed();
    }

    @Test
    void testAroundShouldDoRetryIfActualTransactionIsNotActiveAndTransactionalClass() throws Throwable {
        // given
        when(pjp.proceed()).thenReturn(new TransactionTestData());
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_CLASS_FUNC_NAME));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndPassOnSuccessAttemptIfTransactionIsNotActiveAndPropagationIsRequired()
            throws Throwable {
        // given
        var expectedSqlState = "57P01";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));
        when(pjp.proceed())
                .thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)))
                .thenReturn(new TransactionTestData());

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp, times(ATTEMPTS)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndPassOnSuccessAttemptIfTransactionIsNotActiveAndPropagationIsRequiredNew()
            throws Throwable {
        // given
        var expectedSqlState = "57P01";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME));
        when(pjp.proceed())
                .thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)))
                .thenReturn(new TransactionTestData());

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp, times(ATTEMPTS)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndPassOnSuccessAttemptIfTransactionIsNotActiveAndTransactionalClass()
            throws Throwable {
        // given
        var expectedSqlState = "57P01";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_CLASS_FUNC_NAME));
        when(pjp.proceed())
                .thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)))
                .thenReturn(new TransactionTestData());

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp, times(ATTEMPTS)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndPassOnSuccessAttemptIfTransactionIsActiveAndPropagationIsRequiredNew()
            throws Throwable {
        // given
        var expectedSqlState = "57P01";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME));
        when(pjp.proceed())
                .thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)))
                .thenReturn(new TransactionTestData());

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
        }
        // then
        verify(pjp, times(ATTEMPTS)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndFailOnFailedAttemptIfTransactionIsNotActiveAndPropagationIsRequired()
            throws Throwable {
        // given
        var expectedSqlState = "57P01";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));
        when(pjp.proceed()).thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (GenericJDBCException e) {
            // then
            assertThat(e.getMessage()).isEqualTo("Generic");
            assertThat(e.getSQLState()).isEqualTo(expectedSqlState);
        }
        verify(pjp, times(ATTEMPTS + 1)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndFailOnFailedAttemptIfTransactionIsNotActiveAndPropagationIsRequiredNew()
            throws Throwable {
        // given
        var expectedSqlState = "08003";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME));
        when(pjp.proceed()).thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (GenericJDBCException e) {
            // then
            assertThat(e.getMessage()).isEqualTo("Generic");
            assertThat(e.getSQLState()).isEqualTo(expectedSqlState);
        }
        verify(pjp, times(ATTEMPTS + 1)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndFailOnFailedAttemptIfTransactionIsNotActiveAndTransactionalClass()
            throws Throwable {
        // given
        var expectedSqlState = "08003";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_CLASS_FUNC_NAME));
        when(pjp.proceed()).thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (GenericJDBCException e) {
            // then
            assertThat(e.getMessage()).isEqualTo("Generic");
            assertThat(e.getSQLState()).isEqualTo(expectedSqlState);
        }
        verify(pjp, times(ATTEMPTS + 1)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnExceptionAndFailOnFailedAttemptIfTransactionIsActiveAndPropagationIsRequiredNew()
            throws Throwable {
        // given
        var expectedSqlState = "08001";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_WITH_REQUIRED_NEW_FUNC_NAME));
        when(pjp.proceed()).thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(true);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (GenericJDBCException e) {
            // then
            assertThat(e.getMessage()).isEqualTo("Generic");
            assertThat(e.getSQLState()).isEqualTo(expectedSqlState);
        }
        verify(pjp, times(ATTEMPTS + 1)).proceed();
    }

    @Test
    void testAroundShouldDoRetryOnSqlExceptionAndFailOnFailedAttemptIfTransactionIsNotActiveAndPropagationIsRequired()
            throws Throwable {
        // given
        var expectedMessage = "Could not roll back JPA transaction";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));
        when(pjp.proceed()).thenThrow(
                new TransactionSystemException(expectedMessage,
                        new TransactionException("Unable to rollback against JDBC Connection",
                                new SQLException("Connection is closed"))));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (TransactionSystemException e) {
            // then
            assertThat(e.getMessage()).isEqualTo(expectedMessage);
        }
        verify(pjp, times(ATTEMPTS + 1)).proceed();
    }

    @Test
    void testAroundShouldFailWithAttemptsOnExceptionWithUnknownSqlStateIfTransactionIsNotActiveAndPropagationIsRequired()
            throws Throwable {
        // given
        var expectedSqlState = "UNKNOWN";
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));
        when(pjp.proceed()).thenThrow(new GenericJDBCException("Generic", new SQLException("SQL", expectedSqlState)));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (GenericJDBCException e) {
            // then
            assertThat(e.getMessage()).isEqualTo("Generic");
            assertThat(e.getSQLState()).isEqualTo(expectedSqlState);
        }
        verify(pjp, times(ATTEMPTS + 1)).proceed();
    }

    @Test
    void testAroundShouldFailWithoutAttemptsOnUnknownExceptionIfTransactionIsNotActiveAndPropagationIsRequired()
            throws Throwable {
        // given
        when(methodSignature.getMethod()).thenReturn(getMethod(TRANSACTIONAL_ON_METHOD_FUNC_NAME));
        when(pjp.proceed()).thenThrow(new RuntimeException("UNKNOWN", new IOException("IO exception")));

        try (var utilities = mockStatic(TransactionSynchronizationManager.class)) {
            utilities.when(TransactionSynchronizationManager::isActualTransactionActive)
                    .thenReturn(false);
            // when
            transactionalAspect.around(pjp);
            fail();
        } catch (Exception e) {
            // then
            assertThat(e.getMessage()).isEqualTo("UNKNOWN");
        }
        verify(pjp).proceed();
    }

    private Method getMethod(String methodName) throws NoSuchMethodException {
        return TransactionTestData.class.getMethod(methodName);
    }

}