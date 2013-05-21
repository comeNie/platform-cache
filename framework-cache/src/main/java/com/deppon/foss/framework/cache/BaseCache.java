package com.deppon.foss.framework.cache;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 缓存基类
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-22 下午5:46:59,content:TODO </p>
 * @author ningyu
 * @date 2013-4-22 下午5:46:59
 * @since
 * @version
 */
public abstract class BaseCache<K,V> implements ICache<K, V>, InitializingBean, DisposableBean {

    /**
     * 上下文对象
     */
    private Context context = null;
    
    /**
     * 获取上下文对象
     * @author ningyu
     * @date 2013-4-22 下午5:47:21
     * @return
     * @see
     */
    public Context getContext() {
        return this.context;
    }
    
    /**
     * 创建上下文对象
     * @author ningyu
     * @date 2013-4-22 下午5:47:29
     * @see
     */
    protected void createContext() {
        context = new Context(this);
    }
    
    /**
     * 初始化方法
     * @author ningyu
     * @date 2013-4-22 下午5:47:39
     * @throws Exception
     * @see
     */
    public void afterPropertiesSet() throws Exception {
        this.createContext();
    }
    
    /** 
     * 销毁方法
     * @author ningyu
     * @date 2013-4-22 下午5:52:46
     * @throws Exception 
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub
        
    }
}
