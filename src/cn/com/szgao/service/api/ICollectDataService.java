package cn.com.szgao.service.api;

import java.util.List;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.NoticeVO;
import cn.com.szgao.dto.ObjectVO;

/**
 * @author xiongchangyi
 * 搜集数据接口
 * @version v1
 * @since 2014-12-16
 */

public interface ICollectDataService {
	
	/**
	 * 插入数据到CouchBase
	 * @param objectVO
	 * @throws Exception
	 * @author xiongchangyi
	 * @since 2015-2-11CollectionDataDao
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
	 * 保存数据到JSON文档库 
	 */
	public boolean createJsonData(List<String[]> list)throws Exception;
	/**
	 * 修改json文件库数据
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public boolean updateJsonData(List<ArchivesVO> list)throws Exception;
	/**
	 * 保存到postgresql
	 */
	public boolean createPostgresql(List<String[]> list)throws Exception;
	/**
	 * 用JDBC保存数据
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public boolean createJdbcPostgresql(List<String[]> list) throws  Exception;
	/**
	 * 手动更新pg数据
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public boolean updateJsonSingleData(List<ArchivesVO> list) throws Exception ;

	boolean createJson(List<ArchivesVO> list) throws Exception;

	boolean createJSONPostgresql(List<ArchivesVO> list);
	/**
	 * 公告修改couchbase
	 * @param listarchs
	 * @return
	 * @throws Exception
	 */
	public boolean updateJsonDataNotice(List<NoticeVO> listarchs) throws Exception;
	boolean createJSONInsertPostgresql(List<ArchivesVO> list);
}
