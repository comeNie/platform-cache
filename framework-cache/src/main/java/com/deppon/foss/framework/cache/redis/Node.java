package com.deppon.foss.framework.cache.redis;

import redis.clients.jedis.Jedis;


/**
 * redis集群节点信息
 * <p style="display:none">modifyRecord</p>
 * <p style="display:none">version:V1.0,author:ningyu,date:2013-1-26 下午3:49:07,content:TODO </p>
 * @author ningyu
 * @date 2013-1-26 下午3:49:07
 * @since
 * @version
 */
public class Node {

	private String name = null;
	private int timeout = 5;
	private String host;
	private int port = 6379;
	private String password = null;
	private int weight = 1;

	private Node previous;
	private Node next;

	/**
	 * 超时时间
	 * @author ningyu
	 * @date 2013-1-26 下午3:49:20
	 * @return
	 * @see
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * 该节点的前一个集群节点
	 * @author ningyu
	 * @date 2013-1-26 下午3:49:32
	 * @return
	 * @see
	 */
	public Node getPrevious() {
		return previous;
	}

	public void setPrevious(Node previous) {
		this.previous = previous;
	}

	/**
	 * 该节点的下一个集群节点
	 * @author ningyu
	 * @date 2013-1-26 下午3:49:48
	 * @return
	 * @see
	 */
	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * redis地址
	 * @author ningyu
	 * @date 2013-1-26 下午3:50:03
	 * @return
	 * @see
	 */
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * redis端口
	 * @author ningyu
	 * @date 2013-1-26 下午3:50:17
	 * @return
	 * @see
	 */
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * redis密码
	 * @author ningyu
	 * @date 2013-1-26 下午3:50:24
	 * @return
	 * @see
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 集群名称 
	 * @author ningyu
	 * @date 2013-1-26 下午3:50:40
	 * @return
	 * @see
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 集群权重，目前没有实现
	 * @author ningyu
	 * @date 2013-1-26 下午3:50:49
	 * @return
	 * @see
	 */
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * 将node对象转成字符串描述
	 * @author ningyu
	 * @date 2013-1-26 下午3:48:08
	 * @param head
	 * @return
	 * @see
	 */
	public static String marshal(Node head) {
		Node node = null;
		if (head == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		node = head;
		sb.append("[");
		sb.append(passJson(node));
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * 返回json字符串
	 * @author ningyu
	 * @date 2013-1-26 下午3:47:45
	 * @param node
	 * @return
	 * @see
	 */
	private static String passJson(Node node) {
	    StringBuilder sb = new StringBuilder();
	    if (node != null) {
            sb.append("{");
            sb.append("host:" + node.getHost()).append(", ");
            sb.append("port:" + node.getPort()).append(", ");
            sb.append("password:" + node.getPassword()).append(", ");
            sb.append("weight:" + node.getWeight()).append(", ");
            sb.append("name:" + node.getName()).append(", ");
            sb.append("timeout:" + node.getTimeout());
            sb.append("}");
            node = node.getNext();
            if (node != null) {
                sb.append(",");
                sb.append(passJson(node));
            }
        }
	    return sb.toString();
	}

	/**
	 * 将字符串描述转换成node对象
	 * @author ningyu
	 * @date 2013-1-26 下午3:47:28
	 * @param msg
	 * @return
	 * @see
	 */
	public static Node unmarshal(String msg) {
		Node head = null;
		Node node = null;
		String str = msg.substring(1, msg.length() - 1);
		String[] nodes = str.split("\\},\\{");
		for (String s : nodes) {
			String[] ps = s.split(",");
			Node cur = new Node();

			cur.setHost(ps[0].split(":")[1]);
			cur.setPort(Integer.valueOf(ps[1].split(":")[1]));

			String password = ps[2].split(":")[1];
			cur.setPassword(password.equals("null") ? null : password);

			cur.setWeight(Integer.valueOf(ps[3].split(":")[1]));

			String name = ps[4].split(":")[1];
			cur.setName(name.equals("null") ? null : name);
			
			String timeout = ps[5].split(":")[1];
			if (timeout.endsWith("}")) {
			    timeout = timeout.substring(0, timeout.length() - 1);
            }
			cur.setTimeout(Integer.valueOf(timeout));
			if (node == null) {
				node = cur;
				head = node;
			} else {
				node.setNext(cur);
				cur.setPrevious(node);
				node = cur;
			}
		}
		return head;
	}

	/** 
	 * 判断两个node对象中host和port是否相等
	 * @author ningyu
	 * @date 2013-1-26 下午3:46:50
	 * @param anObject
	 * @return 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object anObject) {
	    if (this == anObject) {
	        return true;
	    }
	    if(anObject instanceof Node) {
	        Node node = (Node) anObject;
	        if (this.getHost().equals(node.getHost())
	                && (this.getPort() == node.getPort())) {
	            return true;
	        }
	    }
		return false;
	}

    /** 
     * node对象的hashcode算法
     * @author ningyu
     * @date 2013-1-26 下午3:47:05
     * @return 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hostHash = this.getHost().hashCode();
        int portHash = this.getPort();
        int[] array = {hostHash,portHash};
        int result = 0; //注意，此处初始值为0
        for (int element : array) {
            result = 31 * result + element;
        }
        return result;
    }

    /**
     * 执行redis ping操作
     * @author ningyu
     * @date 2013-1-26 下午3:51:07
     * @param node
     * @return
     * @see
     */
    public static boolean pingNode(Node node) {
		Jedis j = null;
		try {
			j = new Jedis(node.getHost(), node.getPort());
			String re = j.ping();
			return re.equals("PONG");
		} catch (Exception e) {
			return false;
		} finally {
		    if(j != null) {
		        j.disconnect();
		    }
		}
	}
}
