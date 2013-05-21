package com.deppon.foss.framework.cache.cache;

import com.deppon.foss.framework.cache.IKeyGenerator;
import com.deppon.foss.framework.cache.ResultMap;
import com.deppon.foss.framework.cache.entity.RoleEntity;
import com.deppon.foss.framework.exception.BusinessException;
import com.deppon.foss.framework.service.IService;

public class RoleService extends DemoBaseCache<RoleEntity> implements IService {

    public static final String[] IDS = new String[]{
        "99e3115b53204ac2b1328bc55de6f723",
        "8ba1f66ea5504d3fa9ae25cc95a36534",
        "111df93edb1f41c09a4805b62bce256a",
        "02492c88c8b046368fcaeb7945c9a749",
        "65107823923b45a0ade01286fcb017f8",
        "63e28ecee83a499185eae857f042e3e6",
        "9eeea954a8d944198832db88a947730b",
        "7676892a27044ae6832554a12773d831",
        "ef1fdf6779c4456d8a337ff7aca6085f",
        "4c1a58b9d6304c3a88200dfa0ce58de4"
    };
    
    @Override
    public String getCacheId() {
        return ROLECACHE_UUID;
    }

    @Override
    public RoleEntity doGet(String k) throws BusinessException {
        RoleEntity entity = new RoleEntity();
        entity.setId(k);
        return entity;
    }

    @Override
    public ResultMap<String, RoleEntity> doInitialization(
            IKeyGenerator<String> keyGenerator) throws BusinessException {
        ResultMap<String, RoleEntity> map = new ResultMap<String, RoleEntity>(keyGenerator);
        for(int i=0;i<IDS.length;i++) {
            RoleEntity entity = new RoleEntity();
            entity.setId(IDS[i]);
            map.put(IDS[i], entity);
        }
        return map;
    }

}
