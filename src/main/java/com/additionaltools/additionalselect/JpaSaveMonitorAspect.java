package com.additionaltools.additionalselect;

import com.additionaltools.logging.LoggingService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

@Aspect
public class JpaSaveMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(JpaSaveMonitorAspect.class);
    private final SessionFactory sessionFactory;
    private final LoggingService loggingService;

    public JpaSaveMonitorAspect(SessionFactory sessionFactory, LoggingService loggingService) {
        this.sessionFactory = sessionFactory;
        this.loggingService = loggingService;
    }

    //todo add saveAll
    @Around("execution(* org.springframework.data.repository.CrudRepository.save(..))")
    public Object logSaveOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        Object target = joinPoint.getTarget();

        String repositoryName = "";
        if (Proxy.isProxyClass(target.getClass())) {
            Class<?>[] interfaces = target.getClass().getInterfaces();
            repositoryName = interfaces.length > 0 ? interfaces[0].getName() : "";
        } else if (target.getClass().getName().contains("CGLIB")) {
            repositoryName = target.getClass().getName();
        }
        Object result = joinPoint.proceed();

        long preparedCount = statistics.getPrepareStatementCount();
        long insertCount = statistics.getEntityInsertCount();

        if (preparedCount > insertCount && insertCount > 0) {
            String message = "SELECT_BEFORE_INSERT: Potential inefficiency detected in %s: SELECT before INSERT during save operation.".formatted(repositoryName);
            loggingService.addLog(message);
            logger.warn(message);
        }
        return result;
    }
}
