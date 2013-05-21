package com.deppon.foss.framework.cache;

import com.deppon.foss.framework.exception.BusinessException;


public class TTLCache extends CacheSupport<TTLEntity> {

	@Override
	public TTLEntity doGet(String k) throws BusinessException {
		TTLEntity entity = new TTLEntity();
        entity.setId(k);
        return entity;
	}

	@Override
	public String getCacheId() {
		// TODO Auto-generated method stub
		return "TTLCache";
	}

	@Override
	public String doGenerate(String key) {
		return "user_2013-05-02_12:15:12_"+key;
	}

}
