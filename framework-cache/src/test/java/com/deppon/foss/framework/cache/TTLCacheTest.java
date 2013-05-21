package com.deppon.foss.framework.cache;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import com.deppon.foss.framework.cache.exception.RedisConnectionException;
import com.deppon.foss.framework.cache.redis.RedisClient;
import com.deppon.foss.framework.cache.storage.CentralizedStorage;

public class TTLCacheTest {

    private static TTLCache cache = new TTLCache();
    private static CentralizedStorage<TTLEntity> storage = new CentralizedStorage<TTLEntity>();
    private static RedisClient client = new RedisClient();
    
    static {
        try {
            client.setHost1("192.168.17.167");
            client.setPort1(6379);
            client.setHost2("192.168.17.167");
            client.setPort2(6380);
            client.afterPropertiesSet();
            storage.setClient(client);
            cache.setStorage(storage);
            cache.setTimeOut(50);
            cache.afterPropertiesSet();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void test111() {
    	TTLEntity ttl = cache.get("111");
    	System.out.println(ttl.getId());
    }
    
//    @Test
    public void testJson() {
        Object obj = storage.get("com.deppon.foss.framework.entity.IUser_21ff1a74-6822-4070-8625-ebe50ec4bebf14");
        System.out.println(obj);
    }
//    @Before
    public void init() {
    }
    
//    @Test
    public void test() {
//        System.out.println(System.currentTimeMillis());
//        for(int i=0;i<100000;i++) {
//            storage.set("com.deppon.foss.framework.entity.IUser"+i, "com.deppon.foss.framework.entity.IUser"+i, 8000);
//        }
//        System.out.println(System.currentTimeMillis());
    }
    
//    @Test
    public void gettest() {
        System.out.println(System.currentTimeMillis());
        for(int i=0;i<100000;i++) {
            System.out.println(storage.get("com.deppon.foss.framework.entity.IUser"+i));
        }
        System.out.println(System.currentTimeMillis());
    }
    
//    @Test
    public void gesttest1() {
//        Jedis j = new Jedis("192.168.17.167", 6379);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxActive(1024);
        poolConfig.setMaxIdle(200);
        poolConfig.setMaxWait(1000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        JedisPool pool = new JedisPool(poolConfig, "192.168.17.167", 6379, 1000, null);
        for(int i=0;i<100000;i++) {
            Jedis j = null;
            try {
                j = pool.getResource();
                String re = j.get("com.deppon.foss.framework.entity.IUser"+i);
                pool.returnResource(j);
//                String re = j.get("com.deppon.foss.framework.entity.IUser"+i);
                //返回到资源池
//                client.returnResource(j);
//                if(re != null) {
//                    if(StringUtils.isBlank(re)) {
//                        //key存在，value为空串
//                        throw new ValueIsBlankException("key exists, value is blank!");
//                    } else if(StringUtils.equalsIgnoreCase(re, "nil")) {
//                        //key不存在
//                        throw new KeyIsNotFoundException("key is not found!");
//                    } else if(StringUtils.equalsIgnoreCase(re, "null")) {
//                        //key存在，value为null
//                        throw new ValueIsNullException("key exists, value is null!");
//                    } else {
//                        System.out.println(re);
//                    }
//                } else {
//                    //key不存在
//                    throw new KeyIsNotFoundException("key is not found!");
//                }
            } catch (JedisException e) {
//                if (j != null) {
//                    client.returnBrokenResource(j);
//                }
                throw new RedisConnectionException(e);
//            client.handoverToSlave();
//            return get(key);
            }
        }
        
    }
    
//    @Test
    public void test1() {
        String[] str = new String[100000];
        for(int i=0;i<100000;i++) {
            str[i] = "com.deppon.foss.framework.entity.IUser"+i;
//            System.out.println(i);
//            storage.remove("userId"+i);
        }
        System.out.println("100000个key进行删除操作开始:"+System.currentTimeMillis());
        if(!client.getPoolInited()) {
            return;
        }
        Jedis j = null;
        String[] skeys = new String[str.length];
        System.out.println("开始key json序列化:"+System.currentTimeMillis());
        for(int i = 0;i<str.length;i++) {
            skeys[i] = CacheUtils.toJsonString(str[i]);
        }
        System.out.println("key json序列化完毕:"+System.currentTimeMillis());
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            System.out.println("开始删除:"+System.currentTimeMillis());
            j.del(skeys);
            System.out.println("删除完毕:"+System.currentTimeMillis());
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
//          client.handoverToSlave();
//          remove(key);
        } finally {
            if (borrowOrOprSuccess) {
                client.returnResource(j);//返回到资源池
            }
        }
        
//        System.out.println(str.length + str.toString());
    }
    
//    @Test
    public void getSet() {
        TTLEntity entity = cache.get("ttlCacheTest");
        Assert.assertEquals("成功", entity.getId(), "ttlCacheTest");
    }
    
//    @Test(expected=RuntimeException.class)
    public void getAll() {
        Map<String,TTLEntity> map = cache.get();
    }
    
//    @Test
    public void invalid() {
        TTLEntity entity = cache.get("ttlCacheTest");
        TTLEntity entity1 = cache.get("ttlCacheTest");
        Assert.assertEquals("success", entity.name, entity1.name);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cache.invalid("ttlCacheTest");
    }
    
//    @Test
    public void getValueIsNull() {
//        String key = "valueisnull";
//        key = cache.getUUID() + "_" + key;
//        storage.set(key, null, 50);
//        TTLEntity entity = cache.get("valueisnull");
//        Assert.assertNull(entity);
//        cache.invalid("valueisnull");
    }
    
//    @Test
    public void getValueIsBlank() {
//        String key = "valueisnull";
//        key = cache.getUUID() + "_" + key;
//        storage.set(key, "", 50);
//        TTLEntity entity = cache.get("valueisnull");
//        Assert.assertNull(entity);
//        cache.invalid("valueisnull");
    }
}
