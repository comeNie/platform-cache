package com.deppon.foss.framework.cache.redis;

import java.lang.Thread.State;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.deppon.foss.framework.cache.exception.CacheConfigException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * 1.封装redis客户端
 * 2.支持failover(2.1.尝试恢复client连接,2.2.尝试修复集群信息,2.3.尝试验证strongCache初始化是否成功)
 * 3.支持动态切换redis服务(4.1.master slave之间切换)
 * 
 * @author ningyu
 * @date 2012-10-20 下午1:43:53
 * @since
 * @version 1.1.1
 */
public class RedisClient implements InitializingBean,DisposableBean {
    
    /**
     * 日志
     */
    Log log = LogFactory.getLog(getClass());
    
    /**
     * 节点唯一标识
     */
    private String nodeUUID;

    /**
     * redis client连接池
     */
    private JedisPool pool;
    
    /**
     * 配置参数
     */
    private JedisPoolConfig poolConfig;
    
    /**
     * 第一个服务IP
     */
    private String host1 = "localhost";
    
    /**
     * 第一个服务端口
     */
    private int port1 = Protocol.DEFAULT_PORT;
    
    /**
     * 连接超时时间
     */
    private int timeout1 = Protocol.DEFAULT_TIMEOUT;
    
    /**
     * 服务器密码
     */
    private String password1;

    /**
     * 第二个服务IP
     */
    private String host2;
    
    /**
     * 第二个服务端口
     */
    private int port2 = 0;
    
    /**
     * 连接超时时间
     */
    private int timeout2 = Protocol.DEFAULT_TIMEOUT;
    
    /**
     * 服务器密码
     */
    private String password2;
    
    private JedisShardInfo shardInfo1;
    
    private JedisShardInfo shardInfo2;
    
    /**
     * 第一个服务client
     */
    private Jedis jedis1;
    
    /**
     * 第二个服务client
     */
    private Jedis jedis2;
    
    /**
     * 主节点
     */
    private Node masterNode;

    /**
     * 备节点
     */
    private Node slaveNode;
    
    /**
     * 稍后重试初始化redis client任务
     */
    private RetryTask retryTask;
    
    /**
     * 稍后重试初始化redis client 延迟时间，默认1分钟
     */
    private int retryDelayTime = 60 * 1000;
    
    /**
     * 修复集群信息任务
     */
    private RepairTask repairTask;
    
    /**
     * 修复集群信息 延迟时间，默认1分钟
     */
    private int repairDelayTime = 60 * 1000;
    
    /**
     * 初始化client pool成功为true，在切换服务器的时候被赋值为false，切换成功后被赋值为true
     */
    private AtomicBoolean poolInited = new AtomicBoolean(false);
    
    /**
     * 集群信息 key
     */
    public static final String CLUSTER_KEY = "DPAP-Redis-Cluster-Nodes";
    
    /**
     * 初始化锁 key
     */
    public static final String LOCK_KEY = "DPAP.redis.lock";
    
    @Override
    public void afterPropertiesSet() throws Exception {
        nodeUUID = UUID.randomUUID().toString();
        if (shardInfo1 == null && StringUtils.isNotBlank(host1) && port1 > 0) {
        	shardInfo1 = new JedisShardInfo(host1, port1);
        	if (StringUtils.isNotBlank(password1)) {
        		shardInfo1.setPassword(password1);
        	}
        	shardInfo1.setTimeout(timeout1);
        }
        if (shardInfo2 == null && StringUtils.isNotBlank(host2) && port2 > 0) {
        	shardInfo2 = new JedisShardInfo(host2, port2);
        	if (StringUtils.isNotBlank(password2)) {
        		shardInfo2.setPassword(password2);
        	}
        	shardInfo2.setTimeout(timeout2);
        }
        initialization();
    }
    
    public JedisPoolConfig getPoolConfig() {
        if(poolConfig == null) {
            poolConfig = new JedisPoolConfig();
            poolConfig.setMaxActive(1024);
            poolConfig.setMaxIdle(200);
            poolConfig.setMaxWait(1000);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
        }
        return poolConfig;
    }

    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public String getHost1() {
        return host1;
    }

