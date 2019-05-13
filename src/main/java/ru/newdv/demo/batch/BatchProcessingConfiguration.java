package ru.newdv.demo.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import ru.newdv.demo.data.SourceLineData;
import ru.newdv.demo.data.SourceLineRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ru.newdv.demo.batch.SourceLine.SOURCE_LINE_BEAN_NAME;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchProcessingConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private BatchProcessingListener listener;

    @Bean
    public Job job(Step loadLinesStep) {
        return jobBuilderFactory.get("sourceLineJob")
                .listener(listener)
                .validator(jobParametersValidator())
                .incrementer(new RunIdIncrementer())
                .flow(loadLinesStep)
                .next(moveFileStep())
                .end()
                .build();
    }

    @Bean
    public Step loadLinesStep(ItemWriter<SourceLineData> writer, ItemReader<SourceLine> reader) {
        return stepBuilderFactory.get("loadLinesStep")
                .listener(listener)
                .<SourceLine, SourceLineData> chunk(10)
                .faultTolerant()
                .skipPolicy(skipPolicy())
                .reader(reader)
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public Step moveFileStep() {
        return stepBuilderFactory.get("moveFileStep")
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = chunkContext.getStepContext().getStepExecution().getJobParameters();
                    String sourcePath = jobParameters.getString("source.path");
                    String targetPath = jobParameters.getString("target.path");
                    log.info("Moving source file '{}' to the target path '{}'", sourcePath, targetPath);
                    Path sourceFile = Paths.get(sourcePath);
                    Files.move(sourceFile, Paths.get(targetPath).resolve(sourceFile.getFileName()), REPLACE_EXISTING);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    /**
     * Мы пропускаем линии, имеющие не верный формат.
     * @return SkipPolicy для наших линий
     */
    @Bean
    public SkipPolicy skipPolicy() {
        return new AlwaysSkipItemSkipPolicy() {
            @Override
            public boolean shouldSkip(Throwable t, int skipCount) {
                if (t instanceof FlatFileParseException) {
                    log.error("Line skipped", t);
                    return true;
                }
                return false;
            }
        };
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SourceLine> reader(@Value("#{jobParameters['source.path']}") String sourcePath) {
        return new FlatFileItemReaderBuilder<SourceLine>()
                .name("sourceLineReader")
                .resource(new FileSystemResource(sourcePath))
                .fieldSetMapper(fieldSetMapper())
                .linesToSkip(1)
                .delimited().delimiter(",").names(new String[] {"id", "name", "value"})
                .build();
    }

    @Bean
    public JobParametersValidator jobParametersValidator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[] {"source.path"});
        return validator;
    }

    @Bean
    public ItemProcessor<SourceLine, SourceLineData> processor() {
        return item -> new SourceLineData(item.getId(), item.getName(), item.getValue());
    }

    @Bean
    public ItemWriter<SourceLineData> writer(SourceLineRepository repository) {
        return new RepositoryItemWriterBuilder<SourceLineData>()
                .methodName("save")
                .repository(repository)
                .build();
    }

    @Bean
    public FieldSetMapper<SourceLine> fieldSetMapper() {
        BeanWrapperFieldSetMapper<SourceLine> fieldSetMapper = new BeanWrapperFieldSetMapper<>();

        fieldSetMapper.setPrototypeBeanName(SOURCE_LINE_BEAN_NAME);

        return fieldSetMapper;
    }
}
