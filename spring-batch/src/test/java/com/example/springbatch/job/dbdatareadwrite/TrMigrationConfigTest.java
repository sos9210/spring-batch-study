package com.example.springbatch.job.dbdatareadwrite;

import com.example.springbatch.SpringBatchTestConfig;
import com.example.springbatch.core.domain.accounts.AccountsRepository;
import com.example.springbatch.core.domain.orders.Orders;
import com.example.springbatch.core.domain.orders.OrdersRepository;
import com.example.springbatch.job.helloworld.HelloWorldJobConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = {SpringBatchTestConfig.class, TrMigrationConfig.class})
class TrMigrationConfigTest {

    //Job테스트를 위한 TestUtil
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @AfterEach
    void cleanUpEach() {
        ordersRepository.deleteAll();
    }

    @Test
    void success_noData() throws Exception {
        //Job실행
        JobExecution execution = jobLauncherTestUtils.launchJob();

        //실행결과 검증
        assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
        assertEquals(0,accountsRepository.count());
    }

    @Test
    void success_yesData() throws Exception{
        //데이터 셋팅
        Orders orders1 = new Orders(null, "kakao gift", 15000, new Date());
        Orders orders2 = new Orders(null, "naver gift", 15000, new Date());

        ordersRepository.save(orders1);
        ordersRepository.save(orders2);

        //Job실행
        JobExecution execution = jobLauncherTestUtils.launchJob();

        //실행결과 검증
        assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
        assertEquals(2,accountsRepository.count());
    }
}