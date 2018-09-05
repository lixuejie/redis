package com.lxj.redis;

import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

public class ConnectionFactory extends JedisConnectionFactory {

    public ConnectionFactory(JedisPoolConfig poolConfig) {
        super(poolConfig);
    }

    public ConnectionFactory(RedisSentinelConfiguration sentinelConfig, JedisPoolConfig poolConfig) {
        super(sentinelConfig, poolConfig);
    }
    
    public ConnectionFactory(RedisClusterConfiguration clusterConfig, JedisPoolConfig poolConfig) {
    	super(clusterConfig, poolConfig);
	}
    
    @Override
    public Jedis fetchJedisConnector() {
        Jedis jedis = super.fetchJedisConnector();
        jedis.select(super.getDatabase());
        return jedis;
    }

}
