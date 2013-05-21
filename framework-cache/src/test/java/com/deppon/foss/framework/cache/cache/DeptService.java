package com.deppon.foss.framework.cache.cache;

import com.deppon.foss.framework.cache.IKeyGenerator;
import com.deppon.foss.framework.cache.ResultMap;
import com.deppon.foss.framework.cache.entity.DeptEntity;
import com.deppon.foss.framework.exception.BusinessException;
import com.deppon.foss.framework.service.IService;

public class DeptService extends DemoBaseCache<DeptEntity> implements IService {

    public static final String[] IDS = new String[]{
            "cbb9cb3e3527440fb7bfc8bb36630ee0",
            "300743e923944a24bbca60741c1d0516",
            "8b8299789c52439ea4a1c7334e17a622",
            "4b55e95f504749aca582e924ffadc541",
            "ffe37eaf23284ef7a11a827252cabf0b",
            "ed7a580f649b428185ef36bd93d39bc0",
            "30f1480f26f047c3b76972aa199e14f1",
            "166e50a2097f49aab30323fb99dae488",
            "f9a3ded55ed9401b9ac2880c3b8a60a4",
            "efefea40a8934dbcb06579fde6a5d4b8"
    };
    
    @Override
    public String getCacheId() {
        return DEPTCACHE_UUID;
    }

    @Override
    public DeptEntity doGet(String k) throws BusinessException {
        DeptEntity entity = new DeptEntity();
        entity.setId(k);
        return entity;
    }

    @Override
    public ResultMap<String, DeptEntity> doInitialization(
            IKeyGenerator<String> keyGenerator) throws BusinessException {
        ResultMap<String, DeptEntity> map = new ResultMap<String, DeptEntity>(keyGenerator);
        for(int i=0;i<IDS.length;i++) {
            DeptEntity entity = new DeptEntity();
            entity.setId(IDS[i]);
            map.put(IDS[i], entity);
        }
        return map;
    }

}
