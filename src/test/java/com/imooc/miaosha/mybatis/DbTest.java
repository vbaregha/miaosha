package com.imooc.miaosha.mybatis;

import com.imooc.miaosha.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbTest {

    @Autowired
    UserService userService;

    @Test
    public void getUserById() throws Exception{
        int id = 1;
        User user = userService.getById(id);
        System.out.println(user);

    }
}
