package ru.newdv.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.newdv.demo.batch.BatchProcessingConfiguration;
import ru.newdv.demo.batch.BatchProcessingListener;
import ru.newdv.demo.batch.SourceLine;
import ru.newdv.demo.data.SourceLineData;
import ru.newdv.demo.data.SourceLineRepository;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = {
		BatchProcessingConfiguration.class,
		BatchProcessingListener.class,
		SourceLine.class,
		JobLauncherTestUtils.class})
class BatchProcessingTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	@MockBean
	private SourceLineRepository repository;

	@BeforeEach
	void beforeEach() {
		// Это unit тест, мы делаем mock для слоя работы с БД.
		when(repository.save(any(SourceLineData.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	void jobExecutionTest(@Autowired SourceLineRepository repository) throws Exception {
		JobParameters parameters = newJobParameters("source.csv");
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);

		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		verify(repository, times(10)).save(any());
	}

	@Test
	void emptySourceTest(@Autowired SourceLineRepository repository) throws Exception {
		JobParameters parameters = newJobParameters("emptySource.csv");
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);

		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		verifyZeroInteractions(repository);
	}

	@Test
	void badFormatSourceTest(@Autowired SourceLineRepository repository) throws Exception {
		JobParameters parameters = newJobParameters("badFormatSource.csv");
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);

		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		verify(repository, times(9)).save(any());
	}

	private JobParameters newJobParameters(String sourceName) throws IOException {
		String path = new ClassPathResource(sourceName).getFile().getPath();
		return new JobParametersBuilder().addString("source.path", path).toJobParameters();
	}
}
