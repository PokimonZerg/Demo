package ru.newdv.demo.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@JobScope
public class BatchProcessingListener implements ItemReadListener<SourceLine>, StepExecutionListener, JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingListener.class);

    private AtomicInteger processedCount = new AtomicInteger();
    private AtomicInteger skippedCount =  new AtomicInteger();

    @Override
    public void beforeRead() {
    }

    @Override
    public void afterRead(SourceLine item) {
        logger.info("Process line: {}", item);
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
        logger.info("Uploading complete. Total lines: {}, processed {}, skipped {}",
                processedCount.get() + skippedCount.get(), processedCount.get(), skippedCount.get());
    }
}
