package ru.newdv.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	@Autowired
	private JobLauncher launcher;
	@Autowired
	private Job job;

	@Value("${source.path}")
	private String sourcePath;
	@Value("${target.path}")
	private String targetPath;

	@Override
	public void run(String... args) throws Exception {
		JobExecution execution = launcher.run(job, jobParameters());

		while (execution.isRunning()) {
			try {
				Thread.sleep(128);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				execution.stop();
				break;
			}
		}

		if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			logger.info("Copy source file '{}' to the target path '{}'", sourcePath, targetPath);
			Path sourceFile = Paths.get(sourcePath);
			Files.copy(sourceFile, Paths.get(targetPath).resolve(sourceFile.getFileName()), REPLACE_EXISTING);
		}
	}

	public JobParameters jobParameters() {
		return new JobParametersBuilder()
				.addDate("date", new Date(), true)
				.addString("source.path", sourcePath).toJobParameters();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
