package io.github.kevinpan45.platform.job;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public abstract class JobEngine {
    public abstract void createJob();

    // public abstract void execute(Integer jobId);
    public void execute(Integer jobId) {
        log.info("Job {} is running", jobId);
    }
}
