package com.deppon.foss.framework.cache.exception;


/**
 * key不存在
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-1-25 上午10:42:13,content:TODO </p>
 * @author ningyu
 * @date 2013-1-25 上午10:42:13
 * @since
 * @version
 */
public class KeyIsNotFoundException extends RedisCacheStorageException {

    private static final long serialVersionUID = 5165307445946057734L;
    
    public KeyIsNotFoundException(String message) {
        super(message);
    }

    public KeyIsNotFoundException(Throwable e) {
        super(e);
    }

    public KeyIsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
