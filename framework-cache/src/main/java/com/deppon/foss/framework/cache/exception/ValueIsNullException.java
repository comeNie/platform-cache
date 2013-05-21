package com.deppon.foss.framework.cache.exception;


/**
 * key存在，value为null
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-1-25 上午10:42:56,content:TODO </p>
 * @author ningyu
 * @date 2013-1-25 上午10:42:56
 * @since
 * @version
 */
public class ValueIsNullException extends RedisCacheStorageException {

    private static final long serialVersionUID = 932825584009506614L;

    public ValueIsNullException(String message) {
        super(message);
    }

    public ValueIsNullException(Throwable e) {
        super(e);
    }

    public ValueIsNullException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
