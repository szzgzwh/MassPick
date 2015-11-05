package cn.com.szgao.action;

import java.io.File;
import java.io.IOException;
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

import cn.com.szgao.dao.impl.CollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;

public class ExtractCourt {
	private static Logger logger = LogManager.getLogger(ExtractCourt.class
			.getName());
	static ApplicationContext application = new ClassPathXmlApplicationContext(
			"classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory = (SessionFactory) application
			.getBean("sessionFactory");
	static long count = 0;
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
	}


	/**
	 * 裁判文书
	 * 更新couchbase
	 * 抓取HTML修改court
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		File filepor=new File("E:\\Company_File\\log4j-0806\\Java1\\batchImport.log");
		 if(filepor.exists()){
		 filepor.delete();
		 }
		 filepor=null;
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		File file=new File("F:\\DataSheet\\DetailedPage\\00f81cd0-a3f3-51a0-a06a-8eec35dfc75e.html");
		try {
			show(file);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			file = null;
		}
		logger.info(count + ":数量");
		logger.info("所有文件总耗时" + (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}
	/**
	 * 递归遍历html文件
	 * @param file
	 * @throws
	 * @throws Exception
	 */
	private static void show(File file) throws Exception {
		String html = null;
		if (file.isFile()) {
			logger.info(file.getPath());
			String suffix = file.getName();
			suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
			suffix = MAPS.get(suffix);
			if (null == suffix) {
				return;
			}
			html = URLText.getText("file:///", file.getPath());
			html = new String(html.getBytes("UTF-8"));
			logger.info("-----------HTML："+html);
			if (html == null || "".equals(html)) {
				logger.info(file.getPath() + ":网页地址访问失败!");
				return;
			}
			html = ExtractText(html);
		}
	}
	//
	 public static String ExtractText(String value){
			try{
				int index = value.lastIndexOf("【关闭窗口】");
			    if(index<= 0){return null;}
			    int index1=value.indexOf("上一篇：");
			    if(index1 <= 0){return null;}
				return value.substring(index+4,index1);
			}
			catch(Exception e){
				logger.error("提取裁判文书正文出错:"+e.getMessage());
			}			
			return null;
		}
}
