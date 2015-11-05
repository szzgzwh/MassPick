package cn.com.szgao.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.google.gson.Gson;

import cn.com.szgao.action.Test;
import cn.com.szgao.action.TextName;
import cn.com.szgao.dao.api.ICollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.NoticeVO;
import cn.com.szgao.dto.ObjectVO;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.util.CommonConstant;
/**
 * @author xcy
 * 搜集数据接口
 * @version v1.0
 * @since 2014-12-16
 */
public class CollectDataService implements ICollectDataService {
	/**
	 * 注入DAO层对象
	 * 作用：注入DAO层对象
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	private ICollectionDataDao iCollectionDataDao;
	private static Logger logger = LogManager.getLogger(CollectDataService.class.getName());
	public void setiCollectionDataDao(ICollectionDataDao iCollectionDataDao) {
		this.iCollectionDataDao = iCollectionDataDao;
	}

	
	/**
	 * 插入数据到CouchBase
	 * @param objectVO192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251192.168.0.251
	 * @throws Exception192.168.0.251
	 * @author xiongchangyi
	 * @since 2015-2-11
	 */
	public ObjectVO createDocument(ObjectVO objectVO)throws Exception{
		if("".equals(objectVO.getObjName()))
		{
			ObjectVO objVO = new ObjectVO();
			//返回1表示没有填路径
			objVO.setObjId((long)2);
			return objVO;
		}
		return iCollectionDataDao.createDocument(objectVO);
		
	}
	
	/**
	 * 处理企业名录的日期
	 * @param vo 文档路径
	 * @return
	 * @throws Exception
	 */
	public ObjectVO updateDate(ObjectVO vo)throws Exception{
		
		return iCollectionDataDao.updateDate(vo);
	}
	/**
	 * 批量处理Excel文件导入购置工商数据
	 * @author xiongchangyi
	 * @since 2015-3-13
	 * throws Exception
	 * @param objVO
	 * @return 
	 * @throws Exception
	 */
	public ObjectVO batchImportExcelFile(ObjectVO objVO)throws Exception{
		
		return iCollectionDataDao.batchImportExcelFile(objVO);
	}
	
