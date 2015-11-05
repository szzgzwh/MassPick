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
import cn.com.szgao.dto.NoticeVO;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;


public class ExtractNotice {
	private static Logger logger = LogManager.getLogger(ExtractNotice.class.getName());
	static ApplicationContext application = new ClassPathXmlApplicationContext(
			"classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory = (SessionFactory) application.getBean("sessionFactory");
	static long count = 0;
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
	}
	static long ERRORSUM=0;
	static long INPUTSUM=0;
	static long REPEATSUM=0;
	//所有文书类型
	private static String[] suitype= {"起诉状副本及开庭传票","裁判文书","公示催告","开庭传票","破产文书","起诉状、上诉状副本","宣告失踪、死亡","执行文书","其他","其它"};
	//抽取公告内容关键字
	private static String[] keywordke1= {"：原告","希望","票号","：我院受理",":我院受理","我院受理","：本院受理的",":本院受理的",":本院受理","：本院受理","本院受理","：本院","： 本院","本院于","关于申请人","申请人","上诉人","：关于申请执行人","本院受理",":","："};
	private static String[] keywordke2= {"现通知你们自公告之日起","现通知你自公告之日起","你司自公","现本院依法","限你们自公告之日起","限你自公告之日起","限你们自本公告之日起","限你于公告之日",
		"限你自本公告之日起","限你自本公告","本判决自公告之日起","自发出公告之日起","自发出本公告之日起","自本公告发出之日起","本公告自发出之日起","自本公告见报之日起","你们自公告之日起","你自公告之日起","现自公告之日起",
		"自公告之日","自本公告之日","自公告发出之日起","自判决公告之日起","。。[","出票人","。"};
	//抽取公告主体键字
	private static String[] keywordke4= {":原告","：原告",":我院受理","：我院受理",":本院受理","：本院受理","：本院","： 本院","因银行","因其持有","因遗失","破产清算一案","申请破产","：上诉人","：关于申请执行人","：关于申请人",":","：","公司"};
	/**
	 * 公告抽取
	 * 更新couchbase
	 * 抓取HTML修改courtPub
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		//清空不用的文件或日志
		File filepor=new File("E:\\Company_File\\log4j-0811\\Java1\\batchImport.log");   
		if(filepor.exists()){
			filepor.delete();			
		}
		filepor=null;
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
//		File file = new File("F:\\DataSheet\\DetailedPage\\NoticeHtm\\法院公告\\法院公告HTML\\4月28号之前\\法院公告1（html）\\downloadreprocess\\0000aa64-b0d1-558d-b82a-20d18552c0ea.html");
		File file = new File("F:\\DataSheet\\DetailedPage\\NoticeHtm\\法院公告\\法院公告HTML");
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
		CollectionDataDao dataDao = new CollectionDataDao();
		dataDao.setSf(sessionFactory);
		CollectDataService service = new CollectDataService();
		service.setiCollectionDataDao(dataDao);
		ICollectDataService services = service;
		String html = null;
		NoticeVO notice = null;
		List<NoticeVO> notices = null;
		notices = new ArrayList<NoticeVO>();
			try {
				if (file.isFile()) {
					notice = new NoticeVO();
//				count++;
					logger.info(file.getPath());
					String suffix = file.getName();
					suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
					suffix = MAPS.get(suffix);
					if (null == suffix) {
						return;
					}
					logger.info("网址:" + file.getPath());
					html = URLText.getText("file:///", file.getPath());
					if (html == null || "".equals(html)) {
						logger.info(file.getPath() + ":网页地址访问失败!");
						return;
					}
					html = ExtractText(html);
//					notice.setUuid(file.getName().substring(0,file.getName().lastIndexOf(".")));//生成UuId
					notice.setPubPerson(getPubPerson(html));//法院名
					notice.setClient(getClient(html));//公告主体
					notice.setPubDate(getPubDate(html));//公告日期
					notice.setPubContent(getPubContent(html));//公告内容
					notice.setPubType(getPubType(html));//公告类型
					notice.setProvince(getProvince(html));//省
					notice.setUuid(file.getName().substring(0,file.getName().lastIndexOf(".")));//修改时需要，新增必须注释本行
					showLog(notice);
					notices.add(notice);
					logger.info("<<--------------------------end---------------------------->>");
//				boolean result = services.createJsonDataNotice(notices);//进入新增
					boolean result = services.updateJsonDataNotice(notices);//进入修改
					logger.info("<<-------------------------result----------------------------->>"+result);
					if (!result) {
						logger.info(file.getPath() + ":更新失败");
					}
					count += notices.size();
					notices = null;
					return;
				}
				logger.info("<<----------------------------end-------------------------->>");
				File[] files = file.listFiles();
				for (File fi : files) {
					if (fi.isFile()) {
						notice = new NoticeVO();
						String suffix = fi.getName();
						suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
						suffix = MAPS.get(suffix);
						if (null == suffix) {
							continue;
						}
						html = URLText.getText("file:///", fi.getPath());
						logger.info("网址:" + fi.getPath());
						if (html == null || "".equals(html)) {
							logger.error(fi.getPath() + ":网页地址访问失败!");
							continue;
						}
						html = ExtractText(html);  //  截取公告
//						html = getReplaceAll(html);		//去特殊字符
						notice.setPubPerson(getPubPerson(html));//法院名
						notice.setClient(getClient(html));//公告主体
						notice.setPubDate(getPubDate(html));//公告日期
						notice.setPubContent(getPubContent(html));//公告内容
						notice.setPubType(getPubType(html));//公告类型
						notice.setProvince(getProvince(html));//省
						notice.setUuid(fi.getName().substring(0,fi.getName().lastIndexOf(".")));//修改时需要，新增必须注释本行
//						showLog(notice);
						notices.add(notice);
						if (notices.size() >= 1000) {
//						boolean result = services.createJsonDataNotice(notices);//进入新增
						boolean result = services.updateJsonDataNotice(notices);//进入修改
						if (!result) {
							logger.info(fi.getPath() + ":更新失败1");
						}
							count += notices.size();
							notices = null;
							notices = new ArrayList<NoticeVO>();
						}
					} 
					else if (fi.isDirectory()) {
						logger.info(fi.getName());
						show(fi);
					}
					else {
						continue;
					}
				}
				if (null != notices && notices.size() > 0) {
//				boolean result = services.createJsonDataNotice(notices);//进入新增
				boolean result = services.updateJsonDataNotice(notices);//进入修改
					if (!result) {
						logger.info("更新失败2");
					}
					count += notices.size();
					notices = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	 public static String getReplaceAll(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			    value=value.replaceAll(",","，");
			    value=value.replaceAll("�","O");
			    value=value.replaceAll("[×,X,Ｘ,x,╳,＊,\\*]","某");
			    value=value.replaceAll("[\n,\t,\r,\\s,&nbsp; ,：,“,”,・ ,:,<,/>,</,>,a-z,A-Z,-,+,=,},{,.,#,\",',-,%,^,*]","");
				value=getSpecialStringALL(value);
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
	 }
   /**
     * 去掉特殊字符
     * @param value
     * @return
     */
    public static String getSpecialStringALL(String value){
    	if(null==value||"".equals(value)){return null;} 
    	char[] chs=value.toCharArray();
    	 StringBuffer sb=new StringBuffer();
    	 for(char c:chs){
    		 if(((int)c)!=12288&&((int)c)!=160){
    			 sb.append(String.valueOf(c));
    		 }
    	 }
    	 return sb.toString();
    } 
    /**
	 * 打印信息
	 * @param notice
	 */
    public static void showLog(NoticeVO notice){
    	if(null==notice){return;}
    	logger.info("<<------------------------------------------------------>>");
		logger.info("机关:"+notice.getClient()+"相关的"+notice.getPubType()+"公告");
		logger.info("发文机构:"+notice.getPubPerson());
		logger.info("公告时间："+notice.getPubDate());
		logger.info("公告内容："+notice.getPubContent());
		logger.info("UUID:"+notice.getUuid());
		logger.info("<<------------------------------------------------------>>");
    }
   
    
    public static String ExtractText(String value){
		try{
			int index = value.lastIndexOf("公告内容");
		    if(index<= 0){return null;}
		    int index1=value.indexOf("下载打印本公告");
		    if(index1 <= 0){return null;}
			return value.substring(index+4,index1);
		}
		catch(Exception e){
			logger.error("提取公告正文出错:"+e.getMessage());
		}			
		return null;
	}
    
