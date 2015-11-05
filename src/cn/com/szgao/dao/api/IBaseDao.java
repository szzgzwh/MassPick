package cn.com.szgao.dao.api;

import org.hibernate.Session;

import com.couchbase.client.java.Bucket;

/**
 * 基础Dao层接口
 * 作用：数据传输层
 * 2015-2-11
 * @author xiongchangyi
 */
public interface IBaseDao {
	/**
	 * 连接CouchBase数据库
	 * 作用：连接库
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	public Bucket connectionCouchBase();
	/**
	 * 断连接CouchBase数据库
	 * 作用：断连接
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	public void closeConnectionCouchBase();
	// 共用打开Session
	public Session openSession();
	// 共用关闭Session
	public void closeSession();
		
}