	 public static void readFileByLines(String file) {
	        BufferedReader reader = null;
	        try {
	            System.out.println("以行为单位读取文件内容，一次读一整行：");
	            reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	            int line = 1;
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	                // 显示行号
	                System.out.println("line " + line + ": " + tempString);
	                line++;
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	    }
	/**
	 * 保存数据到JSON文档库 
	 */
	@Override
	public boolean createJsonData(List<String[]> list) throws Exception {
		if(null==list||list.size()<=0){
			return false;
		}
		//连接库
		Bucket bucket = CommonConstant.connectionCouchBase();
		JsonObject obj = null;	
		JsonDocument doc =null;
		int i=0;
		int sum=list.size();
		String urlId=null;
		try {
		for(i= 0;i<sum;i++)
		{
			urlId = CommonConstant.getUUID(list.get(i)[1]).toString();//需要指定URL生成UUID的字符串作为参数	
			
//				//标题，URL+，分类，字号，法院名+，发布日期+，省，市，区，采集时间			
				obj = JsonObject.empty().put("title",list.get(i)[0]).put("detailLink",list.get(i)[1])
						.put("catalog",list.get(i)[2]).put("caseNum",list.get(i)[3]).put("courtName", list.get(i)[4])
						.put("publishDate", list.get(i)[5]).put("province", list.get(i)[6]).put("city",list.get(i)[7])
						.put("area",list.get(i)[8]).put("collectDate",list.get(i)[9]);
				
				doc = JsonDocument.create(urlId, obj); //创建JSON文档	
				
				bucket.upsert(doc);	//插入JSON文档到库 
				
		}
		} catch (Exception e) {
			return false;
		}
		finally{
			bucket.close();
			obj=null;
		}
		return true;
	}
	
	/**
	 * 保存到postgresql
	 */
	@Override
	public boolean createPostgresql(List<String[]> list)  {	
		if(null==list||list.size()<=0){
			return false;
		}
		boolean reuslt=true;
		try {			
			if(list.size()<=CommonConstant.NUMBER){
				 long da=System.currentTimeMillis();
				 //iCollectionDataDao.createPostgresql(list);
				 reuslt=iCollectionDataDao.jdbcCreatePostgresql(list);
				 logger.info("第1批数据耗时:"+(System.currentTimeMillis()-da));
                 return reuslt;
			}
			else{
				int sum=list.size();
				int page=sum%CommonConstant.NUMBER;
				if(page==0){
					page=sum/CommonConstant.NUMBER;
				}
				else{
					page=sum/CommonConstant.NUMBER+1;
				}
				int index=0;
				int last=0;
				int first=0;
				List<String[]> listData=null;
				for(index=0;index<page;index++){
					first=index*CommonConstant.NUMBER;
					last=(index+1)*CommonConstant.NUMBER;
					if(last>=sum){
						last=sum;
						
					}
					long da=System.currentTimeMillis();
					//iCollectionDataDao.createPostgresql(list.subList(index*CommonConstant.NUMBER, last));
					logger.info("数据量:"+first+"--"+last);
					listData=list.subList(first, last);
					reuslt=iCollectionDataDao.jdbcCreatePostgresql(listData);
					logger.info("第"+(index+1)+"批数据耗时:"+(System.currentTimeMillis()-da));
					//listData.clear();
					if(!reuslt){
						return false;
					}
				}			
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}		
		return true;		
	}
	
	/**
	 * json文件保存数据到JSON文档库 
	 */
	@Override
	public boolean createJson(List<ArchivesVO> list) throws Exception {
		if(null==list||list.size()<=0){
			return false;
		}
		//连接库
		Bucket bucket = CommonConstant.connectionCouchBase();
		/*NameBasedGenerator nbg = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);*/
		JsonObject obj = null;	
		JsonDocument doc =null;
		int i=0;
		int sum=list.size();
		String urlId=null;
		try {
		for(i= 0;i<sum;i++)
		{
			obj=JsonObject.empty().put("title",list.get(i).getTitle())
					.put("detailLink",list.get(i).getDetailLink())
					.put("catalog",list.get(i).getCatalog())
					.put("caseNum",list.get(i).getCaseNum())
					.put("courtName", list.get(i).getCourtName())
					.put("publishDate", list.get(i).getPublishDate())
					.put("province", list.get(i).getProvince())
					.put("city",list.get(i).getCity())
					.put("area",list.get(i).getArea())
					.put("collectDate",list.get(i).getCollectDate());
				//需要指定URL生成UUID的字符串作为参数	
				 urlId = CommonConstant.getUUID(list.get(i).getDetailLink()).toString();
				//创建JSON文档	
				doc = JsonDocument.create(urlId, obj);
				//插入JSON文档到库 
				bucket.upsert(doc);	
				
		}
		} catch (Exception e) {
			return false;
		}
		finally{
			bucket.close();
			obj=null;
		}
		return true;
	}
	
	
	/**json插入
	 * 保存到postgresql
	 */
	@Override
	public boolean createJSONInsertPostgresql(List<ArchivesVO> list)  {	
		if(null==list||list.size()<=0){
			return false;
		}
		boolean reuslt=true;
		try {			
			if(list.size()<=CommonConstant.NUMBER){
				 long da=System.currentTimeMillis();
				 //iCollectionDataDao.createPostgresql(list);
				 reuslt=iCollectionDataDao.jdbcCreateJSONPostgresql(list);
				 logger.info("第1批数据耗时:"+(System.currentTimeMillis()-da));
                 return reuslt;
			}
			else{
				int sum=list.size();
				int page=sum%CommonConstant.NUMBER;
				if(page==0){
					page=sum/CommonConstant.NUMBER;
				}
				else{
					page=sum/CommonConstant.NUMBER+1;
				}
				int index=0;
				int last=0;
				int first=0;
				List<ArchivesVO> listData=null;
				for(index=0;index<page;index++){
					first=index*CommonConstant.NUMBER;
					last=(index+1)*CommonConstant.NUMBER;
					if(last>=sum){
						last=sum;
						
					}
					long da=System.currentTimeMillis();
					//iCollectionDataDao.createPostgresql(list.subList(index*CommonConstant.NUMBER, last));
					logger.info("数据量:"+first+"--"+last);
					listData=list.subList(first, last);
					reuslt=iCollectionDataDao.jdbcCreateJSONPostgresql(listData);
					logger.info("第"+(index+1)+"批数据耗时:"+(System.currentTimeMillis()-da));
					//listData.clear();
					if(!reuslt){
						return false;
					}
				}			
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}		
		return true;		
	}
	
	
	/**json插入
	 * 保存到postgresql
	 */
	@Override
	public boolean createJSONPostgresql(List<ArchivesVO> list)  {	
		if(null==list||list.size()<=0){
			return false;
		}
		boolean reuslt=true;
		try {			
			if(list.size()<=CommonConstant.NUMBER){
				 long da=System.currentTimeMillis();
				 //iCollectionDataDao.createPostgresql(list);
				 reuslt=iCollectionDataDao.jdbcCreateJSONPostgresql(list);
				 logger.info("第1批数据耗时:"+(System.currentTimeMillis()-da));
                 return reuslt;
			}
			else{
				int sum=list.size();
				System.out.println("********sum***********"+sum);
				int page=sum%CommonConstant.NUMBER;
				if(page==0){
					page=sum/CommonConstant.NUMBER;
				}
				else{
					page=sum/CommonConstant.NUMBER+1;
				}
				int index=0;
				int last=0;
				int first=0;
				Iterable<ArchivesVO> listData=null;
				for(index=0;index<page;index++){
					first=index*CommonConstant.NUMBER;
					last=(index+1)*CommonConstant.NUMBER;
					if(last>=sum){
						last=sum;
						
					}
					long da=System.currentTimeMillis();
					//iCollectionDataDao.createPostgresql(list.subList(index*CommonConstant.NUMBER, last));
					logger.info("数据量:"+first+"--"+last);
					listData=list.subList(first, last);
					reuslt=iCollectionDataDao.jdbcCreateJSONPostgresql(listData);
					logger.info("第"+(index+1)+"批数据耗时:"+(System.currentTimeMillis()-da));
					//listData.clear();
					if(!reuslt){
						return false;
					}
				}			
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}		
		return true;		
	}
	public boolean createJdbcPostgresql(List<String[]> list) throws  Exception {
		iCollectionDataDao.jdbcCreatePostgresql2(list);
		return true;
	}
	/***
	 * 公告抓取
	 */
	@Override
	public boolean updateJsonDataNotice(List<NoticeVO> list)
			throws Exception {
		if(null==list||list.size()<=0){
			return false;
		}
		//连接库
		Bucket bucket = CommonConstant.connectionCouchBaseNotice();
		JsonDocument doc=null;
		JsonObject obj=null;		
//		Gson gson=new Gson();
//		NoticeVO notices = null;
//		List<NoticeVO> noticelist = new ArrayList<NoticeVO>();
		try {
		for(NoticeVO notice:list)
		{	
			//查询数据	
			doc = JsonDocument.create(notice.getUuid());
			obj =bucket.get(doc)==null?null:bucket.get(doc).content();
			if(obj == null){
				logger.info("匹配不到UUID:"+notice.getUuid());
				continue;	
			}
			//获取couchbase中已有的数据
			if(null!=obj.get("detailLink")&&!"".equals(obj.get("detailLink").toString())){				
				notice.setDetailLink(obj.get("detailLink").toString());
			}
			if(null!=obj.get("pubDate")&&!"".equals(obj.get("pubDate").toString())){				
				notice.setPubDate(getReplaceAllDate(obj.get("pubDate").toString()));
			}
			if(null!=obj.get("province")&&!"".equals(obj.get("province").toString())){				
				notice.setProvince(obj.get("province").toString());
			}
			if(null!=obj.get("city")&&!"".equals(obj.get("city").toString())){				
				notice.setCity(obj.get("city").toString());
			}
			if(null!=obj.get("area")&&!"".equals(obj.get("area").toString())){				
				notice.setArea(obj.get("area").toString());
			}
			if(null!=obj.get("collectTime")&&!"".equals(obj.get("collectTime").toString())){				
				notice.setCollectTime(getReplaceAllDate(obj.get("collectTime").toString()));
			}
			if(null!=obj.get("client")&&!"".equals(obj.get("client").toString())){				
				notice.setClient(obj.get("client").toString());
			}
			if(null!=obj.get("pubPerson")&&!"".equals(obj.get("pubPerson").toString())){				
				notice.setPubPerson(obj.get("pubPerson").toString());
			}
			if(null!=obj.get("pdfLink")&&!"".equals(obj.get("pdfLink").toString())){				
				notice.setPdfLink(obj.get("pdfLink").toString());
			}
			if(null!=obj.get("isCourtPub")&&!"".equals(obj.get("isCourtPub").toString())){				
				notice.setIsCourtPub(obj.get("isCourtPub").toString());
			}
			
			//将值放入对应字段中
			obj = JsonObject.empty().put("PubType", notice.getPubType()==null?"":notice.getPubType()).put("detailLink",notice.getDetailLink() ==null?"":notice.getDetailLink())
					.put("pdfLink", notice.getPdfLink()==null?"":notice.getPdfLink()).put("province", notice.getProvince()==null?"":notice.getProvince())
					.put("city", notice.getCity()==null?"":notice.getCity()).put("area", notice.getArea()==null?"":notice.getArea())
					.put("pubPerson", notice.getPubPerson()==null?"":notice.getPubPerson()).put("client", notice.getClient()==null?"":notice.getClient())
					.put("pubDate", notice.getPubDate()==null?"":notice.getPubDate()).put("collectTime", notice.getCollectTime()==null?"":notice.getCollectTime())
					.put("PubContent", notice.getPubContent()==null?"":notice.getPubContent()).put("isCourtPub", notice.getIsCourtPub()==null?"":notice.getIsCourtPub());
			//创建JSON文档	
			doc= JsonDocument.create(notice.getUuid(), obj);
			bucket.upsert(doc);											
		}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		finally{
			bucket.close();
			obj=null;
			doc=null;
			
		}
		return true;
	}
	
	/**
	 * 统一日期格式
	 * @param value
	 * @return
	 */
	 public static String getReplaceAllDate(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			    value=value.replaceAll("[（,）,(,),[,],{,},<,>]","");
			    value=value.replaceAll("[-,-,.,/,\"]","-");
			    value=value.replaceAll("[:,：]", ":");
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
	 }
	
	@Override
	public boolean updateJsonData(List<ArchivesVO> list) throws Exception {
		
		if(null==list||list.size()<=0){
			return false;
		}
		//连接库
		Bucket bucket = CommonConstant.connectionCouchBase();
		JsonDocument doc=null;
		JsonObject obj2=null;	
		com.google.gson.JsonObject json=null;
		Gson gson=new Gson();
		ArchivesVO archs = null;
		try {
		for(ArchivesVO arch:list)
		{	
			//查询数据	
			doc = JsonDocument.create(arch.getUuid());
			obj2 =bucket.get(doc)==null?null:bucket.get(doc).content();
			if(obj2==null){
				logger.info("匹配不到UUID:"+arch.getUuid());
				continue;	
			}
			archs = new ArchivesVO();
			json=gson.fromJson(obj2.toString(), com.google.gson.JsonObject.class);
			archs = gson.fromJson(json, ArchivesVO.class);
			if(null!=obj2.get("title")&&!"".equals(obj2.get("title"))){				
				archs.setTitle(obj2.get("title").toString());
			}			
			if(null!=obj2.get("caseNum")&&!"".equals(obj2.get("caseNum"))){				
				archs.setCaseNum(obj2.get("caseNum").toString());
			}
			if(null!=obj2.get("courtName")&&!"".equals(obj2.get("courtName"))){				
				archs.setCourtName(obj2.get("courtName").toString());
			}
			if(null!=obj2.get("catalog")&&!"".equals(obj2.get("catalog"))){				
				archs.setCatalog(obj2.get("catalog").toString());
			}
			if(null!=obj2.get("publishDate")&&!"".equals(obj2.get("publishDate"))){				
				archs.setPublishDate(getReplaceAll(obj2.get("publishDate").toString()));
			}
			if(null!=obj2.get("collectDate")&&!"".equals(obj2.get("collectDate"))){				
				archs.setCollectDate(getReplaceAll(obj2.get("collectDate").toString()));
			}
			if(null != arch.getTitle() && !"".equals(arch.getTitle())){
				archs.setTitle(arch.getTitle());
			}
			if(null != arch.getSuitType() && !"".equals(arch.getSuitType())){
				archs.setCatalog(arch.getSuitType());
			}
			if(null != arch.getCaseNum() && !"".equals(arch.getCaseNum())){
				archs.setCaseNum(arch.getCaseNum());
			}
			if(null != arch.getCourtName() && !"".equals(arch.getCourtName())){
				archs.setCourtName(arch.getCourtName());
			}
			if(null != arch.getPlaintiff() && !"".equals(arch.getPlaintiff())){
				archs.setPlaintiff(arch.getPlaintiff());
			}
			if(null != arch.getDefendant() && !"".equals(arch.getDefendant())){
				archs.setDefendant(arch.getDefendant());
			}
			if(null != arch.getApproval() && !"".equals(arch.getApproval())){
				archs.setApproval(arch.getApproval());
			}
			if(null != arch.getSuitType() && !"".equals(arch.getSuitType())){
				archs.setSuitType(arch.getSuitType());
			}
			if(null != arch.getApprovalDate() && !"".equals(arch.getApprovalDate())){
				archs.setApprovalDate(arch.getApprovalDate());
			}
			if(null != arch.getCaseCause() && !"".equals(arch.getCaseCause())){
				archs.setCaseCause(arch.getCaseCause());
			}
			if(null != arch.getSummary() && !"".equals(arch.getSummary())){
				archs.setSummary(arch.getSummary());
			}
			String jsonss=gson.toJson(archs);
			doc = JsonDocument.create(arch.getUuid(),JsonObject.fromJson(jsonss));
			bucket.upsert(doc);	
			//标题，URL+，分类，字号，法院名+，发布日期+，省，市，区，采集时间			
			/*已废弃，原来插入couchbase方式
			 obj2 = JsonObject.empty().put("title",arch.getTitle()).put("detailLink",obj2.get("detailLink"))
					.put("catalog",arch.getCatalog()).put("caseNum",arch.getCaseNum()).put("courtName",arch.getCourtName())
					.put("publishDate", arch.getPublishDate()).put("province", obj2.get("province")).put("city",obj2.get("city"))
					.put("area",obj2.get("area")).put("collectDate",arch.getCollectDate())
					.put("plaintiff",arch.getPlaintiff()).put("defendant",arch.getDefendant())
					.put("approval",arch.getApproval()).put("suitDate", arch.getSuitDate())
					.put("approvalDate", arch.getApprovalDate()).put("caseCause", arch.getCaseCause()).put("summary",arch.getSummary());
			//创建JSON文档	
			doc= JsonDocument.create(arch.getUuid(), obj2);
			bucket.upsert(doc);	*/										
		}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		finally{
			bucket.close();
			gson = null;
			json = null;
			archs = null;
			obj2=null;
			doc=null;
		}
		return true;
	}
	
	/**
	 * 统一日期格式
	 * @param value
	 * @return
	 */
	 public static String getReplaceAll(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			    value=value.replaceAll("[（,）,(,),[,],{,},<,>]","");
			    value=value.replaceAll("[-,-,/,\"],年,月,日","-");
			    value=value.replaceAll("[:,：]", ":");
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
	 }
	@Override
	public boolean updateJsonSingleData(List<ArchivesVO> list) throws Exception {
		
		if(null==list||list.size()<=0){
			return false;
		}
		//连接库
		Bucket bucket = CommonConstant.connectionCouchBase();
		JsonDocument doc=null;
		JsonObject obj2=null;		
		try {
		for(ArchivesVO arch:list)
		{	
			//查询数据	
			doc = JsonDocument.create(arch.getUuid());
			obj2 =bucket.get(doc)==null?null:bucket.get(doc).content();
			if(obj2==null){
				logger.info("匹配不到UUID:"+arch.getUuid());
				continue;	
			}
			logger.info("detailLink:"+obj2.get("detailLink"));
			logger.info("title:"+obj2.get("title"));
			//logger.info("area:"+obj2.get("area"));
			logger.info("courtName:"+obj2.get("courtName"));
			logger.info("catalog:"+obj2.get("catalog"));
			logger.info("province:"+obj2.get("province"));
			logger.info("publishDate:"+obj2.get("publishDate"));
			//logger.info("caseNum:"+obj2.get("caseNum"));
			//logger.info("city:"+obj2.get("city"));
			logger.info("caseNum:"+obj2.get("caseNum"));
			logger.info("approvalDate:"+obj2.get("approvalDate"));
			logger.info("defendant:"+obj2.get("defendant"));
			logger.info("plaintiff:"+obj2.get("plaintiff"));
			logger.info("approval:"+obj2.get("approval"));
			logger.info("caseCause:"+obj2.get("caseCause"));
			/*if(null!=obj2.get("title")&&!"".equals(obj2.get("title").toString())){				
				arch.setTitle(obj2.get("title").toString());
			}			
			if(null!=obj2.get("caseNum")&&!"".equals(obj2.get("caseNum").toString())){				
				arch.setCaseNum(obj2.get("caseNum").toString());
			}
			if(null!=obj2.get("courtName")&&!"".equals(obj2.get("courtName").toString())){				
				arch.setCourtName(obj2.get("courtName").toString());
			}
			if(null!=obj2.get("catalog")&&!"".equals(obj2.get("catalog").toString())){				
				arch.setCatalog(obj2.get("catalog").toString());
			}
			else{
				arch.setCatalog(arch.getSuitType());
			}*/
			//标题，URL+，分类，字号，法院名+，发布日期+，省，市，区，采集时间			
			obj2 = JsonObject.empty().put("title",arch.getTitle()).put("detailLink",obj2.get("detailLink"))
					.put("catalog",arch.getSuitType()).put("caseNum",arch.getCaseNum()).put("courtName",arch.getCourtName())
					.put("publishDate", obj2.get("publishDate")).put("province", obj2.get("province")).put("city",obj2.get("city"))
					.put("area",obj2.get("area")).put("collectDate",obj2.get("collectDate")).put("plaintiff",arch.getPlaintiff()).put("defendant",arch.getDefendant())
					.put("approval",arch.getApproval()).put("suitDate", arch.getSuitDate())
					.put("approvalDate", arch.getApprovalDate()).put("caseCause", arch.getCaseCause()).put("summary",arch.getSummary());
			//创建JSON文档	
			doc= JsonDocument.create(arch.getUuid(), obj2);
			bucket.upsert(doc);											
		}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		finally{
			bucket.close();
			obj2=null;
			doc=null;
		}
		return true;
	}
}