    public void setHost1(String host1) {
    	if(StringUtils.isNotBlank(host1)) {
    		this.host1 = host1;
    	} else {
    		throw new CacheConfigException("参数配置错误:host1=["+host1+"]");
    	}
    }

    public int getPort1() {
        return port1;
    }

    public void setPort1(int port1) {
    	if(port1 > 0) {
    		this.port1 = port1;
    	} else {
    		throw new CacheConfigException("参数配置错误:port1=["+port1+"]");
    	}
    }

    public String getHost2() {
        return host2;
    }

    public void setHost2(String host2) {
    	if(StringUtils.isNotBlank(host2)) {
    		this.host2 = host2;
    	} else {
    		throw new CacheConfigException("参数配置错误:host2=["+host2+"]");
    	}
    }

    public int getPort2() {
        return port2;
    }

    public void setPort2(int port2) {
    	if(port2 > 0) {
    		this.port2 = port2;
    	} else {
    		throw new CacheConfigException("参数配置错误:port2=["+port2+"]");
    	}
    }

    public boolean getPoolInited() {
        return poolInited.get();
    }
    
    public int getTimeout1() {
        return timeout1;
    }

    public void setTimeout1(int timeout1) {
    	if(timeout1 > 0) {
    		this.timeout1 = timeout1;
    	} else {
    		throw new CacheConfigException("参数配置错误:timeout1=["+timeout1+"]");
    	}
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public int getTimeout2() {
        return timeout2;
    }

    public void setTimeout2(int timeout2) {
    	if(timeout2 > 0) {
    		this.timeout2 = timeout2;
    	} else {
    		throw new CacheConfigException("参数配置错误:timeout2=["+timeout2+"]");
    	}
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public JedisShardInfo getShardInfo1() {
        return shardInfo1;
    }

    public void setShardInfo1(JedisShardInfo shardInfo1) {
        this.shardInfo1 = shardInfo1;
    }

    public JedisShardInfo getShardInfo2() {
        return shardInfo2;
    }

    public void setShardInfo2(JedisShardInfo shardInfo2) {
        this.shardInfo2 = shardInfo2;
    }
    
    private boolean enableServer2 = false;

    /**
     * 这个和线程中稍后重试初始化client pool任务代码一样
     * 这里在初始化的时候调用，如果执行失败会交给单独线程去尝试操作
     * 
     * <p>可能出现的环境情况</p>
     * 1.两个服务都是好的，初始过了集群信息，执行过程中某个服务有问题，稍后再重试
     * 2.两个服务都是好的，没有初始集群信息，执行过程中某个服务有问题，稍后再重试
     * 3.只有一个服务是好的，没有初始集群信息，执行过程中某个服务有问题，稍后再重试
     * 4.只有一个服务是好的，初始过了集群信息，执行过程中某个服务有问题，稍后再重试
     * 5.两个服务都是坏的，稍后再初始
     *  
     * @author ningyu
     * @date 2012-10-22 上午9:01:54
     * @see
     */
    private void initialization() {
        poolInited.set(false);
        
        jedis1 = new Jedis(shardInfo1);
        if(shardInfo2 != null) {
        	jedis2 = new Jedis(shardInfo2);
        	enableServer2 = true;
        }
        
        boolean connected1 = false;
        boolean connected2 = false;
        try {
            jedis1.getClient().connect();
            connected1 = jedis1.getClient().isConnected();
        } catch(JedisConnectionException e) {
            connected1 = false;
        }
        if(enableServer2) {
        	try {
        		jedis2.getClient().connect();
        		connected2 = jedis2.getClient().isConnected();
        	} catch(JedisConnectionException e) {
        		connected2 = false;
        	}
        }
        String nodesMessage1 = null;
        String nodesMessage2 = null;
        if(enableServer2) {
        	if(connected1 && connected2) {
                //1.两个服务都是好的
                try {
                    nodesMessage1 = jedis1.get(CLUSTER_KEY);
                } catch (JedisConnectionException e) {
                    log.error(e.getMessage(),e);
                    disconnect(jedis1);
                    disconnect(jedis2);
                    
                    retry();
                    return;// 稍后再试
                }
                try {
                    nodesMessage2 = jedis2.get(CLUSTER_KEY);
                } catch (JedisConnectionException e) {
                    log.error(e.getMessage(),e);
                    disconnect(jedis1);
                    disconnect(jedis2);
                    
                    retry();
                    return;// 稍后再试
                }
                if(nodesMessage1 != null && nodesMessage2 != null && nodesMessage1.equals(nodesMessage2)) {
                    //1.两个服务都是好的，初始过了集群信息
                    initPool(nodesMessage1);
                    disconnect(jedis1);
                    disconnect(jedis2);
                    return;
                } else {
                    //忽略错误的集群信息，2.两个服务都是好的，没有初始集群信息
                    boolean lock = getLock(jedis1,jedis2);
                    if(lock) {
                        //只有两个同时锁定时才去执行初始化
                        try {
                            Node node = initClusterNodes(jedis1,jedis2);
                            initPool(node);
                            releaseLock(jedis1);
                        } catch(JedisConnectionException e) {
                            log.error(e.getMessage(),e);
                            releaseLock(jedis1,jedis2);
                            retry();
                            return;// 稍后再试
                        } finally {
                            disconnect(jedis1);
                            disconnect(jedis2);
                        }
                        return;
                    } else {
                        //只锁定了一个释放锁，稍后修复集群信息
                        releaseLock(jedis1,jedis2);
                        
                        Node master = new Node();
                        master.setHost(jedis1.getClient().getHost());
                        master.setPort(jedis1.getClient().getPort());
                        master.setTimeout(jedis1.getClient().getTimeout());
                        Node slave = new Node();
                        slave.setHost(jedis2.getClient().getHost());
                        slave.setPort(jedis2.getClient().getPort());
                        slave.setTimeout(jedis2.getClient().getTimeout());
                        master.setNext(slave);
                        slave.setPrevious(master);
                        try {
                            jedis1.slaveofNoOne();
                            initPool(master);
                            disconnect(jedis1);
                            disconnect(jedis2);
                        } catch(JedisConnectionException e) {
                            log.error(e.getMessage(),e);
                            disconnect(jedis1);
                            disconnect(jedis2);
                            retry();
                            return;// 稍后再试
                        }
                        repair();
                        return;// 修复集群信息
                    }
                }
            } else if(connected1){
                //只有一个服务是好的
                //忽略现有的集群信息，不用集群信息初始化client，直接初始化client连接指向正常的redis服务，稍后修复集群信息保存到redis服务上
                Node master = new Node();
                master.setHost(jedis1.getClient().getHost());
                master.setPort(jedis1.getClient().getPort());
                master.setTimeout(jedis1.getClient().getTimeout());
                Node slave = new Node();
                slave.setHost(jedis2.getClient().getHost());
                slave.setPort(jedis2.getClient().getPort());
                slave.setTimeout(jedis2.getClient().getTimeout());
                master.setNext(slave);
                slave.setPrevious(master);
                try {
                    jedis1.slaveofNoOne();
                    initPool(master);
                    disconnect(jedis1);
                    disconnect(jedis2);
                } catch(JedisConnectionException e) {
                    log.error(e.getMessage(),e);
                    disconnect(jedis1);
                    disconnect(jedis2);
                    retry();
                    return;// 稍后再试
                }
                repair();
                return;// 修复集群信息
            } else if(connected2) {
                //只有一个服务是好的
                //忽略现有的集群信息，不用集群信息初始化client，直接初始化client连接指向正常的redis服务，稍后修复集群信息保存到redis服务上
                Node master = new Node();
                master.setHost(jedis2.getClient().getHost());
                master.setPort(jedis2.getClient().getPort());
                master.setTimeout(jedis2.getClient().getTimeout());
                Node slave = new Node();
                slave.setHost(jedis1.getClient().getHost());
                slave.setPort(jedis1.getClient().getPort());
                slave.setTimeout(jedis1.getClient().getTimeout());
                master.setNext(slave);
                slave.setPrevious(master);
                try {
                    jedis2.slaveofNoOne();
                    initPool(master);
                    disconnect(jedis1);
                    disconnect(jedis2);
                } catch(JedisConnectionException e) {
                    log.error(e.getMessage(),e);
                    disconnect(jedis1);
                    disconnect(jedis2);
                    retry();
                    return;// 稍后再试
                }
                repair();
                return;// 修复集群信息
            } else {
                //如果没有一个服务是好的
                retry();
                return;// 稍后再试
            }
        } else {
        	if(connected1){
                //只有一个服务是好的
                //忽略现有的集群信息，不用集群信息初始化client，直接初始化client连接指向正常的redis服务，稍后修复集群信息保存到redis服务上
                Node master = new Node();
                master.setHost(jedis1.getClient().getHost());
                master.setPort(jedis1.getClient().getPort());
                master.setTimeout(jedis1.getClient().getTimeout());
                try {
                    jedis1.slaveofNoOne();
                    initPool(master);
                    disconnect(jedis1);
                } catch(JedisConnectionException e) {
                    log.error(e.getMessage(),e);
                    disconnect(jedis1);
                    retry();
                    return;// 稍后再试
                }
            } else {
                //如果没有一个服务是好的
                retry();
                return;// 稍后再试
            }
        }
        
    }
    
    /**
     * 前提条件必须两个redis服务都是正常的,没有满足前提条件的情况下忽略集群信息,开启线程稍后尝试恢复集群信息
     * <p>构造集群节点信息更新到服务器上</p> 
     * @author ningyu
     * @date 2012-10-20 上午11:40:41
     * @return
     * @see
     */
    private Node initClusterNodes(Jedis master,Jedis slave) {
        Node mNode = new Node();
        Node sNode = new Node();
        
        if(master.getClient().getHost().equalsIgnoreCase(shardInfo1.getHost())) {
            mNode.setHost(shardInfo1.getHost());
            mNode.setPort(shardInfo1.getPort());
            mNode.setTimeout(shardInfo1.getTimeout());
            mNode.setPassword(shardInfo1.getPassword());
            mNode.setWeight(shardInfo1.getWeight());
            
            sNode.setHost(shardInfo2.getHost());
            sNode.setPort(shardInfo2.getPort());
            sNode.setTimeout(shardInfo2.getTimeout());
            sNode.setPassword(shardInfo2.getPassword());
            sNode.setWeight(shardInfo2.getWeight());
        } else {
            mNode.setHost(shardInfo2.getHost());
            mNode.setPort(shardInfo2.getPort());
            mNode.setTimeout(shardInfo2.getTimeout());
            mNode.setPassword(shardInfo2.getPassword());
            mNode.setWeight(shardInfo2.getWeight());
            
            sNode.setHost(shardInfo1.getHost());
            sNode.setPort(shardInfo1.getPort());
            sNode.setTimeout(shardInfo1.getTimeout());
            sNode.setPassword(shardInfo1.getPassword());
            sNode.setWeight(shardInfo1.getWeight());
        }
        
        mNode.setPrevious(null);
        mNode.setNext(sNode);
        sNode.setPrevious(mNode);
        sNode.setNext(null);
        
        master.slaveofNoOne();
        slave.slaveof(mNode.getHost(), mNode.getPort());
        master.set(CLUSTER_KEY, Node.marshal(mNode));
        return mNode;
    }
    
    /**
     * <p>根据服务器上的集群信息初始化client pool</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:14:45
     * @param nodesMessage
     * @see
     */
    private void initPool(String nodesMessage) {
        Node node = Node.unmarshal(nodesMessage);
        try {
            if(node.getHost().equals(jedis1.getClient().getHost())) {
                jedis1.slaveofNoOne();
                jedis2.slaveof(node.getHost(), node.getPort());
            } else {
                jedis2.slaveofNoOne();
                jedis1.slaveof(node.getHost(), node.getPort());
            }
        } catch(JedisConnectionException e) {
            log.error(e.getMessage(),e);
        }
        initPool(node);
    }

    /**
     * <p>根据节点信息初始化client pool</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:14:30
     * @param master
     * @see
     */
    private void initPool(Node master) {
        this.masterNode = master;
        this.slaveNode = master.getNext();
        this.destroy();
        pool = new JedisPool(getPoolConfig(), master.getHost(), master.getPort(), master.getTimeout(), master.getPassword());
        poolInited.set(true);
        log.info("Init local redundant pool.");
    }
    
    /**
     * <p>在调用client连接redis服务器段开时，尝试failover</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:11:49
     * @see
     */
    public void handoverToSlave() {
        log.debug("invoke handoverToSlave");
        //1.重新初始化redis client连接指向一个可用的redis服务器，忽略集群信息
        //2.稍后尝试修复集群信息
        
        //如果服务器停止了需要切换redis服务器连接初始化client pool，则停止修复集群信息线程任务
        if(repairTask != null && !repairTask.getState().name().equals(State.TERMINATED.name())) {
            repairTask.stopThread();
        }
        initialization();
    }
    
    /**
     * <p>检查集群信息是否已经有其他节点修复过了</p> 
     * @author ningyu
     * @date 2012-10-22 下午4:27:44
     * @param master
     * @param slave
     * @return
     * @see
     */
    private boolean checkClusterNodes() {
        try {
            Jedis masterJedis = getMasterJedis();
            String clusterInfo = masterJedis.get(CLUSTER_KEY);
            String str = Node.marshal(this.masterNode);
            if(StringUtils.isBlank(clusterInfo) || !clusterInfo.equals(str)) {
                return true;
            }
        } catch(JedisConnectionException e) {
            log.error(e.getMessage(),e);
            return true;
        }
        return false;
    }
    
    /**
     * <p>从jedis1和jedis2中返回master的那个</p> 
     * 因为全局只new两个jedis对象,所以这个方法返回jedis1和jedis2中为master的那个
     * @author ningyu
     * @date 2012-10-24 上午11:50:12
     * @return
     * @see
     */
    private Jedis getMasterJedis() {
        if(jedis1.getClient().getHost().equals(masterNode.getHost())) {
            return jedis1;
        } else {
            return jedis2;
        }
    }
    
    /**
     * <p>从jedis1和jedis2中返回slave的那个</p> 
     * 因为全局只new两个jedis对象,所以这个方法返回jedis1和jedis2中为slave的那个
     * @author ningyu
     * @date 2012-10-24 上午11:50:12
     * @return
     * @see
     */
    private Jedis getSlaveJedis() {
        if(jedis1.getClient().getHost().equals(slaveNode.getHost())) {
            return jedis1;
        } else {
            return jedis2;
        }
    }
    
    /**
     * <p>关闭连接</p> 
     * 
     * @author ningyu
     * @date 2012-10-24 下午1:47:57
     * @param jedis
     * @see
     */
    private void disconnect(Jedis jedis) {
        jedis.disconnect();
    }
    
    public Jedis getResource() {
        return pool.getResource();
    }
    
    public void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }
    
    public void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }
    
