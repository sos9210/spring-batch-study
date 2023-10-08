package com.example.springbatch.job.helloworld;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//--spring.batch.job.names=helloWorldJob
@Configuration
@RequiredArgsConstructor
public class HelloWorldJobConfig {

    /**
     * Job과 Step을 만들기위해
     * JobBuilderFactory와 StepBuilderFactory주입받는다
     */
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    //Job빈 생성
    @Bean
    public Job helloWorldJob() {
        return jobBuilderFactory.get("helloWorldJob")   //Job이름부여
                .incrementer(new RunIdIncrementer())    //Job아이디부여
                .start(helloWorldStep())                //Job안에 Step을 생성
                .build()
                ;
    }

    //Step빈 생성
    //@JobScope, @StepScope : 해당 스코프가 선언되면 빈 생성이 어플리케이션 구동시점이 아니라 빈의 실행시점에 이루어지게 된다.
    @JobScope
    @Bean
    public Step helloWorldStep() {
        return stepBuilderFactory.get("helloWorldStep")
                .tasklet(helloWorldTasklet())
                .build();
    }

    @StepScope
    @Bean
    public Tasklet helloWorldTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello World Spring Batch");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
