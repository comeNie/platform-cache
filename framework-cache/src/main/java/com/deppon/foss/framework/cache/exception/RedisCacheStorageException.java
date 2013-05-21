package com.deppon.foss.framework.cache.exception;

import com.deppon.foss.framework.exception.GeneralException;

/**
 * RedisCacheStorage 查询参数异常
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2012-10-22 下午3:07:44,content:TODO </p>
 * @author ningyu
 * @date 2012-10-22 下午3:07:44
 * @since
 * @version
 */
public class RedisCacheStorageException extends GeneralException {

    private static final long serialVersionUID = 4664623827741256267L;
    
    public RedisCacheStorageException(String message) {
        super(message);
    }

    public RedisCacheStorageException(Throwable e) {
        super(e);
    }

    public RedisCacheStorageException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
