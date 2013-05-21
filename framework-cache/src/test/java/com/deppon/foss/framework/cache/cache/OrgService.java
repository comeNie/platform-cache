package com.deppon.foss.framework.cache.cache;

import com.deppon.foss.framework.cache.IKeyGenerator;
import com.deppon.foss.framework.cache.ResultMap;
import com.deppon.foss.framework.cache.entity.OrgEntity;
import com.deppon.foss.framework.exception.BusinessException;
import com.deppon.foss.framework.service.IService;

public class OrgService extends DemoBaseCache<OrgEntity> implements IService {

    public static final String[] IDS = new String[]{
        "8812086ae4ea40119726ac4ecdb6abd0",
        "470399ed72794e659e2ddf89c4c09229",
        "03fa9cbc00f54ce9941832d68b05434b",
        "e75460314b37420787888dbbe57ef045",
        "f12d02d6440548e6a62be333f2108ea6",
        "3aecdee751a9407ba11b9af08b0c86e2",
        "ccc44f48cfb14f819c42148876ab9826",
        "9032cf50dd5b48ed9d68245e518cefe4",
        "1a012f1f0e5040fea3c1609dbfb8ecf9",
        "7c556aeed6bb4f108d544da89111156f"
    };
    
    @Override
    public String getCacheId() {
        return ORGCACHE_UUID;
    }

    @Override
    public OrgEntity doGet(String k) throws BusinessException {
        OrgEntity entity = new OrgEntity();
        entity.setId(k);
        return entity;
    }

    @Override
    public ResultMap<String, OrgEntity> doInitialization(
            IKeyGenerator<String> keyGenerator) throws BusinessException {
        ResultMap<String, OrgEntity> map = new ResultMap<String, OrgEntity>(keyGenerator);
        for(int i=0;i<IDS.length;i++) {
            OrgEntity entity = new OrgEntity();
            entity.setId(IDS[i]);
            map.put(IDS[i], entity);
        }
        return map;
    }

}