    /**
     * 提取法院名称
     * @param value
     * @return
     */
    public static String getPubPerson(String value){

		try{
			int index1 = value.lastIndexOf("]");
		    int idnex=value.lastIndexOf("院");
		    if(idnex==-1){return null;}
			return value.substring(index1+1,idnex+1);
		}
		catch(Exception e){
			logger.error("提取法院名称出错:"+e.getMessage());
		}			
		return null;
	} 
    
    public static String getProvince(String value){
    	try {
			int index =  value.lastIndexOf("[");
			if(index == -1){return null;}
			int index1 =  value.lastIndexOf("]");
			if(index1 == -1){return null;}
			return value.substring(index+1, index1);
		} catch (Exception e) {
			logger.error("提取法院名称出错:"+e.getMessage());
		}
    	return null;
    }
    /**
     * 提取公告主体
     * @param value
     * @return
     */
    public static String getClient(String value){
  		try{
  			for(String val:suitype){
  		    int index=value.indexOf(val);
  		    if(index >= 0){
  		    	index = index +val.length();
  		    	value = value.substring(index, value.length());
  		    	for(String val1:suitype){
  		  		    int index1=value.indexOf(val1);
  		  		    if(index1 >= 0){
  		  		    index1 = index1 +val.length();
		  		  for(String val2:keywordke4){
		  			  int index2 = value.indexOf(val2);
		  			  if(index2>=0){
		  				  value = value.substring(index,index2);
		  				int index3 = value.length()/2;
		  				if(value.substring(0,index3).equals(value.substring(index3, value.length()))){
		  					value = value.substring(0,index3);
		  				}
		  				value = getReplaceAll(value);
		  			return value;
		  		  }
		  			}
  		  		    }
  		    	}
  			}
  		}
  		}
  		catch(Exception e){
  			logger.error("提取被告名称出错:"+e.getMessage());
  		}			
  		return null;
  	}
   
    
    /**
     * 提取日期
     * @param value
     * @return
     */
    public static String getPubDate(String value){

  		try{
  		    int idnex=value.lastIndexOf("上传日期");
  		    if(idnex==-1){return null;}
  			return value.substring(idnex+5,idnex+15);
  		}
  		catch(Exception e){
  			logger.error("提取日期出错:"+e.getMessage());
  		}			
  		return null;
  	}
    
    /**
     * 提取类型
     * @param value
     * @return
     */
    public static String getPubType(String value){

  		try{
  			for(String val:suitype){
  				int index = value.indexOf(val);
  				if(index >= 0){
  					return val;
  				}
  				if(val == null || "".equals(val)){
  					return "其它";
  				}
  			}
  		}
  		catch(Exception e){
  			logger.error("提取文书类型出错:"+e.getMessage());
  		}			
  		return null;
  	}
    
    //提取公告内容
    public static String getPubContent(String value){
    	String values;
    	try{
  		  for(String val:keywordke1){
  			  int index=value.indexOf(val);
    		    if(index >=0){
    		    	index = index+val.length();
    		  for(String val2:keywordke2){
    			  int index2 = value.lastIndexOf(val2);
    			  if(index2>=0){
    			  values = value.substring(index,index2);
  			return values;
    		  }
  		  }
    		    }
  		  }
  		}
  		catch(Exception e){
  			logger.error("提取内容出错:"+e.getMessage());
  		}
  		return null;
    }
}
