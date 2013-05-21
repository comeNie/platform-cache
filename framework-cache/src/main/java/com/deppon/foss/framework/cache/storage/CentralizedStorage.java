package com.deppon.foss.framework.cache.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

import com.deppon.foss.framework.cache.CacheUtils;
import com.deppon.foss.framework.cache.Context;
import com.deppon.foss.framework.cache.exception.KeyIsNotFoundException;
import com.deppon.foss.framework.cache.exception.RedisCacheStorageException;
import com.deppon.foss.framework.cache.exception.RedisConnectionException;
import com.deppon.foss.framework.cache.exception.ValueIsBlankException;
import com.deppon.foss.framework.cache.exception.ValueIsNullException;
import com.deppon.foss.framework.cache.redis.RedisClient;

/**
 * 集中式存储
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-19 下午1:37:58,content:TODO </p>
 * @author ningyu
 * @date 2013-4-19 下午1:37:58
 * @since
 * @version
 */
public class CentralizedStorage<V> implements ICacheStorage<String, V> {
    
    /**
     * redis client
     */
    private RedisClient client;

    /**
     * 日志
     */
    private static final Log LOG = LogFactory.getLog(CentralizedStorage.class);


    /**
     * 设置redis client
     * @author ningyu
     * @date 2013-4-22 下午5:37:10
     * @param client
     * @see
     */
    public void setClient(RedisClient client) {
        this.client = client;
    }


