package com.lxj.redis;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTestApplicationTests {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisTestApplicationTests.class);
	
	@Resource(name = "writeFactory")
	private ConnectionFactory writeFactory;
	
	@Resource(name = "readonlyFactory")
	private ConnectionFactory readonlyFactory;
	
	@Test
	public void test() {
		Jedis read = null;
		Jedis write = null;
		try {
			read = readonlyFactory.fetchJedisConnector();
			write = writeFactory.fetchJedisConnector();
			
			logger.info("Before write: " + read.get("test"));
			
			write.set("test", "hello world");
			write.expire("test", 10);
			
			logger.info("After write: " + read.get("test"));
			
			for (int i = 0; i < 10; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						Jedis jedis = readonlyFactory.fetchJedisConnector();
						logger.info(jedis.getClient().getHost() + " get " + jedis.get("test"));
						jedis.close();
					}
				});

				t.start();
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			if (null != read) {
				read.close();
			}
			if (null != write) {
				write.close();
			}
		}
		
	}

}
