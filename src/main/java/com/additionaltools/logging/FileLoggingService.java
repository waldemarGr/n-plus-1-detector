package com.additionaltools.logging;


import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FileLoggingService implements LoggingService {

    private static final String FILE_PATH = "HiPerAnalyzerLogs.txt";
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final Lock lock = new ReentrantLock();

    /**
     * Dodaje log do kolejki.
     *
     * @param log Log do dodania.
     */
    @Override
    public void addLog(String log) {
        logQueue.offer(log);
    }

        @PostConstruct
        public void initializeLogFile() {
            lock.lock();
            try {
                File file = new File(FILE_PATH);
                    addWelcomeMessage(file);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
    }



    @Override
    @Scheduled(fixedRate = 10000)
    public void flushLogs() {
        if (!logQueue.isEmpty()) {
            lock.lock();
            try (FileWriter fw = new FileWriter(FILE_PATH, true);
                 PrintWriter writer = new PrintWriter(fw)) {
                String log;
                while ((log = logQueue.poll()) != null) {
                    writer.println(log);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    private void addWelcomeMessage(File file) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (FileWriter fw = new FileWriter(file, false);
             PrintWriter writer = new PrintWriter(fw)) {
            writer.println("=== Welcome to HiPerAnalyzer (Hibernate Performance Analyzer)! ===");
            writer.println("This application logs potential inefficient Hibernate usage.");
            writer.println("------------------------------------------");
            writer.println("For more details visit: https://github.com/waldemarGr/n-plus-1-detector");
            writer.println("------------------------------------------");
            writer.println("Application started at: " + now.format(formatter));
            writer.println("------------------------------------------");
        }
    }
}