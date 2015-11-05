package cn.com.szgao.action;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.RecordData;
import cn.com.szgao.util.CommonConstant;

public class TestToDetailLink {
	/**
	 * 写日志
	 */
	private static Logger logger = LogManager.getLogger(Test.class.getName());
	static ApplicationContext application=new ClassPathXmlApplicationContext("classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory=(SessionFactory)application.getBean("sessionFactory");	
	static Map<String,List<RecordData>> MAPS=new HashMap<String,List<RecordData>>();
	static long ERRORSUM=0;
	static long INPUTSUM=0;
	static long REPEATSUM=0;
	/**
	 * 裁判文书
	 * 数据写库PostgreSql和couchbase
	 * JSON导入extracl_url_t表和court桶
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {	
		//清空不用的文件或日志
//		File filepor=new File("E:\\Company_File\\log4j-0925\\Java1\\batchImport.log");   
//		 if(filepor.exists()){
//		 filepor.delete();
//		 }
//			filepor=null;
		 PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties"); 
		 long da = System.currentTimeMillis();
		File file=new File("F:\\DataSheet\\excel");
		Bucket bucket = CommonConstant.connectionCouchBase();
		try {
			show(file,bucket);	
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		finally{
			sessionFactory.close();
		}
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
	}
	/**
	 * 写数据   
	 * @param file
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	private static <ObjectDataVO> void create(File file,Bucket bucket) throws Exception, UnsupportedEncodingException{
		InputStream input=null;
		Workbook wokr= null;
		String name=file.getParentFile().getPath();
		name=name.substring(name.lastIndexOf("\\")+1,name.length());
		try {
			input=CommonConstant.getInputStram(file);//获取文件输入流       
			wokr =CommonConstant.getWorkbook(input);//获取对象数据
			long count = wokr.getSheetAt(0).getLastRowNum();	//统计条数
			List<String[]>  list=CommonConstant.listObject(wokr);//将数据放入到集合中
		   boolean result=TestToDetailLink.createJsonData(list,bucket);
		    REPEATSUM+=list.size();
		    //
			if(!result){			
				logger.error("读取"+name+"<<"+file.getName()+">>文件时发生JSON异常!");
				ERRORSUM+=count;
			}
			//统计数据
			statisticalCount(file,count);
		} catch (Exception e) {
			logger.error("读取"+name+"<<"+file.getName()+">>文件时发生IO异常:"+e.getMessage());			
		}
		finally{
			input.close();
			wokr.close();
			file=null;
		}	
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
	 * 保存数据到JSON文档库 
	 */
	public static boolean createJsonData(List<String[]> list,Bucket bucket) throws Exception {
		JsonDocument doc = null;
		JsonObject obj2 = null;
		com.google.gson.JsonObject json = null;
		Gson gson = new Gson();
		ArchivesVO archs = null;
		if(null==list||list.size()<=0){
			return false;
		}
		//连接库
		int i=0;
		int sum=list.size();
		String urlId=null;
		try {
		for(i= 0;i<sum;i++)
		{
			urlId = CommonConstant.getUUID(list.get(i)[1]).toString();//需要指定URL生成UUID的字符串作为参数	
			doc = JsonDocument.create(urlId);
			obj2 = bucket.get(doc) == null ? null : bucket.get(doc).content();
			if (obj2 == null) {
				logger.info("匹配不到UUID:" + urlId);
				continue;
			}
//			logger.info(urlId);
			archs = new ArchivesVO();
			json = gson.fromJson(obj2.toString(),
					com.google.gson.JsonObject.class);
			archs = gson.fromJson(json, ArchivesVO.class);
			if (null != obj2.get("title") && !"".equals(obj2.get("title"))) {
				archs.setTitle(obj2.get("title").toString());// 标题
			}
			if (null != obj2.get("suitType") && !"".equals(obj2.get("suitType"))) {
				archs.setSuitType(obj2.get("suitType").toString());// 诉讼类型
			}
			if (null != obj2.get("caseNum") && !"".equals(obj2.get("caseNum"))) {
				archs.setCaseNum(obj2.get("caseNum").toString());// 案号
			}
			if (null != obj2.get("courtName") && !"".equals(obj2.get("courtName"))) {
				archs.setCourtName(obj2.get("courtName").toString());// 法院名
			}
			if (null != obj2.get("plaintiff") && !"".equals(obj2.get("plaintiff"))) {
				archs.setPlaintiff(obj2.get("plaintiff").toString());// 原告
			}
			if (null != obj2.get("defendant") && !"".equals(obj2.get("defendant"))) {
				archs.setDefendant(obj2.get("defendant").toString());// 被告
			}
			if (null != obj2.get("approval") && !"".equals(obj2.get("approval"))) {
				archs.setApproval(obj2.get("approval").toString());// 审批结果
			}
			if (null != obj2.get("approvalDate") && !"".equals(obj2.get("approvalDate"))) {
				archs.setApprovalDate(obj2.get("approvalDate").toString());// 审结日期
			}
			if (null != obj2.get("caseCause") && !"".equals(obj2.get("caseCause"))) {
				archs.setCaseCause(obj2.get("caseCause").toString());// 案由
			}
			if (null != obj2.get("summary") && !"".equals(obj2.get("summary"))) {
				archs.setSummary(obj2.get("summary").toString());// 摘要
			}
			if (null != list.get(i)[1] && !"".equals(list.get(i)[1])) {
				archs.setDetailLink(list.get(i)[1]);// url
			}
			if (null != obj2.get("catalog") && !"".equals(obj2.get("catalog"))) {
				archs.setCatalog(obj2.get("catalog").toString());// 分类
			}
			if (null != obj2.get("publishDate") && !"".equals(obj2.get("publishDate"))) {
				archs.setPublishDate(obj2.get("publishDate").toString());// 发布日期
			}
			if (null != obj2.get("province") && !"".equals(obj2.get("province"))) {
				archs.setProvince(obj2.get("province").toString());// 省
			}
			if (null != obj2.get("city") && !"".equals(obj2.get("city"))) {
				archs.setCity(obj2.get("city").toString());// 市
			}
			if (null != obj2.get("area") && !"".equals(obj2.get("area"))) {
				archs.setArea(obj2.get("area").toString());// 县
			}
			if (null != obj2.get("collectDate") && !"".equals(obj2.get("collectDate"))) {
				archs.setCollectDate(obj2.get("collectDate").toString());// 采集时间
			}
			if (null != obj2.get("suitDate")&& !"".equals(obj2.get("suitDate"))) {
				archs.setSuitDate(obj2.get("suitDate").toString());// 起诉日期
			}
			String jsonss = gson.toJson(archs);
			doc = JsonDocument.create(urlId,
					JsonObject.fromJson(jsonss));
//			logger.info(doc);
			bucket.upsert(doc);
/*//				//标题，URL+，分类，字号，法院名+，发布日期+，省，市，区，采集时间			
				obj = JsonObject.empty().put("title",list.get(i)[0]).put("detailLink",list.get(i)[1])
						.put("catalog",list.get(i)[2]).put("caseNum",list.get(i)[3]).put("courtName", list.get(i)[4])
						.put("publishDate", list.get(i)[5]).put("province", list.get(i)[6]).put("city",list.get(i)[7])
						.put("area",list.get(i)[8]).put("collectDate",list.get(i)[9]);
				
				doc = JsonDocument.create(urlId, obj); //创建JSON文档	
				
				bucket.upsert(doc);	//插入JSON文档到库 
*/				
		}
		} catch (Exception e) {
			return false;
		}
		finally{
			doc = null;
			obj2 = null;
			archs = null;
			json = null;
			gson = null;
//			bucket.close();
		}
		return true;
	}
}
