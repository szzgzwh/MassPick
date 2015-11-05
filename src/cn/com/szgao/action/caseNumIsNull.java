package cn.com.szgao.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class caseNumIsNull {
	private static Logger logger = LogManager.getLogger(caseNumIsNull.class
			.getName());
	static ApplicationContext application = new ClassPathXmlApplicationContext(
			"classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory = (SessionFactory) application
			.getBean("sessionFactory");
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
	}
	static long count = 0;
	static long num = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File filepor=new File("E:\\Company_File\\log4j-1008\\Java1\\batchImport.log");   
		 if(filepor.exists()){
		 filepor.delete();
		 }
			filepor=null;
		 PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		File file = new File("G:\\Data\\八月裁判文书\\通过法院名查询的裁判文书");
		Bucket bucket = CommonConstant.connectionCouchBase();
		try {
			selectAll(file,bucket);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			file = null;
		}
		logger.info("总数为："+count);
		logger.info("案号为空的有："+num);
		logger.info("所有文件总耗时" + (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}
	/**
	 * 遍历查询所有
	 * @param file
	 * @param bucket
	 */
	public static void selectAll(File file,Bucket bucket){
		List<ArchivesVO> listarchs = new ArrayList<ArchivesVO>();
		ArchivesVO arch = null;
		if(file.isFile()){
			arch = new ArchivesVO();
			String suffix = file.getName();
			suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
			suffix = MAPS.get(suffix);
			if (null == suffix) {
				return;
			}
			arch.setDetailLink(file.getPath());
			arch.setUuid(file.getName().substring(0,file.getName().lastIndexOf(".")));
			listarchs.add(arch);
				 caseNumIsNull.selectHtmlPath(listarchs, bucket);
//				count += listarchs.size();
				listarchs = null;
				listarchs = new ArrayList<ArchivesVO>();
			}
		File[] files = file.listFiles();
		for (File fi : files) {
			if (fi.isFile()) {
				arch = new ArchivesVO();
				String suffix = fi.getName();
				suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
				suffix = MAPS.get(suffix);
				if (null == suffix) {
					return;
				}
				arch.setDetailLink(fi.getPath());
				arch.setUuid(fi.getName().substring(0,fi.getName().lastIndexOf(".")));
				listarchs.add(arch);
//				if(listarchs.size() >= 2000){
//				caseNumIsNull.selectHtmlPath(listarchs, bucket);
//					listarchs = null;
//					listarchs = new ArrayList<ArchivesVO>();
//				}
			}
			 else if (fi.isDirectory()) {
					logger.info(fi.getName());
					selectAll(fi,bucket);//多个文件夹时遍历所有文件夹
				} else {
					continue;
				}
			if(listarchs != null && listarchs.size()>0){
				count += listarchs.size();
				caseNumIsNull.selectHtmlPath(listarchs, bucket);
				listarchs = null;
				listarchs = new ArrayList<ArchivesVO>();
			}
		}
	}
	/**
	 * 匹配CB数据的UUID 判断案号是否为空
	 * @param list
	 * @param bucket
	 * @return
	 */
	public static String selectHtmlPath(List<ArchivesVO> list,Bucket bucket){
		if(null==list||list.size()<=0){
			return null;
		}
		JsonDocument doc=null;
		JsonObject obj=null;	
		try{
			for(ArchivesVO arch : list){
		 	doc = JsonDocument.create(arch.getUuid());
			obj =bucket.get(doc)==null?null:bucket.get(doc).content();
			if(obj == null){
//				logger.info("匹配不到UUID:"+arch.getUuid());
				//
					continue;	
				}
			if(obj.get("detailLink")==null || "".equals(obj.get("detailLink"))){
				num ++;
//				logger.info("ID为空："+arch.getUuid());
				logger.info("URL路径：（"+arch.getDetailLink()+"）。");
				}
			}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			finally{
				obj=null;
				doc=null;
			}
		return null;
	}
}
