package com.deppon.foss.framework.cache.storage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.Map.Entry;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 增加ttl和lru功能的的HashMap
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-19 上午9:58:20,content:TODO </p>
 * @author ningyu
 * @date 2013-4-19 上午9:58:20
 * @since
 * @version
 */
public class LocallyHashMap<K, V> extends LinkedHashMap<K, V> {
    
    /**
     * 日志
     */
    private static final Log LOG = LogFactory.getLog(LocallyHashMap.class);

    private static final long serialVersionUID = 2700836956407925412L;

    /**
     * 最大容量
     */
    private static int MAX_ENTRIES = 1000;

    /**
     * 加载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    /**
     * 时间map
     */
    private volatile Map<K, TimerTask> ttlMap;
    
    /**
     * 时间调度器
     */
    private Timer TTLTimer;

    public LocallyHashMap(int maxEntries) {
        super(maxEntries, DEFAULT_LOAD_FACTOR, true);
        MAX_ENTRIES = maxEntries;
        this.ttlMap = new HashMap<K, TimerTask>();
        TTLTimer = new Timer();
    }

    /** 
     * 当容量大于最大值时，返回true，删除Map.Entry对象
     * @author ningyu
     * @date 2013-4-22 下午5:38:38
     * @param eldest
     * @return 
     * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(Entry<K,V> eldest) {
        boolean b = size() > MAX_ENTRIES;
        if(b) {
            cancel(eldest.getKey());
        }
        return b;
    }

    /**
     * 将value绑定到key，并且设置key的生存时间
     * @author ningyu
     * @date 2013-4-22 下午5:41:42
     * @param key
     * @param value
     * @param expire
     * @return
     * @see
     */
    public V put(K key, V value, int expire) {
        V v = super.put(key, value);
        setExpire(key,expire);
        return v;
    }

    /**
     * 将map中的values绑定到对应的keys上，将map中所有的key设置生存时间
     * @author ningyu
     * @date 2013-4-22 下午5:42:20
     * @param values
     * @param expire
     * @see
     */
    public void putAll(Map<? extends K, ? extends V> values, int expire) {
        try {
            super.putAll(values);
            for(K key : values.keySet()) {
                setExpire(key,expire);
            }
        } catch (RuntimeException e) {
            LOG.error("放入缓存失败!", e);
        }
    }

    /** 
     * 删除这个key的value绑定
     * @author ningyu
     * @date 2013-4-22 下午5:43:15
     * @param key
     * @return 
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public V remove(Object key) {
        V v = super.remove(key);
        cancel(key);
        return v;
    }
    
    /**
     * 查询失效时间
     * @author ningyu
     * @date 2013-4-22 下午5:39:13
     * @param key
     * @return
     * @see
     */
    public long ttl(K key) {
        LOG.debug("计时器任务个数："+ttlMap.size());
        if(key != null) {
            TimerTask task = ttlMap.get(key);
            if(task != null) {
                task.scheduledExecutionTime();
                long currentTime = System.currentTimeMillis();
                long executionTime = task.scheduledExecutionTime();
                if(executionTime > currentTime) {
                    long ttl = ((executionTime - currentTime) / 1000);
                    return ttl <= 0 ? -1 : ttl;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }
    
    /**
     * 设置key的生存时间
     * @author ningyu
     * @date 2013-4-22 下午5:39:26
     * @param key
     * @param expire
     * @see
     */
    private void setExpire(K key,int expire) {
        if(key != null) {
            TimerTask task = ttlMap.get(key);
            if(task != null) {
                task.cancel();
                task = null;
            }
            task = new TTlTimerTask(key);
            TTLTimer.schedule(task, expire * 1000);
            ttlMap.put(key, task);
        }
    }
    
    /**
     * 取消传入key失效时间调度任务
     * @author ningyu
     * @date 2013-4-22 下午5:39:50
     * @param key
     * @see
     */
    private void cancel(Object key) {
        if(key != null) {
            TimerTask task = ttlMap.remove(key);
            if(task != null) {
                long currentTime = System.currentTimeMillis();
                long executionTime = task.scheduledExecutionTime();
                if (executionTime > currentTime) {
                    task.cancel();
                }
            }
        }
    }
    
    /**
     * key的生存时间任务
     * <p style="display:none">modifyRecord</p>
     * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-22 下午5:43:30,content:TODO </p>
     * @author ningyu
     * @date 2013-4-22 下午5:43:30
     * @since
     * @version
     */
    class TTlTimerTask extends TimerTask {

        private K key;
        
        public TTlTimerTask(K key) {
            super();
            this.key = key;
        }

        @Override
        public void run() {
            LOG.debug("["+this.key+"]已经失效");
            remove(this.key);
        }
        
    }

}
