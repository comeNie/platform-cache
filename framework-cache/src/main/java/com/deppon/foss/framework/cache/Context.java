package com.deppon.foss.framework.cache;

/**
 * 缓存上下文信息
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-19 下午1:40:03,content:TODO </p>
 * @author ningyu
 * @date 2013-4-19 下午1:40:03
 * @since
 * @version
 */
public class Context {

    /**
     * 缓存对象
     */
    private ICache<?, ?> cache;

    /**
     * 构造器
     * @author ningyu
     * @date 2013-4-22 下午5:55:11
     * @param cache
     */
    public Context(ICache<?, ?> cache) {
        this.cache = cache;
    }
    
    /**
     * 获取缓存id
     * @author ningyu
     * @date 2013-4-22 下午5:55:16
     * @return
     * @see
     */
    public String getCacheId() {
        return this.cache.getCacheId();
    }
    
}
