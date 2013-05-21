package com.deppon.foss.framework.cache;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.deppon.foss.framework.cache.exception.KeyIsNotFoundException;
import com.deppon.foss.framework.cache.exception.RedisConnectionException;
import com.deppon.foss.framework.cache.exception.ValueIsBlankException;
import com.deppon.foss.framework.cache.exception.ValueIsNullException;
import com.deppon.foss.framework.cache.storage.ICacheStorage;
import com.deppon.foss.framework.cache.storage.LocallyStorage;
import com.deppon.foss.framework.exception.BusinessException;

/**
 * 提供缓存服务的基类
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-19 上午10:04:57,content:TODO </p>
 * @author ningyu
 * @date 2013-4-19 上午10:04:57
 * @since
 * @version
 */
public abstract class CacheSupport<V> extends BaseCache<String, V> implements CacheCallBack<String, V>, IKeyGenerator<String> {
    
    private static final Log LOG = LogFactory.getLog(CacheSupport.class);
    
    private ICacheStorage<String, V> storage = null;
    
    /**
     * 超时时间,单位秒
     */
    private int timeOut = 30 * 60 * 1000;
    
    /**
     * 是否按照时间控制标识
     */
    private boolean enableTimeOut = true;
    
    /**
     * 设置数据超时时间，默认值：30分钟
     * 
     * @author ningyu
     * @date 2013-4-17 上午11:26:47
     * @param timeOut 超时时间，单位：秒，设置为0,不按照时间控制
     * @see
     */
    public void setTimeOut(int timeOut) {
        enableTimeOut = timeOut > 0 ? true : false;
        this.timeOut = timeOut;
    }
    
    private boolean lazy = true;
    
    /**
     * 设置是否延迟加载缓存数据
     * @author ningyu
     * 
     * @date 2013-5-8 下午6:06:59
     * @param lazy is true 延迟加载：在用的时候去装载 
     * @param lazy is false 及时加载：在初始化缓存的时候装载，需要重写{@link CacheSupport#doInitialization(IKeyGenerator)}方法
     * @see
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    /**
     * 设置缓存存储器，参考：集中存储器{@link CentralizedStorage}和本地存储器{@link LocallyStorage}
     * @author ningyu
     * @date 2013-5-8 下午6:04:57
     * @param storage
     * @see
     */
    public void setStorage(ICacheStorage<String, V> storage) {
        this.storage = storage;
    }

    /**
     * 初始化方法
     * 
     * @author ningyu
     * @date 2013-4-17 上午11:14:19
     * @throws Exception
     * @see
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        
        super.afterPropertiesSet();
        
        CacheManager.getInstance().registerCacheProvider(this);
        
        if(!this.lazy) {
            ResultMap<String, V> cacheData = initialization();
            if(this.enableTimeOut) {
                this.storage.set(cacheData,timeOut);
            } else {
                this.storage.set(cacheData);
            }
        }
    }
    
    /** 
     * 获取缓存
     * 
     * @author ningyu
     * @date 2013-5-8 下午6:04:49
     * @param key
     * @return 
     * @see com.deppon.foss.framework.cache.ICache#get(java.lang.Object)
     */
    @Override
    public V get(String key) {
        if(StringUtils.isBlank(key)) {
            throw new RuntimeException("key不允许为null或空串!");
        }
        V value = null;
        String newKey = generate(key);
        try {
            value = storage.get(newKey);
        } catch(ValueIsBlankException e) {
            LOG.warn("缓存["+getCacheId()+"]，key["+key+"]存在，value为空串，返回结果[null]");
            //key存在，value为空串
            return null;
        } catch(ValueIsNullException ex) {
            //key存在，value为null
            LOG.warn("缓存["+getCacheId()+"]，key["+key+"]存在，value为null，返回结果[null]");
            return null;
        } catch(KeyIsNotFoundException ex1) {
            //key不存在
            value = execute(key);
            LOG.warn("缓存["+getCacheId()+"]，key["+key+"]不存在，走数据库查询，返回结果["+value+"]");
            if(enableTimeOut) {
                storage.set(newKey, value, timeOut);
            } else {
                storage.set(newKey, value);
            }
        } catch(RedisConnectionException exx) {
            //redis 连接出现异常
            value = execute(key);
            LOG.warn("redis连接出现异常，走数据库查询!");
            return value;
        }
        return value;
    }

