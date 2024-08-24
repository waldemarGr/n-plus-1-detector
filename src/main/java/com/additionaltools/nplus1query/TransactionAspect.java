package com.additionaltools.nplus1query;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Aspect for monitoring transaction-related statistics.
 *
 * <p>This aspect intercepts methods and classes annotated with {@link org.springframework.transaction.annotation.Transactional}
 * and performs operations before the transaction starts.</p>
 */
@Aspect
@Component
public class TransactionAspect {

    private final SQLStatisticsService sqlStatisticsService;

    public TransactionAspect(SQLStatisticsService sqlStatisticsService) {
        this.sqlStatisticsService = sqlStatisticsService;
    }

    /**
     * Pointcut for methods and classes annotated with {@link org.springframework.transaction.annotation.Transactional}.
     */
    @Pointcut("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalMethodOrClass() {
    }

    /**
     * Method invoked before a transaction starts.
     *
     * <p>If this is a new transaction, registers a transaction synchronization and clears statistics.</p>
     *
     * @param joinPoint the join point providing information about the method being invoked
     */
    @Before("transactionalMethodOrClass()")
    public void beforeTransaction(JoinPoint joinPoint) {
        String signatureMethodBeginningTransaction = joinPoint.getSignature().toString();
        boolean isNewTransaction = TransactionAspectSupport.currentTransactionStatus().isNewTransaction();

        if (isNewTransaction) {
            TransactionSynchronizationManager.registerSynchronization(
                    new StatisticTransactionSynchronization(signatureMethodBeginningTransaction, sqlStatisticsService));
            sqlStatisticsService.clearStatistics();
        }
    }
}