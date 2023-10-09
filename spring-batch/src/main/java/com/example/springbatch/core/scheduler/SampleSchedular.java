package com.example.springbatch.core.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SampleSchedular {

    private final Job helloWorldJob;

    //스케줄링을 활용하여 Job을 실행하기위해 JobLauncher를 주입받는다.
    private final JobLauncher jobLauncher;

    //1분마다 스케줄링을 실행한다.
    @Scheduled(cron = "0 */1 * * * *")
    public void helloWorldJobRun() throws Exception {
        JobParameters jobParameters = new JobParameters(
                Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis()))
        );
        jobLauncher.run(helloWorldJob, jobParameters);
    }
}
