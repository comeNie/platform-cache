package com.deppon.foss.framework.cache.cache;

import com.deppon.foss.framework.cache.IKeyGenerator;
import com.deppon.foss.framework.cache.ResultMap;
import com.deppon.foss.framework.cache.entity.LineEntity;
import com.deppon.foss.framework.exception.BusinessException;
import com.deppon.foss.framework.service.IService;

public class LineService extends DemoBaseCache<LineEntity> implements IService {

    public static final String[] IDS = new String[]{
        "5e776ca50b2f4a41bab6ce756546ff22",
        "c08137771d39471586b8d0a393775ac9",
        "f4ec43c502c649f5bdd3fa7fe325c034",
        "b7053c46622f423e908193d751ef6da1",
        "4dc882b16f1e4d68b1c522c701a06e9a",
        "9fe81f5eda034809b191833ffb9c2fb8",
        "e12f28f6d1234e5db2d81d251ce3f058",
        "31d5a2d658fb4741ba04766744ab992c",
        "90431121014f448c8b60c6d5ea46fbdf",
        "9791e6ea8d934ef99310601d7dd8ea94"
    };
    
    @Override
    public String getCacheId() {
        return LINECACHE_UUID;
    }

    @Override
    public LineEntity doGet(String k) throws BusinessException {
        LineEntity entity = new LineEntity();
        entity.setId(k);
        return entity;
    }

    @Override
    public ResultMap<String, LineEntity> doInitialization(
            IKeyGenerator<String> keyGenerator) throws BusinessException {
        ResultMap<String, LineEntity> map = new ResultMap<String, LineEntity>(keyGenerator);
        for(int i=0;i<IDS.length;i++) {
            LineEntity entity = new LineEntity();
            entity.setId(IDS[i]);
            map.put(IDS[i], entity);
        }
        return map;
    }

}
