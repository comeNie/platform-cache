package com.deppon.foss.framework.cache.exception;


/**
 * key存在，value为空
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-1-25 上午10:42:37,content:TODO </p>
 * @author ningyu
 * @date 2013-1-25 上午10:42:37
 * @since
 * @version
 */
public class ValueIsBlankException extends RedisCacheStorageException {

    private static final long serialVersionUID = 5536157410092139146L;
    
    public ValueIsBlankException(String message) {
        super(message);
    }

    public ValueIsBlankException(Throwable e) {
        super(e);
    }

    public ValueIsBlankException(String message, Throwable cause) {
        super(message, cause);
    }

}
