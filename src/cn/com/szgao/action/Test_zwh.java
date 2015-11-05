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
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.szgao.dao.impl.CollectionDataDao;
import cn.com.szgao.dto.RecordData;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;
import cn.com.szgao.util.CommonConstant;

public class Test_zwh {

	/**
	 * @param args
	 */
	private static Logger logger = LogManager.getLogger(Test.class.getName());
	static ApplicationContext application=new ClassPathXmlApplicationContext("classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory=(SessionFactory)application.getBean("sessionFactory");	
	static Map<String,List<RecordData>> MAPS=new HashMap<String,List<RecordData>>();
	static long ERRORSUM=0;
	static long INPUTSUM=0;
	static long REPEATSUM=0;
	public static void main(String[] args) throws Throwable, Exception {
//		String input  = "经审理查明：原告邵某某与被告关某某未办理结婚登记于1997年11月同居生活，2004年8月28日生男孩邵某某，在荣河润华小学读书，现随被告生活。对于以上事实原告提供了万荣县民政局出具的无婚姻登记记录证明，被告提供了万荣县某某镇某某村委会出具的孩子邵某某从出生到现在一直由关某某抚养证明、万荣县某某镇某某小学出具的孩子邵某某学费由关某某一直交付的证明、孩子邵某某出具的他愿意跟随母亲关某某生活的书面意见。";
//		getSentences2(input);
		
			/*PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");   
			long da=System.currentTimeMillis();
			//导入文件地址
			File file=new File("F:\\山西省");
			try {
				System.out.println("----main----"+file);
				show(file);	
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				e.getLocalizedMessage();
			}
			finally{
				sessionFactory.close();
			}
			logger.info("所有文件总耗时"+(((System.currentTimeMillis()-da)/1000)/60)+"分钟");
			record();
			*/
		String htmlcode="<HTML><HEAD><TITLE>AAA</TITLE></HEAD><BODY></BODY></HTML>";
		Parser parser=Parser.createParser(htmlcode,"GBK");
		HtmlPage page=new HtmlPage(parser);
		try
		{parser.visitAllNodesWith(page);}
		catch(ParserException e)
		{e=null;}
		NodeList nodelist=page.getBody();
		NodeFilter filter=new TagNameFilter("F:/DataSheet/testData/00007759-206f-5028-871c-4e6bf306a742.html");
		nodelist=nodelist.extractAllNodesThatMatch(filter,true);
		for(int i=0;i<nodelist.size();i++)
		{
		LinkTag link=(LinkTag)nodelist.elementAt(i);
		System.out.println(link.getAttribute("href")+"\n");
		}
	}
	
	 public static String[] getSentences2(String input) {
		 System.out.println(input);
	        if (input == null) {
	            return null;
	        } else {	        	
	        	String[] inputs=input.split("[。]");
	        	String[] inputs2=new String[inputs.length];
	        	int index=0;
	        	for(String val:inputs){
	        		if(null!=val&&!"".equals(val)){
//	        			inputs[index++]=val;
	        			val = inputs[index++];
		        		System.out.println("--NO.3--"+val);	
	        		}
	        	}
	        	inputs=null;
	            return inputs2;
	        }
	 
	 }
	 
	 /**
		 * 递归遍历文件
		 * @param file
		 * @throws  
		 * @throws Exception 
		 */
		private static void show(File file) throws Exception{
			System.out.println("---show----"+file);
			if(file.isFile()){
				long da=System.currentTimeMillis();				
				create(file);
				logger.info("读取<<"+file.getName()+">>文件耗时"+(System.currentTimeMillis()-da)+"毫秒");
				return;
			}
			File[] files=file.listFiles();
			for(File fi:files){
				if(fi.isFile()){
					for(int i = 0;i<files.length;i++){
						System.out.println("bbbbbbb"+files[i]);
					}
					long da=System.currentTimeMillis();		
					String name=fi.getParentFile().getPath();
					name=name.substring(name.lastIndexOf("\\")+1,name.length());			
					create(fi);
					logger.info("读取"+name+"<<"+fi.getName()+">>文件耗时"+(System.currentTimeMillis()-da)+"毫秒");
				}
				else if(fi.isDirectory()){
					show(fi);
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
		private static void create(File file) throws Exception, UnsupportedEncodingException{
			System.out.println("---create----"+file);
			CollectionDataDao dataDao=new CollectionDataDao();
			dataDao.setSf(sessionFactory);
			CollectDataService service=new CollectDataService();
			service.setiCollectionDataDao(dataDao);		
			ICollectDataService services=service;
			InputStream input=null;
			Workbook wokr= null;
			String name=file.getParentFile().getPath();
			name=name.substring(name.lastIndexOf("\\")+1,name.length());
			try {
				System.out.println("------one------"+file);
				input=CommonConstant.getInputStram(file);//获取文件输入流       
				System.out.println("------1------"+input);
				wokr =CommonConstant.getWorkbook(input);//获取对象数据
				long count = wokr.getSheetAt(0).getLastRowNum();	//统计条数
				System.out.println("------2------"+count);
				List<String[]>  list=CommonConstant.listObject(wokr);//将数据放入到集合中
			   boolean result=services.createJsonData(list);
			   System.out.println("------3------"+list);
			    REPEATSUM+=list.size();
				if(!result){			
					logger.error("读取"+name+"<<"+file.getName()+">>文件时发生JSON异常!");
					ERRORSUM+=count;
				}
				if(result){
				boolean result2=services.createPostgresql(list);
				if(!result2){
				   logger.error("读取"+name+"<<"+file.getName()+">>文件时发生SQL异常!");
				   ERRORSUM+=count;
				 }
				else{
					INPUTSUM+=list.size();
				 }
				}
				//services.createJdbcPostgresql(list);				
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
}
