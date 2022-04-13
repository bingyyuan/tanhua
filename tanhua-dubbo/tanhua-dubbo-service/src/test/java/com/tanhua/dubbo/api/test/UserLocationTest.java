package com.tanhua.dubbo.api.test;

import com.tanhua.dubbo.api.UserLocationApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserLocationTest {

    @Autowired
    private UserLocationApi userLocationApi;

    @Test
    public void addLocation(){
        userLocationApi.add(1l ,22.582111,113.929778,"深圳黑马程序员");
        userLocationApi.add(2l ,22.587995,113.925528,"红荔村肠粉");
        userLocationApi.add(3l ,22.562578,113.93814 ,"深圳南头直升机场");
        userLocationApi.add(4l ,22.549528,114.064478,"深圳市政府");
        userLocationApi.add(5l ,22.547726,113.986074,"欢乐谷");
        userLocationApi.add(6l ,22.540746,113.979399,"世界之窗");
        userLocationApi.add(7l ,22.632275,114.294924,"东部华侨城");
        userLocationApi.add(8l ,22.598196,114.314011,"大梅沙海滨公园");
        userLocationApi.add(9l ,22.638172,113.821705,"深圳宝安国际机场");
        userLocationApi.add(10l,22.566223,113.912386,"海雅缤纷城(宝安店)");
    }
}