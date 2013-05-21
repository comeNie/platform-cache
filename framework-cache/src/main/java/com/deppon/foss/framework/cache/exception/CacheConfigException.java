package com.deppon.foss.framework.cache.exception;

import com.deppon.foss.framework.exception.GeneralException;

/**
 * 
 * 
*******************************************
* <b style="font-family:微软雅黑"><small>Description:缓存配置异常</small></b>   </br>
* <b style="font-family:微软雅黑"><small>HISTORY</small></b></br>
* <b style="font-family:微软雅黑"><small> ID      DATE    PERSON     REASON</small></b><br>
********************************************
* <div style="font-family:微软雅黑,font-size:70%"> 
* 1  2011-4-14 樊斌  新增
* </div>  
********************************************
 */
public class CacheConfigException extends GeneralException{

	private static final long serialVersionUID = 437438995471412241L;

	public CacheConfigException(String msg) {
        super(msg);
    }
    
}
