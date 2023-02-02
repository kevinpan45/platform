package io.github.kevinpan45.platform.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.util.concurrent.RateLimiter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Use semaphore to implement rate limiter
 */
@Slf4j
@Component
public class JobDispatcher {
    // Semaphore to limit the number of concurrent jobs
    private Semaphore semaphore;

    public Semaphore getSemaphore() {
        return semaphore;
    }

    @Autowired
    private JobEngine jobEngine;

    @Value("${job.maxConcurrentJob:10}")
    private int maxConcurrentJob;

    private List<Integer> jobIds;

    @PostConstruct
    public void init() {
        Assert.isTrue(0 > maxConcurrentJob || maxConcurrentJob < 2000, "Max job count must in range 0-2000");
        semaphore = new Semaphore(maxConcurrentJob);
        log.info("Job dispatcher initialized with max concurrent job {}", maxConcurrentJob);
    }

    public void setMaxConcurrentJob(int maxConcurrentJob) {
        this.maxConcurrentJob = maxConcurrentJob;
        if (maxConcurrentJob > semaphore.availablePermits()) {
            semaphore.release(maxConcurrentJob - semaphore.availablePermits());
        } else {
            semaphore.acquireUninterruptibly(semaphore.availablePermits() - maxConcurrentJob);
        }
        log.info("Max concurrent job set to {}", maxConcurrentJob);
    }

    public void setJobIds(List<Integer> jobIds) {
        this.jobIds = jobIds;
        log.info("Get {} jobs to dispatch.", jobIds.size());
        // if (log.isDebugEnabled()) {
        // log.debug("Job ids: {}", jobIds);
        // }
    }

    public void dispatch() {
        JobEngine jobEngine = new JobEngine() {
            @Override
            public void createJob() {
                // TODO Auto-generated method stub
            }
        };
        for (Integer jobId : jobIds) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                log.error("Job dispatch interrupted", e);
            }
            try {
                jobEngine.execute(jobId);
            } catch (Exception e) {
                log.error("Job {} execution failed", jobId, e);
                // Release the semaphore if job execution failed
                semaphore.release();
            }
        }
    }

    public static void main(String[] args) {
        int maxConcurrentJob = 100;
        List<Integer> jobIds = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            jobIds.add(i);
        }
        JobDispatcher dispatcher = new JobDispatcher();
        dispatcher.init();
        dispatcher.setMaxConcurrentJob(maxConcurrentJob);
        dispatcher.setJobIds(jobIds);
        Thread t = new Thread(new Runnable() {
            RateLimiter limiter = RateLimiter.create(2);

            @Override
            public void run() {
                while (true) {
                    limiter.acquire(5);
                    dispatcher.getSemaphore().release(5);
                }
            }
        });
        t.start();
        dispatcher.dispatch();

    }
}
