package ru.newdv.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

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
		launcher.run(job, jobParameters());
	}

	private JobParameters jobParameters() {
		return new JobParametersBuilder()
				.addDate("date", new Date(), true)
				.addString("source.path", sourcePath)
				.addString("target.path", targetPath).toJobParameters();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
