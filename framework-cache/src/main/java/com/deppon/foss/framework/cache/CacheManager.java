package com.deppon.foss.framework.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.deppon.foss.framework.cache.exception.CacheDumplicateException;
import com.deppon.foss.framework.cache.exception.CacheNotFoundException;

/**
 * 缓存管理器
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-18 上午8:45:08,content:TODO </p>
 * @author ningyu
 * @date 2013-4-18 上午8:45:08
 * @since
 * @version
 */
public final class CacheManager {

	/**
	 * 自己
	 */
	private static final CacheManager INSTANCE = new CacheManager();

	/**
	 * 保存所有缓存实例
	 */
	private final Map<String, ICache> uuidCaches = new ConcurrentHashMap<String, ICache>();

	/**
	 *  禁止从外部拿到实例
	  * 创建一个新的实例 CacheManager.
	  * @since 0.6
	 */
	private CacheManager() {
	}

	public static CacheManager getInstance() {
		return INSTANCE;
	}

	/**
	 * 系统启动后自动注册所有的缓存类别
	 * 
	 * @param cache
	 */
	public <K,V> void registerCacheProvider(ICache<K, V> cache) {
		// 不允许UUID重复，应用必须在实现的Cache接口确保命名不重复
		String cacheId = cache.getCacheId();
		if (uuidCaches.containsKey(cacheId)) {
			throw new CacheDumplicateException("CacheId:[" + cacheId
					+ "] to Class:[" + cache.getClass().getName()
					+ "] and Class:["+ uuidCaches.get(cacheId).getClass().getName() +"]");
		}
		uuidCaches.put(cacheId, cache);
	}

	/**
	 * 根据uuid获取缓存实例
	 * getCache
	 * @param cacheId
	 * @return
	 * @return ICache<K,V>
	 * @since:
	 */
	public <K,V> ICache<K, V> getCache(String cacheId) {
		ICache<K, V> cache = uuidCaches.get(cacheId);
		if (cache == null) {
			throw new CacheNotFoundException("CacheId:["+cacheId+"]");
		}
		return cache;
	}
	
	/**
	 * 根据cacheId，目标Class获取缓存实例
	 * @author ningyu
	 * @date 2013-5-8 下午4:36:57
	 * @param t
	 * @param cacheId
	 * @return
	 * @see
	 */
	public <T> T getCache(Class t,String cacheId) {
		try {
			return (T) getCache(cacheId);
		} catch(ClassCastException e) {
			throw new CacheNotFoundException("CacheId:["+cacheId+"] to Class:["+t.getName()+"]");
		}
	}


	/**
	 * 销毁
	 * @author ningyu
	 * @date 2013-4-22 下午5:54:09
	 * @see
	 */
	public void shutdown() {
		uuidCaches.clear();
	}
}