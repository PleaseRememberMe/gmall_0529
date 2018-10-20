package com.atguigu.gmall.manager;

import com.atguigu.gmall.manager.mapper.BaseCatalog2Mapper;
import com.atguigu.gmall.manager.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerServiceApplicationTests {


	@Autowired
	CatalogService catalogService;

	@Autowired
	UserMapper userMapper;


	@Autowired
	BaseCatalog2Mapper baseCatalog2Mapper;

	@Autowired
	BaseAttrInfoService baseAttrInfoService;


	@Autowired
	StringRedisTemplate stringRedisTemplate; //k_v都是string

	@Autowired
	JedisPool jedisPool;

	@Test
	public void testJedisPool(){
		Jedis resource = jedisPool.getResource();
		resource.set("sdf","fsfsdf");
	}




	@Test
	public void testRedisTemplate(){
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		opsForValue.set("hello","stit");
		System.out.println("6666");
		String hello = opsForValue.get("hello");
		System.out.println("得到的值是;"+hello);
	}


	@Test
	public void testBaseAttrInfoService(){
		List<BaseAttrInfo> b = baseAttrInfoService.getBaseAttrInfoByCatalog3Id(1);
		log.info("平台属性是：{}",b);
		List<BaseAttrValue> c = baseAttrInfoService.getBaseAttrValueByAttrId(1);
		log.info("属性的属性值是，{}",c);
	}


	@Test
	public void testCatalogService(){

		List<BaseCatalog1> allBaseCatalog1 = catalogService.getAllBaseCatalog1();

		log.info("一级分类是：{}",allBaseCatalog1);

		List<BaseCatalog2> allBaseCatalog2ByC1Id = catalogService.getAllBaseCatalog2ByC1Id(allBaseCatalog1.get(0).getId());

		log .info("{} 一级分类的二级分类是，{}",allBaseCatalog1.get(0).getId(),allBaseCatalog2ByC1Id);

		List<BaseCatalog3> allBaseCatalog3ByC2Id = catalogService.getAllBaseCatalog3ByC2Id(allBaseCatalog2ByC1Id.get(0).getId());

		log.info("{} 二级分类的三级分类是，{}",allBaseCatalog2ByC1Id.get(0).getId(),allBaseCatalog3ByC2Id);
	}


	@Test
	public void test2(){
		BaseCatalog2 b = new BaseCatalog2();
		b.setName("ezj");
		baseCatalog2Mapper.insert(b);
		log.info("成功。。。。。id是{}，name是{}",b.getCatalog1Id(),b.getName());
	}

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
