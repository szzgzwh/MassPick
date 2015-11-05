package cn.com.szgao.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

import cn.com.szgao.dao.api.IBaseDao;
/**
 * 基础Dao层
 * 作用：数据传输层
 * 2015-2-11
 * @author xiongchangyi
 */
public class BaseDao implements IBaseDao {
	//群对象
	private Cluster cluster = null;
	//桶对象
	private Bucket bucket = null;
	//会话对象
	private Session session;
	// 定义sf对象，方便注入进来，必须要有getter/setter方法
	private SessionFactory sf;
	
	public SessionFactory getSf() {
		return sf;
	}
	 
	public void setSf(SessionFactory sf) {
		this.sf = sf;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	//获得当前会话对象
	public Session getCurrentSession(){
		return sf.getCurrentSession();
	}
	
	// 共用打开Session
	public Session openSession(){
		return sf.openSession();
	}
	// 共用关闭Session
	public void closeSession(){
		session.close();
	}
	/**
	 * 连接CouchBase数据库
	 * 作用：连接库
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	public Bucket connectionCouchBase(){
		//连接服务器
		cluster = CouchbaseCluster.create("192.168.0.252");
		//连接指定的桶
		bucket = cluster.openBucket("court");		
		return bucket;
	}
	/**
	 * 断连接CouchBase数据库
	 * 作用：断连接
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	public void closeConnectionCouchBase(){
		cluster.disconnect();
		bucket.close();
	}
}
