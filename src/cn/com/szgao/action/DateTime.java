package cn.com.szgao.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.util.CommonConstant;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;

public class DateTime {
	
	private static Logger logger = LogManager.getLogger(DateTime.class.getName());
	static ApplicationContext application = new ClassPathXmlApplicationContext("classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory = (SessionFactory) application.getBean("sessionFactory");
	public static String  CASENUM[] = {"(20","（20","〔20","[20","【20","(19","（19","〔19","[19","【19"};
	static long count = 0;
	/**
	 * 数据清洗
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File filepor=new File("E:\\Company_File\\log4j-0902\\Java1\\batchImport.log");   
		if(filepor.exists()){
			filepor.delete();		//删除日志文件	
		}
		filepor=null;
		
		//log配置路径
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		
		//时间
		long da = System.currentTimeMillis();
		
		//连接couchbase
		Bucket bucket = CommonConstant.connectionCouchBaseNotice();
		Connection conn = null;
		
		//连接PG
		conn=DriverManager.getConnection("jdbc:postgresql://192.168.251:5432/duplicatedb?useServerPrepStmts=false&rewriteBatchedStatements=true", "postgres", "615601.xcy*");
		
		conn.setAutoCommit(false); //当值为false时，sql命令的提交由应用程序负责，程序必须调用commit或者rollback方法；值为true时，sql命令的提交（commit）由驱动程序负责    
		 Statement st = conn.createStatement();
		 String sql = "SELECT URL_ID FROM extract_url_t";//查询SQL
		 ResultSet rs = st.executeQuery(sql);
		 
		 List<ArchivesVO> archlist = new ArrayList<ArchivesVO>();
		 ArchivesVO arch = null;
		 DateTime datetime= new DateTime();
		 int i = 0;//批次
		 //while循环
		 while (rs.next()){
			 arch = new ArchivesVO();
			 	count ++;
				arch.setUuid(rs.getString(1));//获取ID
				archlist.add(arch);
				if(archlist.size()>=1000){
					i++;//每一千条，批次加1
					boolean result = datetime.update(archlist,bucket);//操作成功返回true，否则返回false
					logger.info("第"+i+"批数据："+archlist.size());
					if (!result) {
						logger.info("更新失败1");
					}
					archlist = null;//赋空值，释放资源
					archlist = new ArrayList<ArchivesVO>();
				}
			   }
		 if (null != archlist && archlist.size() > 0) {
			 i++;//不满一千条数据作为一个批次
				boolean result = datetime.update(archlist,bucket);//操作成功返回true，否则返回false
				logger.info("第"+i+"批数据："+archlist.size());
					if (!result) {
						logger.info("更新失败2");
					}
					archlist = null;//赋空值，释放资源
					archlist = new ArrayList<ArchivesVO>();
				}
		 logger.info("总数为："+count);
			logger.info("所有文件总耗时" + (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}
	
	/**
	 * 对couchbase进行操作
	 * @param list 对象集合
	 * @param bucket
	 * @return
	 * @throws Exception
	 */
	public boolean update(List<ArchivesVO> list,Bucket bucket) throws Exception {
		if(null==list||list.size()<=0){
			return false;
		}
		JsonDocument doc=null;
		JsonObject obj=null;	
		com.google.gson.JsonObject json=null;
		Gson gson=new Gson();
		ArchivesVO archs = null;
		try{
		for(ArchivesVO arch : list){
	 	doc = JsonDocument.create(arch.getUuid());
		obj =bucket.get(doc)==null?null:bucket.get(doc).content();
		
		if(obj == null){
			logger.info("匹配不到UUID:"+arch.getUuid());
				continue;	
			}
		archs = new ArchivesVO();
		json=gson.fromJson(obj.toString(), com.google.gson.JsonObject.class);
		archs = gson.fromJson(json, ArchivesVO.class);
		if(null != obj.get("title") && !"".equals(obj.get("title"))){
			archs.setTitle(obj.get("title").toString());//获取CB中该条数据的标题
		}
		if(null != obj.get("detailLink")&& !"".equals(obj.get("detailLink"))){
			archs.setDetailLink(obj.get("detailLink").toString());//获取CB中该条数据的URL
		}
		if(null != obj.get("catalog") && !"".equals(obj.get("catalog"))){
			archs.setCatalog(obj.get("catalog").toString());//
		}
		if(null != obj.get("caseNum") && !"".equals(obj.get("caseNum"))){
//			archs.setCaseNum(full2HalfChange(obj.get("caseNum").toString()));//获取CB中该条数据的案号
			archs.setCaseNum(replaceAllCaseNum(obj.get("caseNum").toString()));//获取CB中该条数据的案号
		}
		if(null != obj.get("courtName") && !"".equals(obj.get("courtName"))){
			archs.setCourtName(obj.get("courtName").toString());//获取CB中该条数据的法院名
		}
		if(null != obj.get("publishDate") && !"".equals(obj.get("publishDate"))){
			archs.setPublishDate(getReplaceAllDate(obj.get("publishDate").toString().trim()));//获取CB中该条数据的审结日期
		}
		if(null != obj.get("province") && !"".equals(obj.get("province"))){
			archs.setProvince(obj.get("province").toString());//获取CB中该条数据的省份名
		}
		if(null != obj.get("city") && !"".equals(obj.get("city"))){
			archs.setCity(obj.get("city").toString());//获取CB中该条数据的
		}
		if(null != obj.get("area") && !"".equals(obj.get("area"))){
			archs.setArea(obj.get("area").toString());//获取CB中该条数据的县/区
		}
		if(null != obj.get("collectDate") && !"".equals(obj.get("collectDate"))){
			archs.setCollectDate(getReplaceAllDate(obj.get("collectDate").toString().trim()));//获取CB中该条数据的采集日期
		}
		if(null != obj.get("plaintiff") && !"".equals(obj.get("plaintiff"))){
			archs.setPlaintiff(obj.get("plaintiff").toString());//获取CB中该条数据的PDF
		}
		if(null != obj.get("defendant") && !"".equals(obj.get("defendant"))){
			archs.setDefendant(obj.get("defendant").toString());//获取CB中该条数据的被告
		}
		if(null != obj.get("approval") && !"".equals(obj.get("approval"))){
			archs.setApproval(obj.get("approval").toString());//获取CB中该条数据的审批结果
		}
		if(null != obj.get("suitType") && !"".equals(obj.get("suitType"))){
			archs.setSuitType(obj.get("suitType").toString());//获取CB中该条数据的诉讼类型
		}
		if(null != obj.get("suitDate") && !"".equals(obj.get("suitDate"))){
			archs.setSuitDate(obj.get("suitDate").toString());//获取CB中该条数据的起诉日期
		}
		if(null != obj.get("approvalDate") && !"".equals(obj.get("approvalDate"))){
			archs.setApprovalDate(obj.get("approvalDate").toString());//获取CB中该条数据的审结日期
		}
		if(null != obj.get("caseCause") && !"".equals(obj.get("caseCause"))){
			archs.setCaseCause(obj.get("caseCause").toString());//获取CB中该条数据的案由
		}
		if(null != obj.get("summary") && !"".equals(obj.get("summary"))){
			archs.setSummary(obj.get("summary").toString());//获取CB中该条数据的摘要
		}
			String jsonss=gson.toJson(archs);
			doc = JsonDocument.create(arch.getUuid(),JsonObject.fromJson(jsonss));
			bucket.upsert(doc);	
		}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		finally{
			archs = null;
			gson = null;
			json = null;
			obj=null;
			doc=null;
			
		}
		return true;
	}
	
	/**
	 * 案号清洗
	 * @param value
	 * @return
	 */
	public static String replaceAllCaseNum(String value){
		if(value == null && "".equals(value)){return null;}
		for(String val:CASENUM){
			int firstIndex = value.lastIndexOf(val);
			if(firstIndex == -1){continue;}
			value = value.substring(firstIndex);
			int secondIndex = value.indexOf("号");
			value = value.substring(0, secondIndex+1);
			value=value.replaceAll("[(,（,〔,【]","("); 
			value=value.replaceAll("[),）,﹞,】]",")");
			value = value.replaceAll("]", ")");
			value = value.replaceAll("[", "(");
			value=value.trim();
			return value;
		}
		return null;
	}
	/**
	 * 统一日期格式
	 * @param value
	 * @return
	 */
	 public static String getReplaceAllDate(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			   //去掉特殊字符
			    value=value.replaceAll("[日,（,）,(,),【,】,{,},<,>]","");
			    value=value.replaceAll("[-,-,/,\",年,月]","-");
			    value=value.replaceAll("[:,：]", ":");
			    value = value.replaceAll("]", "");
				value = value.replaceAll("[", "");
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
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

}
