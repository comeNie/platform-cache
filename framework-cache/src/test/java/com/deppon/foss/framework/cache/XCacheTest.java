package com.deppon.foss.framework.cache;

import java.util.Map;
import java.util.Random;

import com.deppon.foss.framework.cache.cache.DemoBaseCache;
import com.deppon.foss.framework.cache.cache.DeptService;
import com.deppon.foss.framework.cache.cache.LineService;
import com.deppon.foss.framework.cache.cache.OrgService;
import com.deppon.foss.framework.cache.cache.RoleService;
import com.deppon.foss.framework.cache.cache.UserService;
import com.deppon.foss.framework.cache.entity.DeptEntity;
import com.deppon.foss.framework.cache.entity.LineEntity;
import com.deppon.foss.framework.cache.entity.OrgEntity;
import com.deppon.foss.framework.cache.entity.RoleEntity;
import com.deppon.foss.framework.cache.entity.UserEntity;
import com.deppon.foss.framework.cache.storage.LocallyStorage;

public class XCacheTest {

    static LocallyStorage storage = new LocallyStorage(5000);
    
    static String[] cacheIds = new String[]{
        DemoBaseCache.DEPTCACHE_UUID,
        DemoBaseCache.LINECACHE_UUID,
        DemoBaseCache.ORGCACHE_UUID,
        DemoBaseCache.ROLECACHE_UUID,
        DemoBaseCache.USERCACHE_UUID};
    
    public static void initCache() throws Exception {
        UserService user = new UserService();
        user.setLazy(false);
        user.setTimeOut(100);
        user.setStorage(storage);
        user.afterPropertiesSet();
        
        RoleService role = new RoleService();
        role.setLazy(false);
        role.setTimeOut(100);
        role.setStorage(storage);
        role.afterPropertiesSet();
        
        OrgService org = new OrgService();
        org.setLazy(false);
        org.setTimeOut(100);
        org.setStorage(storage);
        org.afterPropertiesSet();
        
        LineService line = new LineService();
        line.setLazy(false);
        line.setTimeOut(100);
        line.setStorage(storage);
        line.afterPropertiesSet();
        
        DeptService dept = new DeptService();
        dept.setLazy(false);
        dept.setTimeOut(100);
        dept.setStorage(storage);
        dept.afterPropertiesSet();
    }
    
    public static synchronized int randomExpire() {
        return 1000;
//        Random random = new Random(System.currentTimeMillis());
//        int k = random.nextInt();
//        int j = Math.abs(k % 99) + 12;
//        return j;
    }

    public static synchronized int random(int i) {
        Random random = new Random();
        int k = random.nextInt();
        int j = Math.abs(k % i);
        return j;
    }
    
    public static void main(String[] args) throws Exception {
//        while(true) {
//            Thread.sleep(1000);
//            int expire = random(10) * 10;
//            System.out.println(expire <= 0 ? 100 : expire);
//        }
        initCache();
        for(int i=0;i<10;i++) {
            MyThread t = new MyThread();
            t.start();
            Thread.sleep(randomExpire());
        }
    }
    
    static class MyThread extends Thread {

