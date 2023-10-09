package com.example.springbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//실행전에 환경설정에 --spring.batch.job.names={jobName} 추가하여 필요함
@EnableBatchProcessing	//배치를 구동하기위해 설정
@SpringBootApplication
@EnableScheduling	//스케줄링을 사용하기위한 설정
public class SpringBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchApplication.class, args);
	}

}
