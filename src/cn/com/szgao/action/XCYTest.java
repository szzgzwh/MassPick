package cn.com.szgao.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

import cn.com.szgao.dao.impl.CollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.RecordData;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;
import cn.com.szgao.util.CommonConstant;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
public class XCYTest {




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
		public static void main(String[] args) throws Exception {	
			//导入文件地址
			File file=new File("F:\\DataSheet\\excel\\3月采集-20150002\\3月采集-20150002");
//			File file=new File("F:\\DataSheet\\excel\\problems\\浙江省\\浙江法院公开网");
			try {
				show(file);	
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
			finally{
				sessionFactory.close();
			}
			record();
	       
		}
		/**
		 * 递归遍历文件
		 * @param file
		 * @throws  
		 * @throws Exception 
		 */
		private static void show(File file) throws Exception{
			if(file.isFile()){
				long da=System.currentTimeMillis();				
//				readFileByLines(file);
				create(file);
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
//					readFileByLines(fi);
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
		private static <ObjectDataVO> void create(File file) throws Exception, UnsupportedEncodingException{
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
				input=CommonConstant.getInputStram(file);//获取文件输入流       
				wokr =CommonConstant.getWorkbook(input);//获取对象数据
				long count = wokr.getSheetAt(0).getLastRowNum();	//统计条数
				List<String[]>  list=CommonConstant.listObject(wokr);//将数据放入到集合中
			   boolean result=services.createJsonData(list);//需要改PG表名
			    REPEATSUM+=list.size();
				if(!result){			
					logger.error("读取"+name+"<<"+file.getName()+">>文件时发生JSON异常!");
					ERRORSUM+=count;
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
		
		/**
		 * @param file
		 * @throws Exception 
		 */
		 public static  void readFileByLines(File file) throws Exception {
			 CollectionDataDao dataDao=new CollectionDataDao();
				dataDao.setSf(sessionFactory);
				CollectDataService service=new CollectDataService();
				service.setiCollectionDataDao(dataDao);		
				ICollectDataService services=service;
				String name=file.getParentFile().getPath();
				name=name.substring(name.lastIndexOf("\\")+1,name.length());
		        BufferedReader reader = null;
		        String temp = null;
		        StringBuffer sb = new StringBuffer();
		        Gson gs = new Gson();	
		        try {
		            reader = new BufferedReader(new FileReader(file));
		            System.out.println("以行为单位读取文件内容，一次读一整行：");
		            // 一次读入一行，直到读入null为文件结束
		            while ((temp = reader.readLine()) != null) {
		            	sb.append(temp+",");
		            }
		            String jsonString = sb.toString();
		            jsonString =jsonString.substring(0, jsonString.lastIndexOf(","));
		            String doString = "["+jsonString+"]";	           
//		            System.out.println("----------:"+doString);
		            List<ArchivesVO> jsonList = gs.fromJson(doString, new TypeToken<List<ArchivesVO>>(){}.getType());	
//		            System.out.println("---------jsonList:" + jsonList);
		            boolean result=services.createJson(jsonList);
		            long count = jsonList.size();	//统计条数
				    REPEATSUM+=jsonList.size();//去重统计
					if(!result){			
						logger.error("读取"+name+"<<"+file.getName()+">>文件时发生JSON异常!");
						ERRORSUM+=count;
					}
		            reader.close();
		        	statisticalCount(file,count);
		        } catch (IOException e) {
		        	logger.error("读取"+name+"<<"+file.getName()+">>文件时发生IO异常:"+e.getMessage());	
		        } finally {
		            if (reader != null) {
		                try {
		                    reader.close();
		                } catch (IOException e1) {
		                }
		            }
		        }
		    }

}
