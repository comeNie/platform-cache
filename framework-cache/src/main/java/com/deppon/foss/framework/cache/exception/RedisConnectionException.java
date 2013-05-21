package com.deppon.foss.framework.cache.exception;


/**
 * Redis连接异常
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-1-25 上午10:30:13,content:TODO </p>
 * @author ningyu
 * @date 2013-1-25 上午10:30:13
 * @since
 * @version
 */
public class RedisConnectionException extends RedisCacheStorageException {

    private static final long serialVersionUID = -4269004402633873780L;
    
    public RedisConnectionException(String message) {
        super(message);
    }

    public RedisConnectionException(Throwable e) {
        super(e);
    }

    public RedisConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
