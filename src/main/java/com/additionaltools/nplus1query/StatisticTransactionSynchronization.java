package com.additionaltools.nplus1query;


import org.springframework.transaction.support.TransactionSynchronization;

/**
 * Custom {@link TransactionSynchronization} implementation for handling statistics
 * after a transaction completes.
 *
 * <p>This class is used to invoke the {@link SQLStatisticsService} to print statistics
 * related to the transaction once it has been committed.</p>
 */
public class StatisticTransactionSynchronization implements TransactionSynchronization {

    private final String signatureMethodBeginningTransaction;
    private final SQLStatisticsService sqlStatisticsService;


    public StatisticTransactionSynchronization(String signatureMethodBeginningTransaction, SQLStatisticsService sqlStatisticsService) {
        this.signatureMethodBeginningTransaction = signatureMethodBeginningTransaction;
        this.sqlStatisticsService = sqlStatisticsService;
    }

    /**
     * Invoked after the transaction has completed.
     *
     * <p>This method is called by the Spring transaction infrastructure after the transaction has
     * finished. If the transaction was committed, it triggers the {@link SQLStatisticsService}
     * to print the collected statistics.</p>
     *
     * @param status the status of the transaction (committed, rolled back, etc.)
     */
    @Override
    public void afterCompletion(int status) {
        if (status == STATUS_COMMITTED) {
            sqlStatisticsService.printStatistics(signatureMethodBeginningTransaction);
        }
    }
}