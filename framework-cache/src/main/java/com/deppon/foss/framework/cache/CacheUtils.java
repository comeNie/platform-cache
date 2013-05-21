package com.deppon.foss.framework.cache;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 工具类
 * 
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-4-22 下午5:54:23,content:TODO </p>
 * @author ningyu
 * @date 2013-4-22 下午5:54:23
 * @since
 * @version
 */
public class CacheUtils {

    /**
     * 转换成json
     * 默认将class标记序列化进去
     * 
     * @author ningyu
     * @date 2012-10-22 下午5:23:39
     * @param obj 序列化对象
     * @return
     * @see
     */
    public static String toJsonString(Object obj) {
        return toJsonString(obj,true);
    }
    
    /**
     * 转换成json
     * 
     * @author ningyu
     * @date 2013-3-7 下午1:49:06
     * @param obj 序列化对象
     * @param seralizerClass 是否序列化class标识
     * @return
     * @see
     */
    public static String toJsonString(Object obj,boolean seralizerClass) {
        if(obj != null) {
            if(obj instanceof String && StringUtils.isBlank((String) obj)) {
                return "";
            }
            try {
                if(seralizerClass) {
                    return JSON.toJSONString(obj, SerializerFeature.WriteClassName);
                } else {
                    return JSON.toJSONString(obj);
                }
            } catch(Exception e) {
                return "null";
            }
        }
        return "null";
    }
    
    /**
     * json转javaBean对象
     * @author ningyu
     * @date 2013-4-22 下午5:54:32
     * @param json
     * @return
     * @see
     */
    public static Object jsonParseObject(String json) {
        if(StringUtils.isBlank(json)) {
            return "";
        } else if(StringUtils.equalsIgnoreCase(json, "null")) {
            return null;
        }
        return JSON.parse(json);
    }
}