    /** 
     * <p>存入数据，默认时效：3600 * 24</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:24:47
     * @param key
     * @param value 
     * @return boolean 是否执行成功
     * @see com.deppon.foss.framework.cache.IRemoteCacheStore#set(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean set(String key, V value) {
    	if(key == null) {
            throw new RedisCacheStorageException("key does not allow for null!");
        }
        if(!client.getPoolInited()) {
            return false;
        }
        Jedis j = null;
        String svalue = CacheUtils.toJsonString(value);
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            j.set(key, svalue);
            return true;
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
        } finally {
            if (borrowOrOprSuccess) {
                //返回到资源池
                client.returnResource(j);
            }
        }
        return false;
    }

    
    /** 
     * <p>存入有时效的数据</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:23:59
     * @param key
     * @param value
     * @param expire 
     * @return boolean 是否执行成功
     * @see com.deppon.foss.framework.cache.IRemoteCacheStore#set(java.lang.Object, java.lang.Object, int)
     */
    @Override
    public boolean set(String key, V value, int expire) {
    	if(key == null) {
            throw new RedisCacheStorageException("key does not allow for null!");
        }
    	if(!client.getPoolInited()) {
            return false;
        }
        Jedis j = null;
        String svalue = CacheUtils.toJsonString(value);
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            j.setex(key, expire, svalue);
            return true;
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
        } finally {
            if (borrowOrOprSuccess) {
                //返回到资源池
                client.returnResource(j);
            }
        }
        return false;
    }

    /** 
     * <p>获取key对应的数据</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:23:29
     * @param key
     * @return 
     * @see com.deppon.foss.framework.cache.IRemoteCacheStore#get(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(String key) {
    	if(key == null) {
            throw new RedisCacheStorageException("key does not allow for null!");
        }
        if(!client.getPoolInited()) {
            throw new RedisConnectionException("jedis pool is not init!");
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            String re = j.get(key);
            //返回到资源池
            client.returnResource(j);
            if(re != null) {
                if(StringUtils.isBlank(re)) {
                    //key存在，value为空串
                    throw new ValueIsBlankException("key exists, value is blank!");
                } else if(StringUtils.equalsIgnoreCase(re, "nil")) {
                    //key不存在
                    throw new KeyIsNotFoundException("key is not found!");
                } else if(StringUtils.equalsIgnoreCase(re, "null")) {
                    //key存在，value为null
                    throw new ValueIsNullException("key exists, value is null!");
                } else {
                    return (V) CacheUtils.jsonParseObject(re);
                }
            } else {
                //key不存在
                throw new KeyIsNotFoundException("key is not found!");
            }
        } catch (JedisException e) {
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
            throw new RedisConnectionException(e);
        } finally {
            if (borrowOrOprSuccess) {
                client.returnResource(j);//返回到资源池
            }
        }
    }

    /** 
     * <p>删除指定的缓存信息</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:23:17
     * @param key 
     * @see com.deppon.foss.framework.cache.IRemoteCacheStore#remove(java.lang.Object)
     */
    @Override
    public void remove(String key) {
    	if(key == null) {
            throw new RedisCacheStorageException("key does not allow for null!");
        }
        if(!client.getPoolInited()) {
            return;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            j.del(key);
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
        } finally {
            if (borrowOrOprSuccess) {
                client.returnResource(j);//返回到资源池
            }
        }
    }
    
    /** 
     * <p>删除多个key的缓存信息</p> 
     * @author ningyu
     * @date 2013-4-9 下午3:54:48
     * @param keys 
     * @see com.deppon.foss.framework.cache.IRemoteCacheStore#removeMulti(K[])
     */
    @Override
    public void removeMulti(String... keys) {
    	if(keys == null) {
            throw new RedisCacheStorageException("keys does not allow for null!");
        }
    	if(keys.length <= 0) {
    		return;
    	}
        if(!client.getPoolInited()) {
            return;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            j.del(keys);
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
        } finally {
            if (borrowOrOprSuccess) {
                client.returnResource(j);//返回到资源池
            }
        }
    }

    /** 
     * 将map放入缓存，使用map中的key作为键，value做为值
     * 
     * @author ningyu
     * @date 2013-5-7 下午1:44:42
     * @param values
     * @return 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#set(java.util.Map)
     */
    @Override
    public boolean set(Map<String, V> values) {
        return processSet(values,-1);
    }
    
    /** 
     * 将map放入缓存，使用map中的key作为键，value做为值，map中每个key的生存时间为expire（以秒为单位）
     * 
     * @author ningyu
     * @date 2013-5-7 下午1:44:55
     * @param values
     * @param expire
     * @return 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#set(java.util.Map, int)
     */
    @Override
    public boolean set(Map<String, V> values, int expire) {
        return processSet(values,expire);
    }
    
    /**
     * 处理set动作
     * 
     * @author ningyu
     * @date 2013-5-7 下午1:45:04
     * @param values
     * @param expire
     * @return
     * @see
     */
    private boolean processSet(Map<String, V> values,int expire) {
    	if(values == null) {
            throw new RedisCacheStorageException("values does not allow for null!");
        }
    	if(values.isEmpty()) {
        	return false;
        }
        if(!client.getPoolInited()) {
            return false;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            Pipeline p = j.pipelined();
            for(Map.Entry<String, V> entry : values.entrySet()) {
                if(StringUtils.isNotBlank(entry.getKey())) {
                    String svalue = CacheUtils.toJsonString(entry.getValue());
                    if(expire > 0) {
                        p.setex(entry.getKey(), expire, svalue);   
                    } else {
                        p.set(entry.getKey(), svalue);
                    }
                }
            }
            p.sync();
            return true;
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
        } finally {
            if (borrowOrOprSuccess) {
                //返回到资源池
                client.returnResource(j);
            }
        }
        return false;
    }

    /** 
     * 获取这个缓存下的所有数据
     * 如果是一个大的数据库中使用它可能造成性能问题
     * 
     * @author ningyu
     * @date 2013-4-18 下午3:03:43
     * @param context
     * @return 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#get(com.deppon.foss.framework.xcache.Context)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Map<String, V> get(Context context) {
    	if(context == null) {
    		return null;
    	}
        if(!client.getPoolInited()) {
            return null;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        Map<String,V> result = null;
        try {
            j = client.getResource();
            Set<String> keySet = j.keys(context.getCacheId());
            if(keySet == null || keySet.size() < 0) {
                return null;
            }
            int length = keySet.size();
            String[] keys = keySet.toArray(new String[length]);
            result = new HashMap<String,V>();
            List<String> values = j.mget(keys);
            for(int i=0;i<length;i++) {
                String v = values.get(i);
                if(!"nil".equals(v)) {
                    result.put(keys[i], (V) CacheUtils.jsonParseObject(v));
                } else {
                    result.put(keys[i], null);
                }
            }
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
            return null;
        } finally {
            if (borrowOrOprSuccess) {
                //返回到资源池
                client.returnResource(j);
            }
        }
        return result;
    }


    /** 
     * 清除同类缓存类型下所有数据
     * 
     * @author ningyu
     * @date 2013-5-7 下午1:48:08
     * @param context 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#clear(com.deppon.foss.framework.cache.Context)
     */
    @Override
    public void clear(Context context) {
    	if(context == null) {
    		return;
    	}
        if(!client.getPoolInited()) {
            return;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            Set<String> keySet = j.keys(context.getCacheId());
            if(keySet == null || keySet.size() < 0) {
                return;
            }
            int length = keySet.size();
            String[] keys = keySet.toArray(new String[length]);
            j.del(keys);
        } catch (JedisException e) {
            borrowOrOprSuccess = false;
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
            return;
        } finally {
            if (borrowOrOprSuccess) {
                //返回到资源池
                client.returnResource(j);
            }
        }
    }
    
    /**
     * 查询key的生存时间
     * @author ningyu
     * @date 2013-4-22 下午5:46:25
     * @param key
     * @return
     * @see
     */
    public long ttl(String key) {
    	if(key == null) {
            return -1;
        }
    	if(!client.getPoolInited()) {
            return -1;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = client.getResource();
            Long re = j.ttl(key);
            //返回到资源池
            client.returnResource(j);
            return re;
        } catch (JedisException e) {
            if (j != null) {
                client.returnBrokenResource(j);
            }
            LOG.error(e.getMessage(),e);
            return -1;
        } finally {
            if (borrowOrOprSuccess) {
                client.returnResource(j);//返回到资源池
            }
        }
    }
}
