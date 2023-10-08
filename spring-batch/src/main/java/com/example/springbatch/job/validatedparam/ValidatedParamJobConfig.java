package com.example.springbatch.job.validatedparam;

import com.example.springbatch.job.validatedparam.validator.FileParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * desc: 파일 이름 파라미터 전달 그리고 검증
 * run : spring.batch.job.names=validatedParamJob -fileName=test.csv
 */
@Configuration
@RequiredArgsConstructor
public class ValidatedParamJobConfig {

    /**
     * Job과 Step을 만들기위해
     * JobBuilderFactory와 StepBuilderFactory주입받는다
     */
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    //Job빈 생성
    @Bean
    public Job validatedParamJob(Step validatedParamStep) {
        return jobBuilderFactory.get("validatedParamJob")   //Job이름부여
                .incrementer(new RunIdIncrementer())        //Job아이디부여
//                .validator(new FileParamValidator())      //단일 validator사용시
                .validator(multipleValidator())             //다중 validator사용시
                .start(validatedParamStep)                //Job안에 Step을 생성
                .build()
                ;
    }
    //다중validator를 적용하기위함
    //CompositeJobParametersValidator를 반환하는 메서드
    private CompositeJobParametersValidator multipleValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(new FileParamValidator()));
        return validator;
    }
    //Step빈 생성
    //@JobScope, @StepScope : 해당 스코프가 선언되면 빈 생성이 어플리케이션 구동시점이 아니라 빈의 실행시점에 이루어지게 된다.
    @JobScope
    @Bean
    public Step validatedParamStep(Tasklet validatedParamTasklet) {
        return stepBuilderFactory.get("validatedParamStep")
                .tasklet(validatedParamTasklet)
                .build();
    }

    @StepScope
    @Bean                               /*#{jobParameters[파라미터명]}을 사용해서 파라미터 값을 가져온다.*/
    public Tasklet validatedParamTasklet(@Value("#{jobParameters['fileName']}") String fileName) {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println(fileName);
                System.out.println("validated Param Spring Batch");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
