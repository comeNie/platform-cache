package com.deppon.foss.framework.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存初始化的返回结果
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-18 上午8:47:06,content:TODO </p>
 * @author ningyu
 * @date 2013-4-18 上午8:47:06
 * @since
 * @version
 */
public class ResultMap<K, V> extends HashMap<K, V> {

    /**
     * TODO（用一句话描述这个变量表示什么）
     */
    private static final long serialVersionUID = 696427811135472911L;
    
    /**
     * key生成器对象
     */
    private IKeyGenerator<K> generator;

    /**
     * 构造器
     * @author ningyu
     * @date 2013-4-22 下午5:56:04
     * @param cache
     */
    public ResultMap(IKeyGenerator<K> generator) {
        super();
        this.generator = generator;
    }

    /** 
     * 设置value绑定到key
     * @author ningyu
     * @date 2013-4-22 下午5:56:18
     * @param key
     * @param value
     * @return 
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(K key, V value) {
        if (key != null) {
            return super.put(generator.generate(key), value);  
        } else {
            return super.put(key, value);
        }
    }

    @Override
	public V get(Object key) {
    	if (key != null) {
            return super.get(generator.generate((K)key));  
        } else {
            return super.get(key);
        }
	}

	/** 
     * 设置map中的values绑定到keys
     * @author ningyu
     * @date 2013-4-22 下午5:56:30
     * @param m 
     * @see java.util.HashMap#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
}