    /**
     * <p>锁定，节点锁定服务器的锁定标记</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:12:44
     * @param j
     * @return
     * @see
     */
    public boolean getLock(Jedis j1, Jedis j2) {
        boolean b1 = false,b2 = false;
        try {
            b1 = getLock(j1,LOCK_KEY);
        } catch(JedisDataException e) {
            b1 = true;
        }
        try {
            b2 = getLock(j2,LOCK_KEY);
        } catch(JedisDataException e) {
            b2 = true;
        }
        return b1 && b2;
    }
    
    /**
     * <p>锁定，节点锁定服务器的锁定标记</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:13:13
     * @param j
     * @param lockKey
     * @return
     * @see
     */
    public boolean getLock(Jedis j,String lockKey) {
        long result = 0;
        try {
            result = j.setnx(lockKey, nodeUUID);
            if (result == 1) {
                return true;
            } else if(nodeUUID.equals(j.get(lockKey))){
                return true;
            }
        } catch (JedisConnectionException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }
    
    /**
     * 设置了主从redis关系之后释放锁操作方法
     * @author ningyu
     * @date 2013-1-6 下午12:43:36
     * @param master
     * @see
     */
    public void releaseLock(Jedis master) {
        releaseLock(master,LOCK_KEY);
    }

    /**
     * j1,j2没有主从关系 释放锁操作方法
     * @author ningyu
     * @date 2012-10-22 下午5:13:24
     * @param j
     * @see
     */
    public void releaseLock(Jedis j1,Jedis j2) {
        releaseLock(j1,LOCK_KEY);
        releaseLock(j2,LOCK_KEY);
    }
    
    /**
     * <p>释放锁，戒掉释放对服务器的锁定标记</p> 
     * @author ningyu
     * @date 2012-10-22 下午5:13:58
     * @param j
     * @param lockKey
     * @see
     */
    public void releaseLock(Jedis j,String lockKey) {
        try {
            j.del(lockKey);
        } catch(JedisException e) {
            log.error(e.getMessage(),e);
        }
    }
    
    /**
     * 重试初始化client pool线程任务
     * 
     * @author ningyu
     * @date 2012-10-22 上午9:12:41
     * @see
     */
    private void retry() {
        if(retryTask == null) {
            retryTask = new RetryTask("重试初始化client pool任务");
            retryTask.setDaemon(true);
            retryTask.start();
        } else if (retryTask.getState().name().equals(State.NEW.name())){
            retryTask.start();
        } else if(retryTask.getState().name().equals(State.TERMINATED.name())) {
            retryTask = new RetryTask("重试初始化client pool任务");
            retryTask.setDaemon(true);
            retryTask.start();
        }
    }
    
    /**
     * 开启修复集群信息线程任务
     * @author ningyu
     * @date 2012-10-22 上午9:29:58
     * @see
     */
    private void repair() {
        if(!checkClusterNodes()) {
        	return;
        }
        if(repairTask == null) {
            repairTask = new RepairTask("重试修复集群信息任务");
            repairTask.setDaemon(true);
            repairTask.start();
        } else if (repairTask.getState().name().equals(State.NEW.name())){
            repairTask.start();
        } else if(repairTask.getState().name().equals(State.TERMINATED.name())) {
            repairTask = new RepairTask("重试修复集群信息任务");
            repairTask.setDaemon(true);
            repairTask.start();
        }
    }
    
    /**
     * 稍后重试任务
     * <p style="display:none">modifyRecord</p>
     * <p style="display:none">version:V1.0,author:ningyu,date:2012-10-22 上午9:09:26,content:TODO </p>
     * @author ningyu
     * @date 2012-10-22 上午9:09:26
     * @since
     * @version
     */
    class RetryTask extends Thread {
        int count = 1;
        public RetryTask(String name) {
            super(name);
            count = 1;
        }
        
        @SuppressWarnings("static-access")
        @Override
        public void run() {
            log.debug("重试创建redis client任务开始，延迟"+retryDelayTime+"后开始执行!");
            
            while(true) {
                
                try {
                    this.sleep(retryDelayTime);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(),e1);
                }
                log.debug("第"+count+"次尝试重新创建redis client!");
                count++;
                
                poolInited.set(false);
                
                jedis1 = new Jedis(shardInfo1);
                if(shardInfo2 != null) {
                	jedis2 = new Jedis(shardInfo2);
                	enableServer2 = true;
                }
                
                boolean connected1 = false;
                boolean connected2 = false;
                try {
                    jedis1.getClient().connect();
                    connected1 = jedis1.getClient().isConnected();
                } catch(JedisConnectionException e) {
                    connected1 = false;
                }
                if(enableServer2) {
                	try {
                		jedis2.getClient().connect();
                		connected2 = jedis2.getClient().isConnected();
                	} catch(JedisConnectionException e) {
                		connected2 = false;
                	}
                }
                String nodesMessage1 = null;
                String nodesMessage2 = null;
                if(enableServer2) {
                	if(connected1 && connected2) {
                		//1.两个服务都是好的
                		try {
                			nodesMessage1 = jedis1.get(CLUSTER_KEY);
                		} catch (JedisConnectionException e) {
                			log.error(e.getMessage(),e);
                			disconnect(jedis1);
                			disconnect(jedis2);
                			continue;// 稍后再试
                		}
                		try {
                			nodesMessage2 = jedis2.get(CLUSTER_KEY);
                		} catch (JedisConnectionException e) {
                			log.error(e.getMessage(),e);
                			disconnect(jedis1);
                			disconnect(jedis2);
                			continue;// 稍后再试
                		}
                		if(nodesMessage1 != null && nodesMessage2 != null && nodesMessage1.equals(nodesMessage2)) {
                			//1.两个服务都是好的，初始过了集群信息
                			initPool(nodesMessage1);
                			disconnect(jedis1);
                			disconnect(jedis2);
                			return;
                		} else {
                			//忽略错误的集群信息，2.两个服务都是好的，没有初始集群信息
                			boolean lock = getLock(jedis1,jedis2);
                			if(lock) {
                				//只有两个同时锁定时才去执行初始化
                				try {
                					Node node = initClusterNodes(jedis1,jedis2);
                					initPool(node);
                					releaseLock(jedis1);
                				} catch(JedisConnectionException e) {
                					log.error(e.getMessage(),e);
                					releaseLock(jedis1,jedis2);
                					continue;// 稍后再试
                				} finally {
                					disconnect(jedis1);
                					disconnect(jedis2);
                				}
                				return;
                			} else {
                				//只锁定了一个释放锁，稍后修复集群信息
                				releaseLock(jedis1,jedis2);
                				
                				Node master = new Node();
                				master.setHost(jedis1.getClient().getHost());
                				master.setPort(jedis1.getClient().getPort());
                				master.setTimeout(jedis1.getClient().getTimeout());
                				Node slave = new Node();
                				slave.setHost(jedis2.getClient().getHost());
                				slave.setPort(jedis2.getClient().getPort());
                				slave.setTimeout(jedis2.getClient().getTimeout());
                				master.setNext(slave);
                				slave.setPrevious(master);
                				try {
                					jedis1.slaveofNoOne();
                					initPool(master);
                					disconnect(jedis1);
                					disconnect(jedis2);
                				} catch(JedisConnectionException e) {
                					log.error(e.getMessage(),e);
                					disconnect(jedis1);
                					disconnect(jedis2);
                					continue;// 稍后再试
                				}
                				repair();
                				return;// 修复集群信息
                			}
                		}
                	} else if(connected1){
                		//只有一个服务是好的
                		//忽略现有的集群信息，不用集群信息初始化client，直接初始化client连接指向正常的redis服务，稍后修复集群信息保存到redis服务上
                		Node master = new Node();
                		master.setHost(jedis1.getClient().getHost());
                		master.setPort(jedis1.getClient().getPort());
                		master.setTimeout(jedis1.getClient().getTimeout());
                		Node slave = new Node();
                		slave.setHost(jedis2.getClient().getHost());
                		slave.setPort(jedis2.getClient().getPort());
                		slave.setTimeout(jedis2.getClient().getTimeout());
                		master.setNext(slave);
                		slave.setPrevious(master);
                		try {
                			jedis1.slaveofNoOne();
                			initPool(master);
                			disconnect(jedis1);
                			disconnect(jedis2);
                		} catch(JedisConnectionException e) {
                			log.error(e.getMessage(),e);
                			disconnect(jedis1);
                			disconnect(jedis2);
                			continue;// 稍后再试
                		}
                		repair();
                		return;// 修复集群信息
                	} else if(connected2) {
                		//只有一个服务是好的
                		//忽略现有的集群信息，不用集群信息初始化client，直接初始化client连接指向正常的redis服务，稍后修复集群信息保存到redis服务上
                		Node master = new Node();
                		master.setHost(jedis2.getClient().getHost());
                		master.setPort(jedis2.getClient().getPort());
                		master.setTimeout(jedis2.getClient().getTimeout());
                		Node slave = new Node();
                		slave.setHost(jedis1.getClient().getHost());
                		slave.setPort(jedis1.getClient().getPort());
                		slave.setTimeout(jedis1.getClient().getTimeout());
                		master.setNext(slave);
                		slave.setPrevious(master);
                		try {
                			jedis2.slaveofNoOne();
                			initPool(master);
                			disconnect(jedis1);
                			disconnect(jedis2);
                		} catch(JedisConnectionException e) {
                			log.error(e.getMessage(),e);
                			disconnect(jedis1);
                			disconnect(jedis2);
                			continue;// 稍后再试
                		}
                		repair();
                		return;// 修复集群信息
                	} else {
                		//如果没有一个服务是好的
                		continue;// 稍后再试
                	}
                } else {
                	if(connected1){
                		//只有一个服务是好的
                		//忽略现有的集群信息，不用集群信息初始化client，直接初始化client连接指向正常的redis服务，稍后修复集群信息保存到redis服务上
                		Node master = new Node();
                		master.setHost(jedis1.getClient().getHost());
                		master.setPort(jedis1.getClient().getPort());
                		master.setTimeout(jedis1.getClient().getTimeout());
                		try {
                			jedis1.slaveofNoOne();
                			initPool(master);
                			disconnect(jedis1);
                		} catch(JedisConnectionException e) {
                			log.error(e.getMessage(),e);
                			disconnect(jedis1);
                			continue;// 稍后再试
                		}
                	} else {
                		//如果没有一个服务是好的
                		continue;// 稍后再试
                	}
                }
            }
            
        }
    }
    
