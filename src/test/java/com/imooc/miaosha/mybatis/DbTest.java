package com.imooc.miaosha.mybatis;

import com.imooc.miaosha.dao.UserDao;
import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.security.RunAs;

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
