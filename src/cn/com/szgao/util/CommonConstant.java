package cn.com.szgao.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.szgao.action.Test;
import cn.com.szgao.dto.ArchivesVO;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

/**
 * 公用静态常量
 * @author xcy
 * 2014-12-24
 */
public class CommonConstant {
	/**
	 * 日期类型
	 * xcy
	 * 2014-12-24
	 */
	private static Logger logger = LogManager.getLogger(CommonConstant.class.getName());
	public static final String DATEPATTERN= "yyyy-MM-dd";
	static String url = "jdbc:postgresql://192.168.251:5432/duplicatedb?useServerPrepStmts=false&rewriteBatchedStatements=true";
	static String usr = "postgres";
	static String psd = "615601.xcy*";
	private static Connection connection=null;
	public  static Map<Integer,String[]> TITLEMAP=new HashMap<Integer,String[]>();
	static{
		TITLEMAP.put(0, new String[]{"标题","title"});
		TITLEMAP.put(1, new String[]{"URL","URL"});
		TITLEMAP.put(2, new String[]{"分类","catalog"});
		TITLEMAP.put(3, new String[]{"案号","caseNum"});
		TITLEMAP.put(4, new String[]{"法院名","courtName"});
		TITLEMAP.put(5, new String[]{"发布日期","publishDate"});
		TITLEMAP.put(6, new String[]{"省","city"});
		TITLEMAP.put(8, new String[]{"区","province"});
		TITLEMAP.put(7, new String[]{"市","area"});
		TITLEMAP.put(9, new String[]{"采集时间","collectDate"});
		TITLEMAP.put(10, new String[]{"发布时间","collectDate"});
	}
	/**
	 * 逗号
	 * xcy
	 * 2014-12-24
	 */
	public static final String COMMA = ",";
	//群对象
	private static Cluster cluster = CouchbaseCluster.create("192.168.0.253");
	/**
	 * 每次写数据的批次数
	 */
	public static int NUMBER=1000; 
	/**
	 * 获取文件流数据
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static InputStream getInputStram(String value,String decode) throws FileNotFoundException, UnsupportedEncodingException{		
		return new FileInputStream(new File(URLDecoder.decode(value,decode)));
	}
	/**
	 * 获取文件流数据
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static InputStream getInputStram(String value) throws FileNotFoundException, UnsupportedEncodingException{

		return new FileInputStream(new File(URLDecoder.decode(value,"utf-8")));
	}
	/**
	 * 获取文件流数据
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static InputStream getInputStram(File value) throws FileNotFoundException, UnsupportedEncodingException{
		return new FileInputStream(value);
	}
	/**
	 * 获取工作薄对象数据
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public static Workbook getWorkbook(InputStream inputStream) throws InvalidFormatException, IOException{
		return WorkbookFactory.create(inputStream);
	}
	/**
	 * 设置数据集合
	 */
	public static List<String[]> listObject(Workbook workBook){
		Sheet sheet = workBook.getSheetAt(0);
		int rowNum=0;
		if(null!=sheet.getRow(0)){
			rowNum=sheet.getRow(0).getLastCellNum()-sheet.getRow(0).getFirstCellNum(); 	
		}
		if(rowNum<10){
			return null;
		}
		if(rowNum>10){
			rowNum=10;
		}
		List<String[]> list=new ArrayList<String[]>();
		Map<String,String[]> map=new HashMap<String, String[]>();
		 String[] singleRow = null;
		 Cell cell=null;
		for(Row row:sheet){
			singleRow = new String[rowNum];				  
			  int index=0;
			  for(int i=0;i<rowNum;i++){ 
				   cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK); 
				  switch(cell.getCellType()){  
	               case Cell.CELL_TYPE_BLANK:  
	                 singleRow[index] = "";
	                 break;  
	               case Cell.CELL_TYPE_BOOLEAN:  
	                 singleRow[index] = Boolean.toString(cell.getBooleanCellValue());  
	                 break;  
	                //数值  
	               case Cell.CELL_TYPE_NUMERIC:                 
	                 if(DateUtil.isCellDateFormatted(cell)){  
	                   singleRow[index] = String.valueOf(cell.getDateCellValue());  
	                 }else{   
	                   cell.setCellType(Cell.CELL_TYPE_STRING);  
	                   String temp = cell.getStringCellValue();  
	                   //判断是否包含小数点，如果不含小数点，则以字符串读取，如果含小数点，则转换为Double类型的字符串  
	                   if(temp.indexOf(".")>-1){  
	                     singleRow[index] = String.valueOf(new Double(temp)).trim();  
	                   }else{  
	                     singleRow[index] = temp.trim();  
	                   }  
	                 }  
	                 break;  
	               case Cell.CELL_TYPE_STRING:
	            	 singleRow[index] = cell.getStringCellValue().trim(); 		                 
	                 break;  
	               case Cell.CELL_TYPE_ERROR:  
	                 singleRow[index] = "";  
	                 break;    
	               case Cell.CELL_TYPE_FORMULA:  
	                 cell.setCellType(Cell.CELL_TYPE_STRING);  
	                 singleRow[index] = cell.getStringCellValue();  
	                 if(singleRow[index]!=null){  
	                   singleRow[index] = singleRow[index].replaceAll("#N/A","").trim();  
	                 }  
	                 break;    
	               default:  
	                 singleRow[index] = "";  
	                 break;  
	             }  				  
				 index++;
			  }	
			  if(0 == row.getRowNum())
	    	  {
				 if(toCompareTitle(singleRow)==false){
					 return null;
				 }
	    		  continue;
	    	  }
			  if("".equals(singleRow[1])){
				  continue;
			   }			
			  //list.add(singleRow);
			  map.put(singleRow[1], singleRow);
		}
		if(null==map||map.size()<=0){
			return null;
		}
		for(Map.Entry<String,String[]> ma:map.entrySet()){
			list.add(ma.getValue());
		}
		return list;
	}
	/**
	 * 连接CouchBase数据库
	 * 作用：连接库
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	public static Bucket connectionCouchBase(){
		//连接指定的桶		
		return cluster.openBucket("court");
	} 
	
	public static Bucket connectionCouchBasexcy(){
		//连接指定的桶		
		return cluster.openBucket("xcy_test2");
//		return cluster.openBucket("court");
	} 
	/**
	 * 公告
	 * @return
	 */
	public static Bucket connectionCouchBaseNotice(){
		//连接指定的桶		
		return cluster.openBucket("court");
	} 
	/**
	 * 生成UUID
	 */
	public static String getUUID(String value){
		NameBasedGenerator nbg = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_DNS);
		return nbg.generate(value).toString();
	}
	
	public static Connection getConnection() throws SQLException{
		if(connection==null){
			logger.info("创建conneciton连接!");
			connection= DriverManager.getConnection(url, usr, psd);
		}
	    return connection;
	}
	/**
	 * 比较标题列是否正确
	 * @param values
	 * @return
	 */
	public static boolean toCompareTitle(String[] values){
		int index=0;
		String[] vals;
		boolean result=false;
		for(String val:values){
			vals=TITLEMAP.get(index++);
			for(String val2:vals){
				if(val2.equalsIgnoreCase(val)){
					result=true;
					break;
				}
			}
			if(result==false){
				return false;
			}
		}
		return true;
	}
	public static List<ArchivesVO> getListExcel(Workbook workBook,int count){
		Sheet sheet = workBook.getSheetAt(0);
		List<ArchivesVO> list=new ArrayList<ArchivesVO>();
		 Cell cell=null;
		 ArchivesVO arch=null;
		 for(Row row:sheet){
			      arch=new ArchivesVO();				  
				  for(int i=0;i<count;i++){ 
					   cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK); 
					   arch.setDetailLink(cell.getStringCellValue().trim());
				  }
				  list.add(arch);
			}			
		return list;
	}
}
