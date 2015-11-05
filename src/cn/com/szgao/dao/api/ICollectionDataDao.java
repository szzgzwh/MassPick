package cn.com.szgao.dao.api;


import java.sql.SQLException;
import java.util.List;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.ObjectVO;

import com.couchbase.client.java.Bucket;

/**
 * Dao层接口
 * 作用：数据传输层
 * 2015-2-11
 * @author xiongchangyi
 */

public interface ICollectionDataDao extends IBaseDao {
	
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
	
	/**
	 * 插入数据到CouchBase
	 * @param objectVO
	 * @throws Exception
	 * @author xiongchangyi
	 * @since 2015-2-11
	 */
	public ObjectVO createDocument(ObjectVO objectVO)throws Exception;
	/**
	 * 处理企业名录的日期
	 * @param vo 文档路径
	 * @return
	 * @throws Exception
	 */
	public ObjectVO updateDate(ObjectVO vo)throws Exception;
	
	/**
	 * 批量处理Excel文件导入购置工商数据
	 * @author xiongchangyi
	 * @since 2015-3-13
	 * throws Exception
	 * @param objVO
	 * @return 
	 * @throws Exception
	 */
	public ObjectVO batchImportExcelFile(ObjectVO objVO)throws Exception;
	/**
	 * 保存到postgresql
	 */
	public boolean createPostgresql(List<String[]> list)throws Exception;
	/**
	 * jdbc写数据
	 */
	public boolean jdbcCreatePostgresql(List<String[]> list)throws Exception;
	/**
	 * jdbc写数据2
	 * @param list
	 * @throws SQLException
	 */
	public boolean jdbcCreatePostgresql2(List<String[]> list) throws Exception;
	public boolean jdbcCreateJSONPostgresql(List<ArchivesVO> list) throws Exception;
	public boolean jdbcCreateJSONPostgresql(Iterable<ArchivesVO> listData);
}