    /**
     * 修复集群信息任务
     * <p style="display:none">modifyRecord</p>
     * <p style="display:none">version:V1.0,author:ningyu,date:2012-10-22 上午9:09:38,content:TODO </p>
     * @author ningyu
     * @date 2012-10-22 上午9:09:38
     * @since
     * @version
     */
    class RepairTask extends Thread {
        int count = 1;
        private boolean running = true;
        public RepairTask(String name) {
            super(name);
            count = 1;
        }
        public void stopThread() {
            this.running = false;
        }
        
        @SuppressWarnings("static-access")
        @Override
        public void run() {
            log.debug("修复集群任务开始，延迟"+repairDelayTime+"后开始执行!");
            
            while(running) {
                try {
                    this.sleep(repairDelayTime);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(),e1);
                }
                
                log.debug("第"+count+"次尝试修复集群信息!");
                count++;
                
                if(!checkClusterNodes()) {
                    return;
                }
                Jedis masterJedis = getMasterJedis();
                Jedis slaveJedis = getSlaveJedis();
                boolean masterConnected = false;
                boolean slaveConnected = false;
                try {
                    masterJedis.getClient().connect();
                    masterConnected = masterJedis.getClient().isConnected();
                } catch(JedisConnectionException e) {
                    masterConnected = false;
                }
                try {
                    slaveJedis.getClient().connect();
                    slaveConnected = slaveJedis.getClient().isConnected();
                } catch(JedisConnectionException e) {
                    slaveConnected = false;
                }
                if(masterConnected && slaveConnected) {
                    String nodesMessage1 = null,nodesMessage2 = null;
                    //1.两个服务都是好的
                    try {
                        nodesMessage1 = masterJedis.get(CLUSTER_KEY);
                    } catch (JedisConnectionException e) {
                        log.error(e.getMessage(),e);
                        disconnect(masterJedis);
                    }
                    try {
                        nodesMessage2 = slaveJedis.get(CLUSTER_KEY);
                    } catch (JedisConnectionException e) {
                        log.error(e.getMessage(),e);
                        disconnect(slaveJedis);
                    }
                    if(nodesMessage1 != null && nodesMessage2 != null && nodesMessage1.equals(nodesMessage2)) {
                        //1.两个服务都是好的，初始过了集群信息
                        initPool(nodesMessage1);
                        disconnect(masterJedis);
                        disconnect(slaveJedis);
                        return;
                    } else {
                        initClusterNodes(masterJedis, slaveJedis);
                        log.debug("修复集群信息成功!");
                        return;
                    }
                } else if(!masterConnected && !slaveConnected) {
                    //如果两个服务都停止了，停止修复集群线程任务，开启重试初始化client pool线程任务
                    retry();
                    return;
                }
            }
        }
    }

    @Override
    public void destroy() {
        if(pool != null) {
            pool.destroy();
        }
    }
}