    /** 
     * 一次性取出所有内容
     * 
     * @author ningyu
     * @date 2013-5-8 下午6:04:40
     * @return 
     * @see com.deppon.foss.framework.cache.ICache#get()
     */
    @Override
    public Map<String, V> get() {
        return storage.get(getContext());
    }
    /** 
     * 失效一组缓存 使旧的一组缓存全部失效 如果是LRU的在下一次使用会自动加载最新的 如果是Strong的会立即重新加载一次新的数据到缓存中
     * 
     * @author ningyu
     * @date 2013-5-8 下午6:04:33 
     * @see com.deppon.foss.framework.cache.ICache#invalid()
     */
    @Override
    public void invalid() {
        storage.clear(getContext());
    }

    /** 
     * 失效key对应的缓存 如果是LRU的会在下一次使用这个Key时自动加载最新的 如果是Strong的会Throws RuntimeException异常，不允许失效部分数据
     * 
     * @author ningyu
     * @date 2013-5-8 下午6:04:23
     * @param key 
     * @see com.deppon.foss.framework.cache.ICache#invalid(java.lang.Object)
     */
    @Override
    public void invalid(String key) {
        storage.remove(generate(key));
    }

    /** 
     * 失效传入的多个key对应的缓存
     * 
     * @author ningyu
     * @date 2013-5-8 下午6:04:13
     * @param keys 
     * @see com.deppon.foss.framework.cache.ICache#invalidMulti(K[])
     */
    @Override
    public void invalidMulti(String... keys) {
        String[] newKeys = new String[keys.length];
        for(int i=0;i<keys.length;i++) {
            newKeys[i] = generate(keys[i]);
        }
        storage.removeMulti(newKeys);
    }
    
    /** 
     * key构造时加上前缀，前缀采用{@link CacheSupport#getCacheId()}
     * @author ningyu
     * @date 2013-5-8 下午6:03:09
     * @param key
     * @return 
     * @see com.deppon.foss.framework.cache.IKeyGenerator#generate(java.lang.Object)
     */
    @Override
    public String generate(String key) {
        return this.getContext().getCacheId() + "_" + doGenerate(key);
    }
    
    /** 
     * CacheSupport默认实现key结构构造过程方法，用户可以重写这个方法达到自定义结构的效果
     * 
     * @author ningyu
     * @date 2013-5-2 上午10:58:19
     * @param key
     * @return 
     * @see com.deppon.foss.framework.cache.CacheCallBack#doGenerate(java.lang.Object)
     */
    @Override
    public String doGenerate(String key) {
        return key;
    }
    
    /**
     * 执行回调
     * 
     * @author ningyu
     * @date 2013-5-8 下午5:59:04
     * @param key
     * @return
     * @throws BusinessException
     * @see
     */
    private V execute(String key) throws BusinessException {
        return doGet(key);
    }
    
    /** 
     * 默认空实现初始化数据回调函数
     * 当{@link CacheSupport#lazy}=false时需要重写这个方法
     * 
     * @author ningyu
     * @date 2013-5-8 下午5:59:27
     * @param generator
     * @return
     * @throws BusinessException 
     * @see com.deppon.foss.framework.cache.CacheCallBack#doInitialization(com.deppon.foss.framework.cache.IKeyGenerator)
     */
    public ResultMap<String,V> doInitialization(IKeyGenerator<String> generator) throws BusinessException {
    	return null;
    }
    
    /**
     * 执行初始化数据
     * 
     * @author ningyu
     * @date 2013-5-8 下午6:02:43
     * @return
     * @throws BusinessException
     * @see
     */
    private ResultMap<String,V> initialization() throws BusinessException{
        return doInitialization(this);
    }
    
}
