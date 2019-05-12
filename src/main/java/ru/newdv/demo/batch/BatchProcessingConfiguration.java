package ru.newdv.demo.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import ru.newdv.demo.data.SourceLineData;
import ru.newdv.demo.data.SourceLineRepository;

import static ru.newdv.demo.batch.SourceLine.SOURCE_LINE_BEAN_NAME;

@Configuration
@EnableBatchProcessing
public class BatchProcessingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private BatchProcessingListener listener;

    @Bean
    public Job job(Step step) {
        return jobBuilderFactory.get("sourceLineJob")
                .listener(listener)
                .validator(jobParametersValidator())
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }

    @Bean
    public Step step(ItemWriter<SourceLineData> writer, ItemReader<SourceLine> reader) {
        return stepBuilderFactory.get("step")
                .listener(listener)
                .<SourceLine, SourceLineData> chunk(10)
                .faultTolerant()
                .skipPolicy(skipPolicy())
                .reader(reader)
                .processor(processor())
                .writer(writer)
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
                    logger.error("Line skipped", t);
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
