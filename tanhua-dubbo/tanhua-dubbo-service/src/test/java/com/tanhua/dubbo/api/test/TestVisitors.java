package com.tanhua.dubbo.api.test;

import com.tanhua.domain.mongo.Visitor;
import com.tanhua.dubbo.api.VisitorsApi;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestVisitors {

    @Autowired
    private VisitorsApi visitorsApi;

    @Test
    public void testSave(){
        for (int i = 0; i < 10; i++) {
            Visitor visitor = new Visitor();
            visitor.setFrom("首页");
            visitor.setUserId(10009l);//用户id
            visitor.setVisitorUserId(RandomUtils.nextLong(1,20));
            this.visitorsApi.save(visitor);
        }
        System.out.println("ok");
    }
}