package com.additionaltools.relationship2;

import com.additionaltools.relationship.EnableRelationshipAnalysis;
import com.additionaltools.relationship.EntityFieldOptimizationInfo;
import com.additionaltools.relationship.RelationshipAnalysis;
import com.additionaltools.relationship.RelationshipAnalysisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Set;

@EnableRelationshipAnalysis
@SpringBootTest(classes = {RelationshipAnalysisConfiguration.class})
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RelationshipAnalysisTest {

    @Autowired
    private RelationshipAnalysis relationshipAnalysis;


    @Test
    void testOutput() throws IOException {
        Set<EntityFieldOptimizationInfo> entityFieldOptimizationInfos = relationshipAnalysis.collectEntityOptimizationData();
    }
}