        @Override
        public void run() {
            while(true) {
                String cacheId_uuid = cacheIds[random(5)];
                System.out.println("cacheId_uuid="+cacheId_uuid);
                ICache c = CacheManager.getInstance().getCache(cacheId_uuid);
                String key = null;
                String cacheId = null;
                if(c instanceof UserService) {
                    key = UserService.IDS[random(10)];
                    UserService user = (UserService) c;
                    cacheId = user.getCacheId();
                } else if(c instanceof DeptService) {
                    key = DeptService.IDS[random(10)];
                    DeptService dept = (DeptService) c;
                    cacheId = dept.getCacheId();
                } else if(c instanceof RoleService) {
                    key = RoleService.IDS[random(10)];
                    RoleService role = (RoleService) c;
                    cacheId = role.getCacheId();
                } else if(c instanceof LineService) {
                    key = LineService.IDS[random(10)];
                    LineService line = (LineService) c;
                    cacheId = line.getCacheId();
                } else if(c instanceof OrgService) {
                    key = OrgService.IDS[random(10)];
                    OrgService org = (OrgService) c;
                    cacheId = org.getCacheId();
                } else {
                    System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                }
                switch(random(10)) {
                case 0:
                    //set
                    key = cacheId + "_" + key;
                    if(c instanceof UserService) {
                        UserEntity entity = new UserEntity();
                        entity.setId(key);
                        storage.set(key,entity);
                    } else if(c instanceof DeptService) {
                        DeptEntity entity = new DeptEntity();
                        entity.setId(key);
                        storage.set(key,entity);
                    } else if(c instanceof RoleService) {
                        RoleEntity entity = new RoleEntity();
                        entity.setId(key);
                        storage.set(key,entity);
                    } else if(c instanceof LineService) {
                        LineEntity entity = new LineEntity();
                        entity.setId(key);
                        storage.set(key,entity);
                    } else if(c instanceof OrgService) {
                        OrgEntity entity = new OrgEntity();
                        entity.setId(key);
                        storage.set(key,entity);
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    System.out.println("set方法调用====key="+key);
                    break;
                case 1:
                    //setex
                    key = cacheId + "_" + key;
                    if(c instanceof UserService) {
                        UserEntity entity = new UserEntity();
                        entity.setId(key);
                        int expire = randomExpire();
                        storage.set(key,entity,expire);
                        System.out.println("setex方法调用====key="+key+",expire="+expire);
                    } else if(c instanceof DeptService) {
                        DeptEntity entity = new DeptEntity();
                        entity.setId(key);
                        int expire = randomExpire();
                        storage.set(key,entity,expire);
                        System.out.println("setex方法调用====key="+key+",expire="+expire);
                    } else if(c instanceof RoleService) {
                        RoleEntity entity = new RoleEntity();
                        entity.setId(key);
                        int expire = randomExpire();
                        storage.set(key,entity,expire);
                        System.out.println("setex方法调用====key="+key+",expire="+expire);
                    } else if(c instanceof LineService) {
                        LineEntity entity = new LineEntity();
                        entity.setId(key);
                        int expire = randomExpire();
                        storage.set(key,entity,expire);
                        System.out.println("setex方法调用====key="+key+",expire="+expire);
                    } else if(c instanceof OrgService) {
                        OrgEntity entity = new OrgEntity();
                        entity.setId(key);
                        int expire = randomExpire();
                        storage.set(key,entity,expire);
                        System.out.println("setex方法调用====key="+key+",expire="+expire);
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    break;
                case 2:
                    //get
                    key = cacheId + "_" + key;
                    if(c instanceof UserService) {
                        UserEntity entity = (UserEntity) storage.get(key);
                        if(entity != null) {
                            System.out.println("get方法调用====key="+key+",value_id="+entity.getId());
                        } else {
                            System.out.println("get方法调用====key="+key+",value=null");
                        }
                    } else if(c instanceof DeptService) {
                        DeptEntity entity = (DeptEntity) storage.get(key);
                        if(entity != null) {
                            System.out.println("get方法调用====key="+key+",value_id="+entity.getId());
                        } else {
                            System.out.println("get方法调用====key="+key+",value=null");
                        }
                    } else if(c instanceof RoleService) {
                        RoleEntity entity = (RoleEntity) storage.get(key);
                        if(entity != null) {
                            System.out.println("get方法调用====key="+key+",value_id="+entity.getId());
                        } else {
                            System.out.println("get方法调用====key="+key+",value=null");
                        }
                    } else if(c instanceof LineService) {
                        LineEntity entity = (LineEntity) storage.get(key);
                        if(entity != null) {
                            System.out.println("get方法调用====key="+key+",value_id="+entity.getId());
                        } else {
                            System.out.println("get方法调用====key="+key+",value=null");
                        }
                    } else if(c instanceof OrgService) {
                        OrgEntity entity = (OrgEntity) storage.get(key);
                        if(entity != null) {
                            System.out.println("get方法调用====key="+key+",value_id="+entity.getId());
                        } else {
                            System.out.println("get方法调用====key="+key+",value=null");
                        }
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    break;
                case 3:
                    key = cacheId + "_" + key;
                    storage.remove(key);
                    System.out.println("remove方法调用====key="+key);
                    //remove
                    break;
                case 4:
                    //removeMulti
                    key = cacheId + "_" + key;
                    storage.removeMulti(key);
                    System.out.println("removeMulti方法调用====key="+key);
                    break;
                case 5:
                    //setMap
                    if(c instanceof UserService) {
                        UserService cache = (UserService) c;
                        ResultMap<String, UserEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        storage.set(map);
                    } else if(c instanceof DeptService) {
                        DeptService cache = (DeptService) c;
                        ResultMap<String, DeptEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        storage.set(map);
                    } else if(c instanceof RoleService) {
                        RoleService cache = (RoleService) c;
                        ResultMap<String, RoleEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        storage.set(map);
                    } else if(c instanceof LineService) {
                        LineService cache = (LineService) c;
                        ResultMap<String, LineEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        storage.set(map);
                    } else if(c instanceof OrgService) {
                        OrgService cache = (OrgService) c;
                        ResultMap<String, OrgEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        storage.set(map);
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    System.out.println("setMap方法调用");
                    break;
                case 6:
                    //setMapEx
                    if(c instanceof UserService) {
                        UserService cache = (UserService) c;
                        ResultMap<String, UserEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        int expire = randomExpire();
                        storage.set(map,expire);
                        System.out.println("setMapEx方法调用,expire="+expire);
                    } else if(c instanceof DeptService) {
                        DeptService cache = (DeptService) c;
                        ResultMap<String, DeptEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        int expire = randomExpire();
                        storage.set(map,expire);
                        System.out.println("setMapEx方法调用,expire="+expire);
                    } else if(c instanceof RoleService) {
                        RoleService cache = (RoleService) c;
                        ResultMap<String, RoleEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        int expire = randomExpire();
                        storage.set(map,expire);
                        System.out.println("setMapEx方法调用,expire="+expire);
                    } else if(c instanceof LineService) {
                        LineService cache = (LineService) c;
                        ResultMap<String, LineEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        int expire = randomExpire();
                        storage.set(map,expire);
                        System.out.println("setMapEx方法调用,expire="+expire);
                    } else if(c instanceof OrgService) {
                        OrgService cache = (OrgService) c;
                        ResultMap<String, OrgEntity> map = cache.doInitialization(new IKeyGenerator<String>() {
                            @Override
                            public String generate(String key) {
                                // TODO Auto-generated method stub
                                return key;
                            }
                        });
                        int expire = randomExpire();
                        storage.set(map,expire);
                        System.out.println("setMapEx方法调用,expire="+expire);
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    break;
                case 7:
                    //getAll
                    key = cacheId + "_" + key;
                    if(c instanceof UserService) {
                        UserService cache = (UserService) c;
                        Map map = storage.get(cache.getContext());
                        System.out.println("getAll方法调用====cacheId="+cacheId+",map="+map.size());
                    } else if(c instanceof DeptService) {
                        DeptService cache = (DeptService) c;
                        Map map = storage.get(cache.getContext());
                        System.out.println("getAll方法调用====cacheId="+cacheId+",map="+map.size());
                    } else if(c instanceof RoleService) {
                        RoleService cache = (RoleService) c;
                        Map map = storage.get(cache.getContext());
                        System.out.println("getAll方法调用====cacheId="+cacheId+",map="+map.size());
                    } else if(c instanceof LineService) {
                        LineService cache = (LineService) c;
                        Map map = storage.get(cache.getContext());
                        System.out.println("getAll方法调用====cacheId="+cacheId+",map="+map.size());
                    } else if(c instanceof OrgService) {
                        OrgService cache = (OrgService) c;
                        Map map = storage.get(cache.getContext());
                        System.out.println("getAll方法调用====cacheId="+cacheId+",map="+map.size());
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    System.out.println("getAll方法调用====cacheId="+cacheId);
                    break;
                case 8:
                    //clear
                    key = cacheId + "_" + key;
                    if(c instanceof UserService) {
                        UserService cache = (UserService) c;
                        storage.clear(cache.getContext());
                    } else if(c instanceof DeptService) {
                        DeptService cache = (DeptService) c;
                        storage.clear(cache.getContext());
                    } else if(c instanceof RoleService) {
                        RoleService cache = (RoleService) c;
                        storage.clear(cache.getContext());
                    } else if(c instanceof LineService) {
                        LineService cache = (LineService) c;
                        storage.clear(cache.getContext());
                    } else if(c instanceof OrgService) {
                        OrgService cache = (OrgService) c;
                        storage.clear(cache.getContext());
                    } else {
                        System.out.println("没有匹配到缓存对象，跳过_"+cacheId_uuid);
                    }
                    System.out.println("clear方法调用====cacheId="+cacheId);
                    break;
                case 9:
                    //ttl
                    key = cacheId + "_" + key;
                    long ttl = storage.ttl(key);;
                    System.out.println("ttl方法调用====cacheId="+cacheId+",key="+key+",ttl="+ttl);
                    break;
                }
                int sleep = random(10) * 100;
                try {
                    sleep(sleep <= 0 ? 1000 : sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

//    if(c instanceof UserService) {
//        key = UserService.IDS[random(10)];
//        UserService user = (UserService) c;
//        System.out.println("user对象="+user.get(key).getId());
//    } else if(c instanceof DeptService) {
//        key = DeptService.IDS[random(10)];
//        DeptService dept = (DeptService) c;
//        System.out.println("dept对象="+dept.get(key).getId());
//    } else if(c instanceof RoleService) {
//        key = RoleService.IDS[random(10)];
//        RoleService role = (RoleService) c;
//        System.out.println("role对象="+role.get(key).getId());
//    } else if(c instanceof LineService) {
//        key = LineService.IDS[random(10)];
//        LineService line = (LineService) c;
//        System.out.println("line对象="+line.get(key).getId());
//    } else if(c instanceof OrgService) {
//        key = OrgService.IDS[random(10)];
//        OrgService org = (OrgService) c;
//        System.out.println("org对象="+org.get(key).getId());
//    }
}
