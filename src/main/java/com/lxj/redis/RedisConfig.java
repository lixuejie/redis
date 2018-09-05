
package com.lxj.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisServer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfig {

	private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

	@Bean
	public JedisPoolConfig poolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(1024);
		poolConfig.setMaxIdle(8);
		poolConfig.setMaxWaitMillis(30000);
		poolConfig.setTestOnBorrow(true);
		return poolConfig;
	}

	@SuppressWarnings("deprecation")
	@Bean
	@DependsOn({ "poolConfig" })
	@Primary
	public ConnectionFactory writeFactory() {
		ConnectionFactory factory = null;
		if (null == masterName || "".equalsIgnoreCase(masterName)) {
			logger.info("Write factory Standalone mode...");
			factory = new ConnectionFactory(poolConfig());
			factory.setHostName(address);
			factory.setPort(Integer.parseInt((port)));
		} else {
			logger.info("Write factory Sentinel mode...");
			LinkedHashSet<String> set = new LinkedHashSet<String>(Arrays.asList(sentinels.split("\\|")));
			RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(masterName, set);
			factory = new ConnectionFactory(redisSentinelConfiguration, poolConfig());
		}
		factory.setPassword(password);
		factory.setTimeout(60000);
		factory.setUsePool(true);
		factory.setDatabase(Integer.parseInt(database));
		return factory;
	}

	@SuppressWarnings("deprecation")
	@Bean
	@DependsOn({ "writeFactory" })
	public ConnectionFactory readonlyFactory() {
		ConnectionFactory factory = null;
		if (null == masterName || "".equalsIgnoreCase(masterName)) {
			logger.info("Readonly factory Standalone mode...");
			factory = new ConnectionFactory(poolConfig());
			factory.setHostName(address);
			factory.setPort(Integer.parseInt((port)));
		} else {
			logger.info("Readonly factory Cluster mode...");
			Collection<RedisServer> masters = writeFactory().getSentinelConnection().masters();
			List<RedisNode> nodes = new ArrayList<>();
			for (RedisServer master : masters) {
				if (null != master.getRunId() && !"".equalsIgnoreCase(master.getRunId())) {
					nodes.add(master);
				}
			}
			Collection<RedisServer> slaves = writeFactory().getSentinelConnection().slaves(writeFactory().getSentinelConfiguration().getMaster());
			for (RedisServer slave : slaves) {
				if (null != slave.getRunId() && !"".equalsIgnoreCase(slave.getRunId())) {
					nodes.add(slave);
				}
			}
			RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
			clusterConfig.setClusterNodes(nodes);
			factory = new ConnectionFactory(clusterConfig, poolConfig());
		}
		factory.setPassword(password);
		factory.setTimeout(60000);
		factory.setUsePool(true);
		factory.setDatabase(Integer.parseInt(database));
		return factory;
	}

	private String address;

	private String port;

	private String database;

	private String masterName;

	private String sentinels;

	private String password;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public String getSentinels() {
		return sentinels;
	}

	public void setSentinels(String sentinels) {
		this.sentinels = sentinels;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
