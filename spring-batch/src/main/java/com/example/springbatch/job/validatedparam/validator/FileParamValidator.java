package com.example.springbatch.job.validatedparam.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

//Job 파라미터를 검증하는 validator
//스프링배치에서 제공하는 JobParametersValidator 구현한다
public class FileParamValidator implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String fileName = parameters.getString("fileName");
        if(!StringUtils.endsWithIgnoreCase(fileName,"csv")) {
            throw new JobParametersInvalidException("This is not csv file");
        }
    }
}
