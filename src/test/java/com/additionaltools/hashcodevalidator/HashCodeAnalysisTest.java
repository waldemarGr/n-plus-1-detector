package com.additionaltools.hashcodevalidator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@EnableHashCodeAnalysis
@SpringBootTest(classes = {HashCodeAnalysisConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HashCodeAnalysisTest {

    @Autowired
    private HashCodeAnalysis hashCodeAnalysis;


    @Test
    void testLogOutput() {
        System.out.println("check logs");
    }


}