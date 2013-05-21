package com.deppon.foss.framework.cache.cache;

import com.deppon.foss.framework.cache.IKeyGenerator;
import com.deppon.foss.framework.cache.ResultMap;
import com.deppon.foss.framework.cache.entity.UserEntity;
import com.deppon.foss.framework.exception.BusinessException;
import com.deppon.foss.framework.service.IService;

public class UserService extends DemoBaseCache<UserEntity> implements IService {

    public static final String[] IDS = new String[]{
        "6e054b1f075549f59c5c1eff3d54a9c8",
        "c73ed3ea52d342f5857867847c6b16f3",
        "d8d62164e0d2421a868f4d8b1b392633",
        "cbbb65e17e5f46218385f92e769e972b",
        "0a31fa013a3d46eab8d2fb511c9e4f87",
        "2367011672e245a6b18df5c8667efe72",
        "95164be406a84438a6a4d99240161f46",
        "81cbc8ebe0e345b186a354662677082e",
        "879e3ff19da3462db396de0b249a6ed7",
        "e0076497f3924b2fb227a33420e70f5e"
    };
    
    @Override
    public String getCacheId() {
        return USERCACHE_UUID;
    }

    @Override
    public UserEntity doGet(String k) throws BusinessException {
        UserEntity entity = new UserEntity();
        entity.setId(k);
        return entity;
    }

    @Override
    public ResultMap<String, UserEntity> doInitialization(
            IKeyGenerator<String> keyGenerator) throws BusinessException {
        ResultMap<String, UserEntity> map = new ResultMap<String, UserEntity>(keyGenerator);
        for(int i=0;i<IDS.length;i++) {
            UserEntity entity = new UserEntity();
            entity.setId(IDS[i]);
            map.put(IDS[i], entity);
        }
        return map;
    }

}
