package com.deppon.foss.framework.cache.storage;

import java.util.Map;

import com.deppon.foss.framework.cache.Context;

/**
 * 数据存储接口
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-17 上午11:18:38,content:TODO </p>
 * @author ningyu
 * @date 2013-4-17 上午11:18:38
 * @since
 * @version
 */
public interface ICacheStorage<K, V> {
    
    /**
     * 将value关联到key
     * 
     * @param k
     * @param v
     * @return boolean 是否执行成功
     */
    boolean set(K key, V value);
    
    /**
     * 将value关联到key,并将key的生存时间设置为expire（以秒为单位）
     * 
     * @author ningyu
     * @date 2013-4-17 下午3:54:38
     * @param key
     * @param value
     * @param expire
     * @return
     * @see
     */
    boolean set(K key, V value, int expire);
    
    /**
     * 返回key所关联的value
     * 
     * @param key 缓存Key
     * @return 缓存Value
     */
    V get(K key);
    
    /**
     * 删除给定key
     * 
     * @param key 缓存Key
     */
    void remove(K key);
    
    /**
     * 删除给定的一个活多个key
     * 
     * @author ningyu
     * @date 2013-4-9 下午3:52:37
     * @param keys 动态参数 数组[]
     * @see
     */
    void removeMulti(K ... keys);

    /**
     * 将map放入缓存，使用map中的key作为键，value做为值
     * 
     * @author ningyu
     * @date 2013-4-17 下午3:54:51
     * @param values
     * @return
     * @see
     */
    boolean set(Map<K, V> values);
    
    /**
     * 将map放入缓存，使用map中的key作为键，value做为值，map中每个key的生存时间为expire（以秒为单位）
     * 
     * @author ningyu
     * @date 2013-4-17 下午3:54:58
     * @param values
     * @param expire
     * @return
     * @see
     */
    boolean set(Map<K, V> values, int expire);
    
    /**
     * 返回同类缓存下所有数据
     *  
     * @author ningyu
     * @date 2013-4-18 下午3:27:36
     * @param context
     * @return
     * @see
     */
    Map<K, V> get(Context context);
    
    /**
     * 清除同类缓存类型下所有数据
     *  
     * @author ningyu
     * @date 2013-4-18 下午3:27:39
     * @param context
     * @see
     */
    void clear(Context context);
    
    /**
     * 查询key的生存时间
     * 
     * @author ningyu
     * @date 2013-5-2 上午11:53:22
     * @param key
     * @return
     * @see
     */
    long ttl(K key);
    
}
