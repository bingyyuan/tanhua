package com.tanhua.server.test.cache;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.server.TanhuaServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TanhuaServerApplication.class)
public class CacheTest {

    @Autowired
    private TestUserInfoService userInfoService;

    @Test
    public void testFindAll() {
        // 第一次从数据库查询
        // 第二次从redis中取
        List<UserInfo> list = userInfoService.findAll();
        for (UserInfo info : list) {
            System.out.println(info);
        }
    }

    @Test
    public  void testSave() {
        // 先查询所有, 先有缓存
        // 再来跑这个测试用例
        UserInfo userInfo = new UserInfo();
        // 执行后会删除缓存
        userInfoService.save(userInfo);
    }

    /**
     * 第一次从数据库查询
     * 第二次从redis中取
     */
    @Test
    public  void testFind() {
        UserInfo info = userInfoService.findById(2l);
        System.out.println(info);
    }

    /**
     * 先testFind
     * 再执行
     */
    @Test
    public  void testUpdate() {
        UserInfo info = new UserInfo();
        info.setId(1l);
        info.setNickname("def");
        userInfoService.update(info);
    }
}