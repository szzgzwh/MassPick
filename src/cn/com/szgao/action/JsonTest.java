package cn.com.szgao.action;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.sql.DATE;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import cn.com.szgao.dao.impl.CollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.RecordData;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;
import cn.com.szgao.util.CommonConstant;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.google.gson.Gson;
public class JsonTest {
	/**
	 * 写日志
	 */
	private static Logger logger = LogManager.getLogger(JsonTest.class.getName());
	static ApplicationContext application=new ClassPathXmlApplicationContext("classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory=(SessionFactory)application.getBean("sessionFactory");	
	static Map<String,List<RecordData>> MAPS=new HashMap<String,List<RecordData>>();
	static long ERRORSUM=0;	//出错数据条数
	static long INPUTSUM=0;	//
	static long REPEATSUM=0;	//去重后数据条数
//	private static int count;
	/**
	 * 裁判文书
	 * 数据写库PostgreSql和couchbase
	 * JSON导入extracl_url_t表和court桶
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {	
		//清空不用的文件或日志
				File filepor=new File("E:\\Company_File\\log4j-0914\\Java1\\batchImport.log");   
				if(filepor.exists()){
					filepor.delete();			
				}
				filepor=null;
		long da=System.currentTimeMillis();
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");    
		//导入文件地址
		File file=new File("F:\\DataSheet\\7.16补上json\\JSON\\JSON\\上海市\\上海法院法院文书检索中心-刑事3.json");
		Bucket bucket = CommonConstant.connectionCouchBase();
		try {
			show(file,bucket);	
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		finally{
			sessionFactory.close();
			file = null;
			bucket.close();
//			caseNumIsNull.main(args);
		}
//		logger.info("数量："+count);
		logger.info("所有文件总耗时"+(((System.currentTimeMillis()-da)/1000)/60)+"分钟");
		record();
	}
	/**
	 * 递归遍历文件
	 * @param file
	 * @throws  
	 * @throws Exception 
	 */
	private static void show(File file,Bucket bucket) throws Exception{
		try {
			if(file.isFile()){
				long da=System.currentTimeMillis();				
				create(file,bucket);
				logger.info("读取<<"+file.getName()+">>文件耗时"+(System.currentTimeMillis()-da)+"毫秒");
				return;
			}
			File[] files=file.listFiles();
			System.out.println("----files---" + files);
			for(File fi:files){
				if(fi.isFile()){
					long da=System.currentTimeMillis();		
					String name=fi.getParentFile().getPath();
					name=name.substring(name.lastIndexOf("\\")+1,name.length());			
					create(fi,bucket);
					logger.info("读取"+name+"<<"+fi.getName()+">>文件耗时"+(System.currentTimeMillis()-da)+"毫秒");
				}
				else if(fi.isDirectory()){
					show(fi,bucket);
				}
				else{
					continue;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 写数据   
	 * @param <JSONObject>
	 * @param file
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	private static <ObjectDataVO, JSONObject> void create(File file,Bucket bucket) throws Exception, UnsupportedEncodingException{
/*		CollectionDataDao dataDao=new CollectionDataDao();
		dataDao.setSf(sessionFactory);
		CollectDataService service=new CollectDataService();
		service.setiCollectionDataDao(dataDao);		
		ICollectDataService services=service;*/
		String name=file.getParentFile().getPath();
		name=name.substring(name.lastIndexOf("\\")+1,name.length());
		 BufferedReader reader = null;
	        Gson gson = new Gson();	
	        ArchivesVO arch = null;
			List<ArchivesVO> list =  new ArrayList<ArchivesVO>();
			 String temp = null;
			 int sum = 0;
			 int count = 0;
		try {
//			logger.info("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            while ((temp = reader.readLine()) != null) {
            	arch = gson.fromJson(temp, ArchivesVO.class);
            	arch.setUuid(CommonConstant.getUUID(arch.getDetailLink().toString()));
            	list.add(arch);
            	list = JsonTest.removeDuplicate(list); //方法去重
            	if(list.size() >= 1000){
            		count++;
            		sum=sum+list.size();
            		boolean result=JsonTest.createJsonPostgreSQL(list,bucket);
            		REPEATSUM+=list.size();
            		if(!result){			
            			logger.error("读取"+name+"<<"+file.getName()+">>文件时发生JSON异常!");
            			ERRORSUM+=sum;
            		}
            		logger.info("第："+count+"批数据："+list.size());
//            	if(result){
//            		boolean result2=services.createJSONInsertPostgresql(list);
//            		if(!result2){
//            			logger.error("读取"+name+"<<"+file.getName()+">>文件时发生SQL异常!");
//            			ERRORSUM+=sum;
//            		}else{
//            			INPUTSUM+=list.size();
//            		}
//            	}
            		arch = null;
            		temp = null;
            		list = null;
            		list = new ArrayList<ArchivesVO>();
            	}
            }
            if(list.size() >= 0){
    		count++;
    		sum=sum+list.size();
    		boolean result=JsonTest.createJsonPostgreSQL(list,bucket);
    		REPEATSUM+=list.size();
    		if(!result){			
    			logger.error("读取"+name+"<<"+file.getName()+">>文件时发生JSON异常!");
    			ERRORSUM+=sum;
    		}
    		logger.info("第："+count+"批数据："+list.size());
//    	if(result){
//    		boolean result2=services.createJSONInsertPostgresql(list);
//    		if(!result2){
//    			logger.error("读取"+name+"<<"+file.getName()+">>文件时发生SQL异常!");
//    			ERRORSUM+=sum;
//    		}else{
//    			INPUTSUM+=list.size();
//    		}
//    	}
    		temp = null;
    		list = null;
    		list = new ArrayList<ArchivesVO>();
            }
            	statisticalCount(file,sum);
		} catch (Exception e) {
			logger.error("读取"+name+"<<"+file.getName()+">>文件时发生IO异常:"+e.getMessage());			
		}
		finally{
			logger.info(name+"<<"+file.getName()+">>记录条数为："+sum);
			reader.close();
			file=null;
			reader.close();
		}	
	}
	/**
	 * 入库couchbase
	 * @param arch
	 * @param urlId
	 * @return
	 * @throws Exception
	 */
	public static boolean createJsonPostgreSQL(List<ArchivesVO> list,Bucket bucket) throws Exception {
		JsonObject obj = null;	
		JsonDocument doc =null;
		String urlId = null;
		com.google.gson.JsonObject json=null;
		Gson gson=new Gson();
		ArchivesVO archs = null;
		for(int i=0;i<list.size();i++){
			urlId=CommonConstant.getUUID(list.get(i).getDetailLink().toString());
			doc = JsonDocument.create(urlId);
			obj = bucket.get(doc) == null ? null : bucket.get(doc).content();
			if (obj == null) {
//				logger.info("匹配不到UUID:" + urlId);
				continue;
			}
			archs = new ArchivesVO();
			json=gson.fromJson(obj.toString(), com.google.gson.JsonObject.class);
			archs = gson.fromJson(json, ArchivesVO.class);
			logger.info("UUID:"+urlId);
			archs.setDetailLink(list.get(i).getDetailLink());
			if (null != obj.get("title") && !"".equals(obj.get("title"))) {
				archs.setTitle(obj.get("title").toString());// 标题
			}
			if(list.get(i).getTitle() != null && !"".equals(list.get(i).getTitle())){
			archs.setTitle(list.get(i).getTitle());
			}
			///////////////////诉讼类型
			if (null != obj.get("suitType") && !"".equals(obj.get("suitType"))) {
				archs.setSuitType(obj.get("suitType").toString());// 诉讼类型
			}
			if (null != list.get(i).getSuitType() && !"".equals(list.get(i).getSuitType())) {
				archs.setSuitType(list.get(i).getSuitType());// 诉讼类型
			}
			///////////////////分类
			if (null != obj.get("catalog") && !"".equals(obj.get("catalog"))) {
				archs.setCatalog(obj.get("catalog").toString());// 分类
			}
			if (null != list.get(i).getCatalog() && !"".equals(list.get(i).getCatalog())) {
				archs.setCatalog(list.get(i).getCatalog());
			}
			///////////////////案号
			if (null != obj.get("caseNum") && !"".equals(obj.get("caseNum"))) {
				archs.setCaseNum(obj.get("caseNum").toString());// 案号
			}
			if (null != list.get(i).getCaseNum() && !"".equals(list.get(i).getCaseNum())) {
				archs.setCaseNum(list.get(i).getCaseNum());
			}
			///////////////////法院名
			if (null != obj.get("courtName") && !"".equals(obj.get("courtName"))) {
				archs.setCourtName(obj.get("courtName").toString());// 法院名
			}
			if (null != list.get(i).getCourtName() && !"".equals(list.get(i).getCourtName())) {
				archs.setCourtName(list.get(i).getCourtName());
			}
			///////////////////发布日期
			if (null != obj.get("publishDate") && !"".equals(obj.get("publishDate"))) {
				archs.setPublishDate(obj.get("publishDate").toString());// 发布日期
			}
			if (null != list.get(i).getPublishDate() && !"".equals(list.get(i).getPublishDate())) {
				archs.setPublishDate(list.get(i).getPublishDate());
			}
			///////////////////原告
			if (null != obj.get("plaintiff") && !"".equals(obj.get("plaintiff"))) {
				archs.setPlaintiff(obj.get("plaintiff").toString());// 原告
			}
			if (null != list.get(i).getPlaintiff() && !"".equals(list.get(i).getPlaintiff())) {
				archs.setPlaintiff(list.get(i).getPlaintiff());
			}
			///////////////////被告
			if (null != obj.get("defendant") && !"".equals(obj.get("defendant"))) {
				archs.setDefendant(obj.get("defendant").toString());// 被告
			}
			if (null != list.get(i).getDefendant() && !"".equals(list.get(i).getDefendant())) {
				archs.setDefendant(list.get(i).getDefendant());// 被告
			}
			///////////////////审批结果
			if (null != obj.get("approval") && !"".equals(obj.get("approval"))) {
				archs.setApproval(obj.get("approval").toString());// 审批结果
			}
			if (null != list.get(i).getApproval() && !"".equals(list.get(i).getApproval())) {
				archs.setApproval(list.get(i).getApproval());// 审批结果
			}
			///////////////////审结日期
			if (null != obj.get("approvalDate") && !"".equals(obj.get("approvalDate"))) {
				archs.setApprovalDate(obj.get("approvalDate").toString());// 审结日期
			}
			if (null != list.get(i).getApprovalDate() && !"".equals(list.get(i).getApprovalDate())) {
				archs.setApprovalDate(list.get(i).getApprovalDate());// 审结日期
			}
			///////////////////案由
			if (null != obj.get("caseCause") && !"".equals(obj.get("caseCause"))) {
				archs.setCaseCause(obj.get("caseCause").toString());// 案由
			}
			if (null != list.get(i).getCaseCause() && !"".equals(list.get(i).getCaseCause())) {
				archs.setCaseCause(list.get(i).getCaseCause());// 案由
			}
			/////////////////// 起诉日期
			if (null != obj.get("suitDate")&& !"".equals(obj.get("suitDate"))) {
				archs.setSuitDate(obj.get("suitDate").toString());// 起诉日期
			}
			if (null != list.get(i).getSuitDate() && !"".equals(list.get(i).getSuitDate())) {
				archs.setSuitDate(list.get(i).getSuitDate());// 起诉日期
			}
			///////////////////摘要
			if (null != obj.get("summary") && !"".equals(obj.get("summary"))) {
				archs.setSummary(obj.get("summary").toString());// 摘要
			}
			if (null != list.get(i).getSummary() && !"".equals(list.get(i).getSummary())) {
				archs.setSummary(list.get(i).getSummary());// 摘要
			}
			///////////////////省
			if (null != obj.get("province") && !"".equals(obj.get("province"))) {
				archs.setProvince(obj.get("province").toString());// 省
			}
			if (null != list.get(i).getProvince() && !"".equals(list.get(i).getProvince())) {
				archs.setProvince(list.get(i).getProvince());
			}
			///////////////////市
			if (null != obj.get("city") && !"".equals(obj.get("city"))) {
				archs.setCity(obj.get("city").toString());// 市
			}
			if (null != list.get(i).getCity() && !"".equals(list.get(i).getCity())) {
				archs.setCity(list.get(i).getCity());
			}
			///////////////////县
			if (null != obj.get("area") && !"".equals(obj.get("area"))) {
				archs.setArea(obj.get("area").toString());// 县
			}
			if (null != list.get(i).getArea() && !"".equals(list.get(i).getArea())) {
				archs.setArea(list.get(i).getArea());
			}
			///////////////////采集时间
			if (null != obj.get("collectDate") && !"".equals(obj.get("collectDate"))) {
				archs.setCollectDate(obj.get("collectDate").toString());// 采集时间
			}
			if (null != list.get(i).getCollectDate() && !"".equals(list.get(i).getCollectDate())) {
				archs.setCollectDate(list.get(i).getCollectDate());
			}
			String jsonss=gson.toJson(archs);
			doc = JsonDocument.create(urlId,JsonObject.fromJson(jsonss));
			bucket.upsert(doc);	
		}
        	return true;
	}
	
	/**
	 * 统一日期格式
	 * 
	 * @param value
	 * @return
	 */
	public static String getReplaceAllDate(String value) {
		StringBuffer sb = null;
		if (value != null && !"".equals(value)) {
			value = value.replaceAll("[（,）,﹝,﹞,〔,〕,(,),{,},<,>]", "");
			value = value.replaceAll("[-,-,/,\",年,月,日]", "-");
			value = value.replaceAll("[:,：]", ":");
			value = value.replaceAll("]", ")");
			value = value.replaceAll("[", "(");
			value = value.trim();
			sb = new StringBuffer();
			sb.append(value);
		}
		return sb == null ? null : sb.toString();
	}
	 
	 /**
		 * 案号清洗
		 * @param value
		 * @return
		 */
		public static String replaceAllCaseNum(String value){
			if(value == null && "".equals(value)){return null;}
				value=value.replaceAll("[(,（,〔,【]","("); 
				value=value.replaceAll("[),）,﹞,】]",")");
				value = value.replaceAll("]", ")");
				value = value.replaceAll("[", "(");
				value=value.trim();
				return value;
		}
	 /**
		 * 把字符串由全角转成半角
		 * @param doString 全角字符串
		 * @return 返回全角字符串对应的半角字符串
		 * @since 2015-8-10
		 */
		  public static String full2HalfChange(String doString)
		  {
				if(null == doString)
				{
					return null;
				}
				StringBuffer outStrBuf = new StringBuffer("");
				 String Tstr = "";
				 byte[] b = null;
				 for (int i = 0; i < doString.length(); i++)
				 {
					 Tstr = doString.substring(i, i + 1);
					 // 全角空格转换成半角空格
					 if (Tstr.equals("　")) 
					 {
						 outStrBuf.append(" ");
						 continue;
					 }
					 try 
					 {
						 b = Tstr.getBytes("unicode");
						 // 得到 unicode 字节数据
						 if (b[2] == -1) 
						 {
									 // 表示全角
							b[3] = (byte) (b[3] + 32);
							b[2] = 0;
							outStrBuf.append(new String(b, "unicode"));
						 } 
						 else 
						 {
							 outStrBuf.append(Tstr);
						 }
					 } 
					 catch(UnsupportedEncodingException e) 
					 {
						 e.printStackTrace();
					 }
			 	} 
			 	return outStrBuf.toString();
			}
	
	/**
	 * 统计导入的各地的记录条数
	 */
	public static void statisticalCount(File file,long count){		
		//取省名
		String provinceName=file.getParentFile().getParent();
		provinceName=provinceName.substring(provinceName.lastIndexOf("\\")+1,provinceName.length());
		//取市名
		String city =file.getParentFile().getPath();
		city=city.substring(city.lastIndexOf("\\")+1,city.length());
		List<RecordData> list=MAPS.get(provinceName);
		if(null==list||list.size()<=0){
			list=new ArrayList<RecordData>();
			list.add(new RecordData(provinceName,city,count));
			MAPS.put(provinceName,list);
		}
		else{
			boolean result=true;
			for(RecordData re:list){
				if(re.getCityName().equalsIgnoreCase(city)){
					re.setNumberData(re.getNumberData()+count);
					result=false;
					break;
				}
			}
			if(result){
				list.add(new RecordData(provinceName,city,count));
				MAPS.put(provinceName,list);
			}		
		}
	}
	/**
	 * 记录各地数据
	 */
	public static void record(){
		long sumCount=0;
		long sum=0;
		for(Map.Entry<String,List<RecordData>> map:MAPS.entrySet()){
			logger.info("###:"+map.getKey());
			List<RecordData> list=map.getValue();
			 sum=0;
			for(RecordData recordData:list){
				logger.info("###:"+recordData.getCityName()+"----记录条数:"+recordData.getNumberData());
				sum+=recordData.getNumberData();
			}
			sumCount+=sum;
			logger.info(map.getKey()+"省总数据条数据:"+sum);
			logger.info("------------------------------");
		}
		logger.info("总文件数据条数:"+sumCount);
		logger.info("去重后的数据条数据:"+REPEATSUM);
		logger.info("错误数据条数:"+ERRORSUM);
	}
	/**
	 * 根据UUID去重   已废弃
	 * @param li
	 * @return
	 */
	public static List<ArchivesVO> getNewList(List<ArchivesVO> li){
		List<ArchivesVO> list = new ArrayList<ArchivesVO>();
		for(int i=0; i<li.size(); i++){
		ArchivesVO str = li.get(i); //获取传入集合对象的每一个元素
		if(!list.contains(str)){ //查看新集合中是否有指定的元素，如果没有则加入
		list.add(str);
			}
		}
		return list; //返回集合
		}
	
	/**
	 * 根据UUID去重
	 * @param list
	 * @return
	 */
	public static List<ArchivesVO> removeDuplicate(List<ArchivesVO> list) { 
		for ( int i = 0 ; i < list.size() - 1 ; i ++ ) { 
		for ( int j = list.size() - 1 ; j > i; j -- ) { 
		if (list.get(j).getUuid().equals(list.get(i).getUuid())) { 
		list.remove(j); 
				} 
			} 
		} 
		return list;
		} 
}
