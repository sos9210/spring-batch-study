package com.example.springbatch.job.filedatareadwrite;

import com.example.springbatch.job.filedatareadwrite.dto.Player;
import com.example.springbatch.job.filedatareadwrite.dto.PlayerYears;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

/**
 * desc:
 * run: --spring.batch.job.names=fileReadWriteJob
 */
@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {

    /**
     * Job과 Step을 만들기위해
     * JobBuilderFactory와 StepBuilderFactory주입받는다
     */
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    //Job빈 생성
    @Bean
    public Job fileReadWriteJob(Step fileReadWriteStep) {
        return jobBuilderFactory.get("fileReadWriteJob")   //Job이름부여
                .incrementer(new RunIdIncrementer())    //Job아이디부여
                .start(fileReadWriteStep)                //Job안에 Step을 생성
                .build()
                ;
    }

    //Step빈 생성
    //@JobScope, @StepScope : 해당 스코프가 선언되면 빈 생성이 어플리케이션 구동시점이 아니라 빈의 실행시점에 이루어지게 된다.
    @JobScope
    @Bean
    public Step fileReadWriteStep(ItemReader playerItemReader,
                                  ItemProcessor playerItemProcessor,
                                  ItemWriter playerYearsItemWriter) {
        return stepBuilderFactory.get("fileReadWriteStep")
                //chunk에서 Data를 어떤객체로 읽어와서 출력할것인지 제네릭타입 명시하고 몇개의 단위로 처리할 것인지 사이즈를 명시한다.
                //Player객체로 읽어와서 PlayerYears로 출력한다.
                //5개의단위로 커밋(하나의 트랜잭션으로 처리한다)
                .<Player, PlayerYears>chunk(5)
                .reader(playerItemReader)
//                .writer(new ItemWriter() {
//                    @Override
//                    public void write(List items) throws Exception {
//                        items.forEach(System.out::println);
//                    }
//                })
                .processor(playerItemProcessor)         //읽어온 데이터를 가공처리 한다.
                .writer(playerYearsItemWriter)          //읽어온 데이터를 File로 쓰기작업을 수행한다.
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Player,PlayerYears> playerItemProcessor() {
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }

    //파일에서 Data를 읽어올수 있는reader
    @StepScope
    @Bean
    public FlatFileItemReader<Player> playerItemReader() {
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerItemReader")
                .resource(new FileSystemResource("players.csv")) //읽어올 Data가 저장된 파일의 위치
                .lineTokenizer(new DelimitedLineTokenizer())        //Data를 어떤 기준으로 나눠서 읽을지
                .fieldSetMapper(new PlayerFieldSetMapper())         //개발자가 읽어온 데이터를 객체로 변경할 수 있는 맵퍼를등록한다
                .linesToSkip(1)                                     //첫번째줄은 필드명라인이기 때문에 스킵한다는것을 명시한다.
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerYears> playerYearsItemWriter() {
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID","lastName","position","yearsExperience"});    //추출할 데이터의 필드명들
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");                                                       //쉼표로 구분자처리를 수행한다.
        lineAggregator.setFieldExtractor(fieldExtractor);

        FileSystemResource outputResource = new FileSystemResource("players_output.txt");

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playerItemWriter")
                .resource(outputResource)
                .lineAggregator(lineAggregator)
                .build();
    }

}
