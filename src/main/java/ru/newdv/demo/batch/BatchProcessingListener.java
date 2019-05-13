package ru.newdv.demo.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@JobScope
@Slf4j
public class BatchProcessingListener implements ItemReadListener<SourceLine>, StepExecutionListener, JobExecutionListener {

    private AtomicInteger processedCount = new AtomicInteger();
    private AtomicInteger skippedCount =  new AtomicInteger();

    @Override
    public void beforeRead() {
    }

    @Override
    public void afterRead(SourceLine item) {
        log.info("Process line: {}", item);
    }

    @Override
    public void onReadError(Exception ex) {
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        processedCount.addAndGet(stepExecution.getWriteCount());
        skippedCount.addAndGet(stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Uploading complete. Total lines: {}, processed {}, skipped {}",
                processedCount.get() + skippedCount.get(), processedCount.get(), skippedCount.get());
    }
}
