package com.deppon.foss.framework.cache;

import com.deppon.foss.framework.exception.BusinessException;


/**
 * 回调接口
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-19 下午1:37:28,content:TODO </p>
 * @author ningyu
 * @date 2013-4-19 下午1:37:28
 * @since
 * @version
 */
public interface CacheCallBack<K, V> {
    
    /**
     * 获取数据，给缓存使用
     * 
     * @param key
     * @return
     * @return V
     * @since:
     */
    V doGet(K k) throws BusinessException;
    
    /**
     * 初始化缓存数据
     * 当cache的lazy为false时调用
     * 
     * @author ningyu
     * @date 2013-4-17 下午3:21:55
     * @return
     * @see
     */
    ResultMap<K,V> doInitialization(IKeyGenerator<K> generator) throws BusinessException;
    
    /**
     * 扩展自定义构造key结构
     * 
     * @author ningyu
     * @date 2013-5-2 上午10:52:11
     * @param key
     * @return
     * @see
     */
    K doGenerate(K key);
}
