package com.additionaltools.nplus1query;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.internal.CollectionStatisticsImpl;
import org.hibernate.stat.internal.StatisticsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing and analyzing SQL statistics from Hibernate.
 *
 * <p>This service retrieves statistics related to SQL queries and collections,
 * and detects potential N+1 query issues.</p>
 */
public class SQLStatisticsService {
    private final Statistics statistics;
    private final StatisticsImpl statisticsImp;
    private static final Logger log = LoggerFactory.getLogger(SQLStatisticsService.class);

    public SQLStatisticsService(EntityManagerFactory entityManagerFactory) {
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        this.statistics.setStatisticsEnabled(true);
        this.statisticsImp = (StatisticsImpl) statistics;
    }

    /**
     * Prints statistics related to the number of additional query fetches for collections.
     *
     * <p>This method checks for potential N+1 query problems by analyzing collection fetches.
     * If such issues are detected, they are logged as warnings.</p>
     *
     * @param methodName the name of the method where the statistics are being analyzed
     */
    public void printStatistics(String methodName) {
        String[] queries = statistics.getQueries();

        String[] collectionRoleNames = statistics.getCollectionRoleNames();

        List<CollectionStatisticsImpl> statisticsList = Arrays.stream(collectionRoleNames).toList().stream()
                .map(statisticsImp::getCollectionStatistics)
                .toList();

        List<CollectionStatisticsImpl> nPlusOneStatisticList = statisticsList.stream()
                .filter(collectionStatistics -> collectionStatistics.getFetchCount() > 0)
                .toList();

        if (!nPlusOneStatisticList.isEmpty()) {

            List<String> fetchDetails = nPlusOneStatisticList.stream()
                    .map(collectionStatistics -> String.format(
                            "Number of additional query fetches: %d, Collection: %s",
                            collectionStatistics.getFetchCount(),
                            extractCollectionRole(collectionStatistics.toString())
                    ))
                    .toList();

            String warning = "N+1 problem detected. Queries: %s; Method: %s; Summary: %s"
                    .formatted(
                            Arrays.toString(queries),
                            methodName,
                            fetchDetails
                    );
            log.warn(warning);
        } else {
            log.trace("No N+1 issue detected for method %s".formatted(methodName));
        }
    }

    /**
     * Clears the collected statistics.
     */
    public void clearStatistics() {
        statistics.clear();
    }

    /**
     * Extracts the collection role from a string representation of collection statistics.
     *
     * <p>This method uses a regex pattern to find the collection role within the provided string.
     * An effort is being made to find a more elegant solution for extracting the collection role.</p>
     *
     * @param collectionStatisticsString the string representation of collection statistics
     * @return the extracted collection role, or the original string if not found
     */
    private static String extractCollectionRole(String collectionStatisticsString) {
        Pattern pattern = Pattern.compile("collectionRole=([^,\\]]+)");
        Matcher matcher = pattern.matcher(collectionStatisticsString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return collectionStatisticsString;
    }
}
