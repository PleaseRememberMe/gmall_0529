package com.atguigu.gmall.manager;

import com.atguigu.gmall.manager.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerServiceApplicationTests {

	@Autowired
	UserMapper userMapper;


	@Test
	public void testDelete(){
		userMapper.deleteById(1L);
		System.out.println("success");
	}




	@Test
	public void contextLoads() {
		for (User user : userMapper.selectList(null)) {
			System.out.println(user);
		}
		;
		System.out.println("===========");
		User user = new User();
		user.setAge(18);
		//要想使xml生效，一定要在配置文件中让spring知道
		User u = userMapper.getUserByLL(user);
		System.out.println(u);
	}

}
