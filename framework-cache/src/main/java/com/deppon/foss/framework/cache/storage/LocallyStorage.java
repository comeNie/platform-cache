package com.deppon.foss.framework.cache.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.deppon.foss.framework.cache.exception.CacheConfigException;
import com.deppon.foss.framework.cache.Context;
import com.deppon.foss.framework.cache.exception.KeyIsNotFoundException;
import com.deppon.foss.framework.cache.exception.ValueIsNullException;

/**
 * 本地式存储
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-19 下午1:38:11,content:TODO </p>
 * @author ningyu
 * @date 2013-4-19 下午1:38:11
 * @since
 * @version
 */
public class LocallyStorage<V> implements ICacheStorage<String, V> {
    
    /**
     * 日志
     */
    private static final Log LOG = LogFactory.getLog(LocallyStorage.class);

    /**
     * 存储的map
     */
    private LocallyHashMap<String, V> map;
    
    /**
     * 线程安全lock对象
     */
    final Object mutex;
    
    /**
     * 最大容量，默认值：10000
     */
    private int maxCapacity = 10000;
    
    /**
     * 通过指定初始化容量初始化缓存存储器
     * @author ningyu
     * @date 2013-4-22 下午5:34:22
     * @param maxCapacity
     */
    public LocallyStorage(int maxCapacity) {
        if(maxCapacity <= 0) throw new CacheConfigException("初始化缓存容量不能小于等于0");
        this.maxCapacity = maxCapacity;
        this.map = new LocallyHashMap<String, V>(maxCapacity);
        mutex = this;
    }

    /**
     * 默认
     * @author ningyu
     * @date 2013-4-22 下午5:34:39
     */
    public LocallyStorage() {
        this.map = new LocallyHashMap<String, V>(maxCapacity);
        mutex = this;
    }

    /**
     * 设置最大的缓存存储器初始化容量,默认10000
     * @author ningyu
     * @date 2013-4-22 下午5:34:55
     * @param maxCapacity
     * @see
     */
    public void setMaxCapacity(int maxCapacity) {
        if(maxCapacity <= 0) throw new CacheConfigException("初始化缓存容量不能小于等于0");
        this.maxCapacity = maxCapacity;
    }

    /** 
     * 将value关联到key
     * @author ningyu
     * @date 2013-4-22 下午5:44:02
     * @param key
     * @param value 
     * @return 是否成功
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#set(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean set(String key, V value) {
        synchronized (mutex) {
            return map.put(key, value) != null;
        }
    }

    /** 
     * 将value关联到key,并将key的生存时间设置为expire（以秒为单位）
     * @author ningyu
     * @date 2013-4-22 下午5:44:20
     * @param key
     * @param value
     * @param expire
     * @return 是否成功
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#set(java.lang.Object, java.lang.Object, int)
     */
    @Override
    public boolean set(String key, V value, int expire) {
        synchronized (mutex) {
            return map.put(key, value, expire) != null;
        }
    }

    /** 
     * 返回key所关联的value
     * @author ningyu
     * @date 2013-4-22 下午5:44:28
     * @param key
     * @return V类型的对象
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#get(java.lang.Object)
     */
    @Override
    public V get(String key) {
        synchronized (mutex) {
            if(map.containsKey(key)) {
                V value = map.get(key);
                if(value == null) {
                    //key存在，value为null
                    throw new ValueIsNullException("key exists, value is null!");
                }
                return value;
            } else {
                //key不存在
                throw new KeyIsNotFoundException("key is not found!"); 
            }
        }
    }

    /** 
     * 删除给定key
     * @author ningyu
     * @date 2013-4-22 下午5:44:36
     * @param key 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#remove(java.lang.Object)
     */
    @Override
    public void remove(String key) {
        synchronized (mutex) {
            map.remove(key);
        }
    }

    /** 
     * 删除给定的一个活多个key
     * @author ningyu
     * @date 2013-4-22 下午5:44:44
     * @param keys 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#removeMulti(K[])
     */
    @Override
    public void removeMulti(String... keys) {
        synchronized (mutex) {
            for(int i=0;i<keys.length;i++) {
                map.remove(keys[i]);
            }
        }
    }

    /** 
     * 将map放入缓存，使用map中的key作为键，value做为值
     * @author ningyu
     * @date 2013-4-22 下午5:44:52
     * @param values
     * @return 是否成功
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#set(java.util.Map)
     */
    @Override
    public boolean set(Map<String, V> values) {
        synchronized (mutex) {
            try {
                map.putAll(values);
            } catch (RuntimeException e) {
                LOG.error("放入缓存失败!", e);
                return false;
            }
            return true;
        }
    }

    /** 
     * 将map放入缓存，使用map中的key作为键，value做为值，map中每个key的生存时间为expire（以秒为单位）
     * @author ningyu
     * @date 2013-4-22 下午5:45:00
     * @param values
     * @param expire
     * @return 是否成功
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#set(java.util.Map, int)
     */
    @Override
    public boolean set(Map<String, V> values, int expire) {
        synchronized (mutex) {
            try {
                map.putAll(values, expire);
            } catch (RuntimeException e) {
                LOG.error("放入缓存失败!", e);
                return false;
            }
            return true;
        }
    }
    
    /**
     * 区分大小写
     * 
     * @author ningyu
     * @date 2013-4-18 下午5:54:48
     * @param target
     * @param regEx
     * @return 是否成功
     * @see
     */
    private boolean matcher(String target,String regEx) {
        Pattern p=Pattern.compile(regEx);
        Matcher m=p.matcher(target);
        return m.find();
    }

    /** 
     * 返回同类缓存下所有数据
     * @author ningyu
     * @date 2013-4-22 下午5:46:09
     * @param context
     * @return 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#get(com.deppon.foss.framework.xcache.Context)
     */
    @Override
    public Map<String, V> get(Context context) {
        Map<String, V> result = new HashMap<String, V>();
        synchronized (mutex) {
            for(Map.Entry<String, V> entry : map.entrySet()) {
                if(matcher(entry.getKey(),context.getCacheId())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
    }
    
    /** 
     * 清除同类缓存类型下所有数据
     * @author ningyu
     * @date 2013-4-22 下午5:46:19
     * @param context 
     * @see com.deppon.foss.framework.cache.storage.ICacheStorage#clear(com.deppon.foss.framework.xcache.Context)
     */
    @Override
    public void clear(Context context) {
        List<String> list = new ArrayList<String>(); 
        synchronized(mutex) {
            for(Map.Entry<String, V> entry : map.entrySet()) {
                if(matcher(entry.getKey(),context.getCacheId())) {
                    list.add(entry.getKey());
                }
            }
        }
        if(list.size() > 0) {
            removeMulti(list.toArray(new String[list.size()]));
        }
    }

//    public int size() {
//        synchronized (mutex) {
//            return map.size();
//        }
//    }
    
    /**
     * 查询key的生存时间
     * @author ningyu
     * @date 2013-4-22 下午5:46:25
     * @param key
     * @return
     * @see
     */
    @Override
    public long ttl(String key) {
        synchronized (mutex) {
            return map.ttl(key);
        }
    }

}
