package com.example.springbatch.job.dbdatareadwrite;

import com.example.springbatch.core.domain.accounts.Accounts;
import com.example.springbatch.core.domain.accounts.AccountsRepository;
import com.example.springbatch.core.domain.orders.Orders;
import com.example.springbatch.core.domain.orders.OrdersRepository;
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
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.awt.print.Pageable;
import java.util.Collections;
import java.util.List;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 * run:  --spring.batch.job.names=trMigration
 */
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {

    private final AccountsRepository accountsRepository;
    private final OrdersRepository ordersRepository;

    /**
     * Job과 Step을 만들기위해
     * JobBuilderFactory와 StepBuilderFactory주입받는다
     */
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    //Job빈 생성
    @Bean
    public Job trMigrationJob(Step trMigrationStep) {
        return jobBuilderFactory.get("trMigrationJob")   //Job이름부여
                .incrementer(new RunIdIncrementer())    //Job아이디부여
                .start(trMigrationStep)                //Job안에 Step을 생성
                .build()
                ;
    }

    //Step빈 생성
    //@JobScope, @StepScope : 해당 스코프가 선언되면 빈 생성이 어플리케이션 구동시점이 아니라 빈의 실행시점에 이루어지게 된다.
    @JobScope
    @Bean
    public Step trMigrationStep(ItemReader trOrdersReader,ItemProcessor trOrderProcessor, ItemWriter trOrdersWriter) {
        return stepBuilderFactory.get("trMigrationStep")
                //chunk에서 Data를 어떤객체로 읽어와서 출력할것인지 제네릭타입 명시하고 몇개의 단위로 처리할 것인지 사이즈를 명시한다.
                //Orders객체로 읽어와서 Accounts로 출력한다.
                //5개의단위로 커밋(하나의 트랜잭션으로 처리한다)
                .<Orders, Accounts> chunk(5)
                .reader(trOrdersReader)
//                .writer(new ItemWriter() {          //읽어온 데이터를 출력한다
//                    @Override
//                    public void write(List items) throws Exception {
//                        items.forEach(System.out::println);
//                    }
//                })
                .processor(trOrderProcessor)
                .writer(trOrdersWriter)
                .build();
    }

    @StepScope
    @Bean
    //repository에서 데이터를 write한다
    public RepositoryItemWriter<Accounts> trOrdersWriter() {
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository) //사용할 Repository
                .methodName("save")             //사용할 method
                .build();
    }

    //직접 ItemWriter를 구현해서 반환하는 방법도 가능하다.
//    @StepScope
//    @Bean
//    public ItemWriter<Accounts> trOrdersWriter() {
//        return new ItemWriter<Accounts>() {
//            @Override
//            public void write(List<? extends Accounts> items) throws Exception {
//                items.forEach(item -> accountsRepository.save(item));
//            }
//        };
//    }

    @StepScope
    @Bean
    //Orders엔티티 에서 Accounts엔티티로 변경할 수 있도록 처리해준다
    public ItemProcessor<Orders,Accounts> trOrderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    @StepScope
    @Bean
    //repository에서 데이터를 read한다
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrderReader")          //ItemReader의 name
                .repository(ordersRepository)   //사용할 Repository
                .methodName("findAll")          //사용할 method
                .pageSize(5)                    //가져올 데이터 사이즈
//                .arguments(..)                //메서드에 전달할 argument가 존재한다면 입력해준다.
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

}

