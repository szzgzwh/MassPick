package cn.com.szgao.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import cn.com.szgao.dao.api.ICollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.CompanyVO;
import cn.com.szgao.dto.NetUrlVO;
import cn.com.szgao.dto.ObjectVO;
import cn.com.szgao.dto.UserVO;
import cn.com.szgao.util.CommonConstant;

/**
 * Dao层接口
 * 作用：数据传输层
 * 2015-2-11
 * @author xiongchangyi
 */
public class CollectionDataDao extends BaseDao implements ICollectionDataDao {	
	/**
	 * 写日志
	 */
	private static Logger logger = LogManager.getLogger(CollectionDataDao.class.getName());
	
	/**
	 * 插入法院数据到CouchBase
	 * @param objectVO
	 * @throws Exception
	 * @author xiongchangyi
	 * @since 2015-2-11
	 */
	public ObjectVO createDocument(ObjectVO objectVO)throws Exception{	
		//存放Excel里面数据集合
		List<String[]> dataList = new ArrayList<String[]>();
		//SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		//连接库
		Bucket bucket = this.connectionCouchBase();
		InputStream is = null;
		Workbook  wb = null;
		//列数
		int columnNum = 0; 
		//要解析的Excel文件
		File file = null;
		if(null !=objectVO)
		{
			file = new File(URLDecoder.decode(objectVO.getObjName(), "UTF-8"));
			//如果文件不存在返回0
			if(!file.exists())
			{
				ObjectVO vo = new ObjectVO();
				vo.setObjId((long)0);
				return vo;
			}
			is = new FileInputStream(file);  
			//创建工作簿对象
			wb = WorkbookFactory.create(is);
			//获得第1个默认的sheet
			Sheet sheet = wb.getSheetAt(0);
			if(sheet.getRow(0)!=null){  
		        columnNum = sheet.getRow(0).getLastCellNum()-sheet.getRow(0).getFirstCellNum();  
		    }  
			if(columnNum>0)
			{  
			      for(Row row:sheet)
			      {  
			    	  if(0 == row.getRowNum())
			    	  {
			    		  continue;
			    	  }
			          String[] singleRow = new String[columnNum];  
			          int n = 0;  
			          for(int i=0;i<columnNum;i++){ 			        	
			             Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);  
			             switch(cell.getCellType()){  
			               case Cell.CELL_TYPE_BLANK:  
			                 singleRow[n] = "";  
			                 break;  
			               case Cell.CELL_TYPE_BOOLEAN:  
			                 singleRow[n] = Boolean.toString(cell.getBooleanCellValue());  
			                 break;  
			                //数值  
			               case Cell.CELL_TYPE_NUMERIC:                 
			                 if(DateUtil.isCellDateFormatted(cell)){  
			                   singleRow[n] = String.valueOf(cell.getDateCellValue());  
			                 }else{   
			                   cell.setCellType(Cell.CELL_TYPE_STRING);  
			                   String temp = cell.getStringCellValue();  
			                   //判断是否包含小数点，如果不含小数点，则以字符串读取，如果含小数点，则转换为Double类型的字符串  
			                   if(temp.indexOf(".")>-1){  
			                     singleRow[n] = String.valueOf(new Double(temp)).trim();  
			                   }else{  
			                     singleRow[n] = temp.trim();  
			                   }  
			                 }  
			                 break;  
			               case Cell.CELL_TYPE_STRING:
			            	 singleRow[n] = cell.getStringCellValue().trim(); 		                 
			                 break;  
			               case Cell.CELL_TYPE_ERROR:  
			                 singleRow[n] = "";  
			                 break;    
			               case Cell.CELL_TYPE_FORMULA:  
			                 cell.setCellType(Cell.CELL_TYPE_STRING);  
			                 singleRow[n] = cell.getStringCellValue();  
			                 if(singleRow[n]!=null){  
			                   singleRow[n] = singleRow[n].replaceAll("#N/A","").trim();  
			                 }  
			                 break;    
			               default:  
			                 singleRow[n] = "";  
			                 break;  
			             }  
			             n++;  
			          }   
			          if("".equals(singleRow[0])){continue;}//如果第一行为空，跳过  
			          dataList.add(singleRow); 			          
			      }		
		
			 }	
		}
		if(null !=dataList && 0!= dataList.size())
		{
			NameBasedGenerator nbg = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
			JsonObject obj = null;	
			Session session=null;
			for(int i = 0;i<dataList.size();i++)
			{
				//标题，URL+，分类，字号，法院名+，发布日期+，省，市，区，采集时间			
				obj = JsonObject.empty().put("title",dataList.get(i)[0]).put("detailLink",dataList.get(i)[1])
						.put("classify",dataList.get(i)[2]).put("caseNO",dataList.get(i)[3]).put("courtName", dataList.get(i)[4])
						.put("province", dataList.get(i)[5]).put("city", dataList.get(i)[6]).put("area",dataList.get(i)[7])
						.put("publishTime",dataList.get(i)[8]).put("collectDate",dataList.get(i)[9]);
				//需要指定URL生成UUID的字符串作为参数				
				String urlId = nbg.generate(dataList.get(i)[1]).toString();
				//创建JSON文档	
				JsonDocument doc = JsonDocument.create(urlId, obj);
				//插入JSON文档到库 
				bucket.upsert(doc);
				//用hibernate存储数据到PostGreSQL库
				 session = this.openSession();
				Transaction t = session.beginTransaction();				
				//查询
				@SuppressWarnings("unchecked")
				List<NetUrlVO> resultList = session.createQuery("from NetUrlVO vo where vo.urlId=?").setString(0, urlId).list();
				if(null !=resultList && resultList.size()!=0)
				{
					resultList = null;
					//关闭会话对象
					session.close();
					continue;
				}
				//用户序号是2
				UserVO user = new UserVO();
				user.setUserId((long)2);
				//创建URL对象，把对象保存到postgresql里面去						
				NetUrlVO urlVO = new NetUrlVO(urlId,dataList.get(i)[1],dataList.get(i)[0],(byte)1,new Date(),user,new Date(),user);	
				//存储urlVO对象
				session.save(urlVO);
				t.commit();
				//释放用户对象
				urlVO = null;
				//释放用户对象
				user = null;
				//关闭会话对象
				session.close();			
			}
		}
		//关闭连接
		this.closeConnectionCouchBase();
		is.close();
		is = null;
		wb.close();
		wb = null;
		//成功则返回1
		ObjectVO vo = new ObjectVO();
		vo.setObjId((long)1);
		return vo;
	}
	/**
	 * 处理企业名录的日期，广东第1分excel
	 * @param vo 文档路径
	 * @return
	 * @throws Exception
	 */
	public ObjectVO updateDate(ObjectVO vo)throws Exception
	{
		//存放Excel里面数据集合
		List<String[]> dataList = new ArrayList<String[]>();
		InputStream is = null;
		Workbook  wb = null;
		//列数
		int columnNum = 0; 
		//要解析的Excel文件
		File file = null;
		if(null !=vo)
		{
			file = new File(URLDecoder.decode(vo.getObjName(), "UTF-8"));
			//如果文件不存在返回0
			if(!file.exists())
			{
				ObjectVO obj = new ObjectVO();
				obj.setObjId((long)0);
				return obj;
			}
			//文件名称
			is = new FileInputStream(file);  
			//创建工作簿对象
			wb = WorkbookFactory.create(is);
			//获得第1个默认的sheet
			Sheet sheet = wb.getSheetAt(0);
			if(sheet.getRow(0)!=null){  
		        columnNum = sheet.getRow(0).getLastCellNum()-sheet.getRow(0).getFirstCellNum();  
		    }  
			if(columnNum>0)
			{  
			      for(Row row:sheet)
			      {  
			    	  if(0 == row.getRowNum())
			    	  {
			    		  continue;
			    	  }
			          String[] singleRow = new String[columnNum];  
			          int n = 0;  
			          for(int i=0;i<columnNum;i++){ 			        	
			             Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);  
			             switch(cell.getCellType()){  
			               case Cell.CELL_TYPE_BLANK:  
			                 singleRow[n] = "";  
			                 break;  
			               case Cell.CELL_TYPE_BOOLEAN:  
			                 singleRow[n] = Boolean.toString(cell.getBooleanCellValue());  
			                 break;  
			                //数值  
			               case Cell.CELL_TYPE_NUMERIC:                 
			                 if(DateUtil.isCellDateFormatted(cell)){  
			                   singleRow[n] = String.valueOf(cell.getDateCellValue());  
			                 }else{   
			                   cell.setCellType(Cell.CELL_TYPE_STRING);  
			                   String temp = cell.getStringCellValue();  
			                   //判断是否包含小数点，如果不含小数点，则以字符串读取，如果含小数点，则转换为Double类型的字符串  
			                   if(temp.indexOf(".")>-1){  
			                     singleRow[n] = String.valueOf(new Double(temp)).trim();  
			                   }else{  
			                     singleRow[n] = temp.trim();  
			                   }  
			                 }  
			                 break;  
			               case Cell.CELL_TYPE_STRING:
			            	 singleRow[n] = cell.getStringCellValue().trim(); 		                 
			                 break;  
			               case Cell.CELL_TYPE_ERROR:  
			                 singleRow[n] = "";  
			                 break;    
			               case Cell.CELL_TYPE_FORMULA:  
			                 cell.setCellType(Cell.CELL_TYPE_STRING);  
			                 singleRow[n] = cell.getStringCellValue();  
			                 if(singleRow[n]!=null){  
			                   singleRow[n] = singleRow[n].replaceAll("#N/A","").trim();  
			                 }  
			                 break;    
			               default:  
			                 singleRow[n] = "";  
			                 break;  
			             }  
			             n++;  
			          }   
			          if("".equals(singleRow[0])){continue;}//如果第一行为空，跳过  
			          dataList.add(singleRow); 			          
			      }		
		
			 }	
		}
		if(null !=dataList && 0!= dataList.size())
		{
			for(int i = 0;i<dataList.size();i++)
			{						
				//用hibernate存储数据到PostGreSQL库
				Session session = this.openSession();
				Transaction t = session.beginTransaction();						
				//创建URL对象，把对象保存到postgresql里面去						
				CompanyVO comVo = new CompanyVO(Long.parseLong(dataList.get(i)[0]),dataList.get(i)[31]);
				//存储urlVO对象
				session.update(comVo);
				t.commit();
				//释放用户对象
				comVo = null;					
				//关闭会话对象
				session.close();			
			}
		}
		is.close();
		is = null;
		wb.close();
		wb = null;
		//成功则返回1
		ObjectVO obj = new ObjectVO();
		obj.setObjId((long)1);
		return obj;
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
		Map<String,Integer> staMap = new HashMap<String,Integer>();
		staMap.put("公司名称", 0);
		staMap.put("单位名称", 1);
		staMap.put("企业名称", 2);
		staMap.put("负责人", 3);
		staMap.put("联系人", 4);
		staMap.put("职位", 5);
		staMap.put("行政区划", 6);
		staMap.put("单位地址", 7);
		staMap.put("地址", 63);
		staMap.put("区号", 8);
		staMap.put("电话", 9);
		staMap.put("电话号码", 10);
		staMap.put("公司电话", 11);
		staMap.put("分机", 12);
		staMap.put("移动电话", 13);
		staMap.put("传真", 14);
		staMap.put("公司传真", 15);
		staMap.put("邮政编码", 16);
		staMap.put("邮编", 17);
		staMap.put("EMAIL", 18);
		staMap.put("邮箱", 19);
		staMap.put("网址", 20);
		staMap.put("经济类型", 21);
		staMap.put("企业类型", 62);
		staMap.put("经营范围", 22);
		staMap.put("主营产品", 23);
		staMap.put("主营行业", 24);
		staMap.put("行业代码", 25);
		staMap.put("经济代码", 26);
		staMap.put("隶属关系", 27);
		staMap.put("开业时间", 28);
		staMap.put("注册日期", 29);
		staMap.put("成立日期", 30);
		staMap.put("成立时间", 31);
		staMap.put("营业状态", 32);
		staMap.put("会计制度", 33);
		staMap.put("机构类型", 34);
		staMap.put("注册资金", 35);
		staMap.put("年营业额", 36);
		staMap.put("营业收入", 37);
		staMap.put("资产总计", 38);
		staMap.put("职工人数", 39);
		staMap.put("员工人数", 59);
		staMap.put("公司主页", 59);
		staMap.put("基本信息", 40);
		staMap.put("经营模式", 41);
		staMap.put("注册地址", 42);
		staMap.put("经营地址", 60);
		staMap.put("主要市场", 43);
		staMap.put("经营品牌", 44);
		staMap.put("主要客户", 45);
		staMap.put("管理体系", 46);
		staMap.put("银行帐号", 47);
		staMap.put("开户银行", 48);
		staMap.put("是否OEM", 49);
		staMap.put("研发人数", 50);
		staMap.put("厂房面积", 51);
		staMap.put("质量控制", 52);
		staMap.put("月产量", 53);
		staMap.put("信用等级", 54);
		staMap.put("年进口额", 55);
		staMap.put("年出口额", 56);
		staMap.put("类型", 57);
		staMap.put("法人", 58);	
		
		//目录路径
		String fileName = URLDecoder.decode(objVO.getObjName(), "UTF-8");
		File file = new File(fileName);
		//目录下的所有文件
		File[] files = file.listFiles();
		//存放Excel里面数据集合
		List<String[]> dataList = new ArrayList<String[]>();
		//每列的名称和excel列号索引找到
		Map<Integer,String> map = new HashMap<Integer,String>();
		InputStream is = null;
		Workbook  wb = null;
		//每个簿的列数
		int columnNum = 0;
		//每个省的总条数
		//long allCount = 0;
		if(null != files)
		{
			for(int j = 0; j<files.length; j++)
			{
				is = new FileInputStream(files[j]);
				wb = WorkbookFactory.create(is);
				//获得第1个默认的sheet
				Sheet sheet = wb.getSheetAt(0);				
				//把每个工作簿的总行数统计到allCount变量，即：统计每个省的总数量
				//allCount += sheet.getLastRowNum()-1;
				if(sheet.getRow(0)!=null){  
			        columnNum = sheet.getRow(0).getLastCellNum()-sheet.getRow(0).getFirstCellNum();  
			    }
				//缺少集合中staMap标准的字段
				//String lostColumn = "";
				if(columnNum>0)
				{  
				      for(Row row:sheet)
				      {  				    	 
				    	 // logger.info(file.getName()+"has"+row.getRowNum()+"row data.");
				    	  //标题行
				    	 if(0 == row.getRowNum())
				    	  {
				    		  //此循环目的把每列的名称和excel列号索引找到
				    		  for(int i=0;i<columnNum;i++)
				    		  { 
				    			  Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
				    			  map.put(i,cell.getStringCellValue());
				    		  }
				    		  continue;
				    	  }
				    	  //把列里面的数据放入数组
				          String[] singleRow = new String[columnNum];  
				          //统计空单元格数据
				          int n = 0;  
				          for(int i = 0; i < columnNum; i++)
				          { 			        	
				             Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);  
				             switch(cell.getCellType()){  
				               case Cell.CELL_TYPE_BLANK:  
				                 singleRow[n] = "";
				                 break;  
				               case Cell.CELL_TYPE_BOOLEAN:  
				                 singleRow[n] = Boolean.toString(cell.getBooleanCellValue());  
				                 break;  
				                //数值  
				               case Cell.CELL_TYPE_NUMERIC:                 
				                 if(DateUtil.isCellDateFormatted(cell)){  
				                   singleRow[n] = String.valueOf(cell.getDateCellValue());  
				                 }else{   
				                   cell.setCellType(Cell.CELL_TYPE_STRING);  
				                   String temp = cell.getStringCellValue();  
				                   //判断是否包含小数点，如果不含小数点，则以字符串读取，如果含小数点，则转换为Double类型的字符串  
				                   if(temp.indexOf(".")>-1){  
				                     singleRow[n] = String.valueOf(new Double(temp)).trim();  
				                   }else{  
				                     singleRow[n] = temp.trim();  
				                   }  
				                 }  
				                 break;  
				               case Cell.CELL_TYPE_STRING:
				            	 singleRow[n] = cell.getStringCellValue().trim(); 		                 
				                 break;  
				               case Cell.CELL_TYPE_ERROR:  
				                 singleRow[n] = "";  
				                 break;    
				               case Cell.CELL_TYPE_FORMULA:  
				                 cell.setCellType(Cell.CELL_TYPE_STRING);  
				                 singleRow[n] = cell.getStringCellValue();  
				                 if(singleRow[n]!=null){  
				                   singleRow[n] = singleRow[n].replaceAll("#N/A","").trim();  
				                 }  
				                 break;    
				               default:  
				                 singleRow[n] = "";  
				                 break;  
				             }  
				             n++;  
				          }   
				          //如果第一行为空，跳过  
				          if("".equals(singleRow[0]))
				          {
				        	  continue;
				          }
				          dataList.add(singleRow); 			          
				      }//for  Row row:sheet	
				      
				      CompanyVO comVO = null;				      
				      //处理插库的逻辑；下面是遍历行
				      for(int k = 0; k < dataList.size(); k++)
				      {
				    	  //用hibernate存储数据到PostGreSQL库
				    	  Session session = this.openSession();
				    	  Transaction t = session.beginTransaction();	
				    	  comVO = new CompanyVO();
				    	  for(int p = 0; p < map.size(); p++)
				    	  {
				    		 if(map.get(p).equals("公司名称")||map.get(p).equals("单位名称")|| map.get(p).equals("企业名称"))
				    		 { 				    			 
				    			/*//去掉相同公司的数据,把公司名称缓存				    			
				    			Query query = session.createQuery("from CompanyVO c where c.companyName=?");
				    			query.setString(0,dataList.get(k)[p].toLowerCase());
				    			query.setCacheable(true);				    			
								@SuppressWarnings("unchecked")
								List<CompanyVO> list =query.list();
				    			 if(null != list && list.size()!=0)
				    			 {				    				 
				    				 break;
				    			 }*/
				    			 comVO.setCompanyName(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("负责人"))
				    		 {
				    			 comVO.setResponsiblePerson(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("联系人"))
				    		 {
				    			 comVO.setContactPerson(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("职位"))
				    		 {
				    			 comVO.setOccupPostion(dataList.get(k)[p].trim());
				    			 
				    		 }
				    		 else if(map.get(p).equals("行政区划"))
				    		 {
				    			 comVO.setCompartment(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("单位地址")||map.get(p).equals("地址"))
				    		 {
				    			 comVO.setEnterpAddress(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("经营地址"))
				    		 {
				    			 comVO.setManageAddress(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("区号"))
				    		 {
				    			 comVO.setAreaCode(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("电话")||map.get(p).equals("电话号码")||map.get(p).equals("公司电话"))
				    		 {
				    			 comVO.setEnterpTell(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("分机"))
				    		 {
				    			 comVO.setEnterpExten(dataList.get(k)[p].trim());   
				    		 }
				    		 else if(map.get(p).equals("移动电话"))
				    		 {
				    			 comVO.setEnterpMobile(dataList.get(k)[p].trim());
				    		 }
				    		/* else if(map.get(p).equals("传真")||map.get(p).equals("公司传真"))
				    		 {
				    			 comVO.setEnterpFax(dataList.get(k)[p].trim());
				    		 }*/
				    						    	
				    		 else if(map.get(p).equals("邮政编码")||map.get(p).equals("邮编"))
				    		 {
				    			 comVO.setPostCode(dataList.get(k)[p].trim());
				    		 }
				    		else if(map.get(p).equals("EMAIL")||map.get(p).equals("邮箱"))
				    		 {
				    			 comVO.setEnterpEmail(dataList.get(k)[p].trim());
				    		 }
				    		/* else if(map.get(p).equals("网址")||map.get(p).equals("公司主页"))
				    		 {
				    			 comVO.setEnterpUrl(dataList.get(k)[p].trim());
				    		 }*/
				    		 else if(map.get(p).equals("经济类型")||map.get(p).equals("企业类型"))
				    		 {
				    			 comVO.setEconomicType(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("经营范围"))
				    		 {
				    			 comVO.setManageScope(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("主营产品"))
				    		 {
				    			 comVO.setManageProduct(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("主营行业"))
				    		 {
				    			 comVO.setManageTrade(dataList.get(k)[p].trim());
				    		 }
				    		/* else if(map.get(p).equals("行业代码"))
				    		 {
				    			 comVO.setIndustryCode(dataList.get(k)[p].trim());
				    		 }*/
				    		 else if(map.get(p).equals("经济代码"))
				    		 {
				    			 comVO.setEconomicCode(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("隶属关系"))
				    		 {
				    			 comVO.setSubordRelation(dataList.get(k)[p].trim());
				    		 }
				    		 /*else if(map.get(p).equals("开业时间"))
				    		 {
				    			 comVO.setOpenDate(dataList.get(k)[p].trim());
				    		 }*/
				    		 else if(map.get(p).equals("注册日期")||map.get(p).equals("成立日期")||map.get(p).equals("成立时间"))
				    		 {
				    			 //需要处理只有/的情况
				    			 comVO.setRegistDate(dataList.get(k)[p].trim());
				    		 }
				    		else if(map.get(p).equals("营业状态"))
				    		 {
				    			 comVO.setManageState(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("会计制度"))
				    		 {
				    			 comVO.setAccountSystem(dataList.get(k)[p].trim());
				    		 }
				    		/* else if(map.get(p).equals("机构类型"))
				    		 {
				    			 comVO.setOrgType(dataList.get(k)[p].trim());
				    		 }*/
				    		 else if(map.get(p).equals("注册资金"))
				    		 {
				    			 comVO.setRegistCapital(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("年营业额"))
				    		 {
				    			 comVO.setYearRurnover(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("营业收入"))
				    		 {
				    			 comVO.setOperationRevenue(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("资产总计"))
				    		 {
				    			 comVO.setTotalAssets(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("职工人数")||map.get(p).equals("员工人数"))
				    		 {
				    			 comVO.setEmployeeAmount(dataList.get(k)[p]);
				    		 }
				    		 else if(map.get(p).equals("基本信息"))
				    		 {
				    			 comVO.setBriefIntroduct(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("经营模式"))
				    		 {
				    			 comVO.setManageModel(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("注册地址"))
				    		 {
				    			 comVO.setRegistAddress(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("主要市场"))
				    		 {
				    			 comVO.setMainMarke(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("经营品牌"))
				    		 {
				    			 comVO.setManageBrand(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("主要客户"))
				    		 {
				    			 comVO.setMainCustom(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("管理体系"))
				    		 {
				    			 comVO.setManageSystem(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("银行帐号"))
				    		 {
				    			 comVO.setBankAccount(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("开户银行"))
				    		 {
				    			 comVO.setBankName(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("是否OEM"))
				    		 {
				    			 if("".equals(dataList.get(k)[p]) || null == dataList.get(k)[p])
				    			 {
				    				 continue;
				    			 }
				    			 if("否".equals(dataList.get(k)[p].trim()))
				    			 {
				    				 comVO.setOemIs((byte)0);
				    			 }
				    			 else
				    			 {
				    				 comVO.setOemIs((byte)1);
				    			 }
				    		 }
				    		 else if(map.get(p).equals("研发人数"))
				    		 {
				    			 comVO.setDevelopAmount(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("厂房面积"))
				    		 {
				    			 comVO.setFactoryArea(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("质量控制"))
				    		 {
				    			 comVO.setQualityControl(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("月产量"))
				    		 {
				    			 comVO.setMonthOutput(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("信用等级"))
				    		 {
				    			 comVO.setCreditRat(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("年进口额"))
				    		 {
				    			 comVO.setImportAmount(dataList.get(k)[p].trim());
				    		 }				    		 
				    		 else if(map.get(p).equals("年出口额"))
				    		 {
				    			 comVO.setOutAmount(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("类型"))
				    		 {
				    			 comVO.setCredibilityType(dataList.get(k)[p].trim());
				    		 }
				    		 else if(map.get(p).equals("法人"))
				    		 {
				    			 comVO.setLegalPerson(dataList.get(k)[p].trim());
				    		 }
				    	  }
				    	if(null!=comVO && null ==comVO.getCompanyName())
				    	{
				    		session.close();
				    		session = null;
				    		comVO = null;
				    		continue;
				    	}
				    	session.save(comVO);
				    	t.commit();
				    	comVO = null;
						session.close();  
						session = null;
				      }//遍历列				      
				      //logger.info(files[j].getName()+"缺少字段："+lostColumn+"\t"+files[j].getName()+"共有"+(sheet.getLastRowNum()-1)+"条数据!");
				 }//if columnNum>0 				
			   wb = null;
			   is = null;	
			   files[j].delete();
			   logger.info(files[j].getName());
			}//for  files
			//logger.info(fileName.substring(fileName.lastIndexOf("\\")+1,fileName.indexOf("("))+allCount);
		}//if
		ObjectVO vo = new ObjectVO();
		vo.setObjId((long)1);
		return vo;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean createPostgresql(List<String[]> list) throws Exception {
		Session session = this.openSession();
		Transaction t =session.beginTransaction();	
		UserVO user=null;
		int i=0;
		int sum=list.size();
		NetUrlVO urlVO=null;
		List<NetUrlVO> resultList =null;
		String urlId=null;
		for(i=0;i<sum;i++)
		{						
			//需要指定URL生成UUID的字符串作为参数				
			 urlId = CommonConstant.getUUID(list.get(i)[1]).toString();			
			 resultList = session.createQuery("from NetUrlVO vo where vo.urlId=?").setString(0, urlId).list();	
			if(null==resultList||resultList.size()<=0){
				//用户序号是2
				user = new UserVO();
				user.setUserId((long)2);
				//创建URL对象，把对象保存到postgresql里面去						
				 urlVO = new NetUrlVO(urlId,list.get(i)[1],list.get(i)[0],(byte)1,new Date(),user,new Date(),user);	
				//存储urlVO对象
				session.save(urlVO);				
			}
		}		
		t.commit();
		session.close();
		return true;
	}
	/**
	 * 用jdbc写数据
	 * @throws SQLException 
	 */
	public boolean jdbcCreatePostgresql(List<String[]> list) throws SQLException{		
		ConnectionProvider cp = ((SessionFactoryImplementor)this.getSf()).getConnectionProvider();
		Connection conn=cp.getConnection();	
		PreparedStatement stmt=null;
		PreparedStatement stmt2=null;
		ResultSet rset=null;
		String urlId=null;
		conn.setAutoCommit(false); 
		int i=0;
		int sum=list.size();
		//stmt=conn.prepareStatement("SELECT COUNT(*) FROM extract_url_t WHERE URL_ID=?");
		int count=list.size();
		int index=1;
		String value=jionSql(count);
		stmt=conn.prepareStatement(value);
		for(String[] val:list){
			urlId = CommonConstant.getUUID(val[1]).toString();	
			stmt.setString(index++, urlId);
		}
		rset =stmt.executeQuery();
		Map<String,String> map=getMap(rset);
		stmt2=conn.prepareStatement("INSERT INTO extract_url_t(URL_ID,URL,URL_TEXT,URL_STATE,CREATE_DATE,LAST_MODIFY_DATE) values(?,?,?,?,?,?)");
		String urlvalue=null;
		try {
			for(i=0;i<sum;i++)
			{						
				//需要指定URL生成UUID的字符串作为参数				
				 urlId = CommonConstant.getUUID(list.get(i)[1]).toString();						 
				 //stmt.setString(1,urlId);			
				 //rset =stmt.executeQuery();
				 java.sql.Date data=new java.sql.Date(System.currentTimeMillis());
				 urlvalue=map.get(urlId);
				 if(null==urlvalue){
					 stmt2.setString(1,urlId);	
					 stmt2.setString(2,list.get(i)[1]);	
					 stmt2.setString(3,list.get(i)[0]);
					 stmt2.setShort(4, (byte)1);
					 stmt2.setDate(5,data);
//					 stmt2.setLong(6,2l);
					 stmt2.setDate(6,data);
//					 stmt2.setLong(8,2l);
					 stmt2.addBatch();
				 }
				 /*while(rset.next()){
					 int count=rset.getInt(1);
					 if(count==0){					
						 stmt2.setString(1,urlId);	
						 stmt2.setString(2,list.get(i)[1]);	
						 stmt2.setString(3,list.get(i)[0]);
						 stmt2.setShort(4, (byte)1);
						 stmt2.setDate(5,data);
						 stmt2.setLong(6,2l);
						 stmt2.setDate(7,data);
						 stmt2.setLong(8,2l);
						 stmt2.addBatch();
					 }				 							
				 }		*/	 
			}		
		} catch (Exception e) {
			  logger.error(e.getMessage());
			  conn.rollback();
			  return false;
		}
		finally{
			stmt2.executeBatch();
			conn.commit();		
			System.out.println("-------stmt2----"+stmt2);
			stmt2.clearBatch();
			rset.close();
			stmt2.close();
			stmt.close();
			conn.close();
			map.clear();	
		}						
		 return true;
	}
	
	/**
	 * 用jdbc把Json数据写入PG
	 * 裁判文书
	 */
	public boolean jdbcCreateJSONPostgresql(List<ArchivesVO> list) throws SQLException{
			ConnectionProvider cp = ((SessionFactoryImplementor)this.getSf()).getConnectionProvider();
			Connection conn=cp.getConnection();	
			PreparedStatement stmt=null;//供查询使用
			PreparedStatement stmt2=null;//供插入操作使用
			ResultSet rset=null;
			conn.setAutoCommit(false); 
			int i=0;
			int sum=list.size();
			int count=list.size();
			int index=1;
			String value=JsonSql(count);
			stmt=conn.prepareStatement(value);
			String urlId = null;

			for(int n=0;n<list.size();n++){
				urlId= CommonConstant.getUUID(list.get(n).getDetailLink().toString());
				stmt.setString(index++, urlId);
			}
			rset =stmt.executeQuery();
			Map<String,String> map=JsongetMap(rset);
			stmt2=conn.prepareStatement("INSERT INTO  filed_zwh_test(URL_ID,URL,URL_TEXT,URL_STATE,CREATE_DATE,LAST_MODIFY_DATE) values(?,?,?,?,?,?)");
			String urlvalue=null;
			
			try {
				for(i=0;i<sum;i++)
				{						
					//需要指定URL生成UUID的字符串作为参数			
					  urlId =CommonConstant.getUUID(list.get(i).getDetailLink().toString());
//					urlId = CommonConstant.getUUID(list.get(i).toString());	//根据整条数据生成UUID	
					java.sql.Date data=new java.sql.Date(System.currentTimeMillis());
					urlvalue=map.get(urlId);
					if(null==urlvalue){
						stmt2.setString(1,urlId);	
						stmt2.setString(2,list.get(i).getDetailLink().toString());	
						stmt2.setString(3,list.get(i).getTitle().toString());
						stmt2.setShort(4, (byte)1);
						stmt2.setDate(5,data);
						stmt2.setDate(6,data);
						stmt2.addBatch();
					}
				}		
			} catch (Exception e) {
				logger.error(e.getMessage());
				conn.rollback();
				return false;
			}
			finally{
				stmt2.executeBatch();
				stmt2.clearBatch();
				map.clear();	
				conn.commit();		
				rset.close();
				stmt.close();
				stmt2.close();
				conn.close();
			}
		return true;
	}
	/**
	 * 设置map数据
	 * @param rset
	 * @return
	 * @throws SQLException
	 */
	private Map<String,String> JsongetMap(ResultSet rset) throws SQLException{		
		Map<String,String> map=new HashMap<String,String>();
		while(rset.next()){
			String val=rset.getString(1);
			map.put(val,val);
		}
		return map;
	}
	/**
	 * json数据写pg
	 * 裁判文书
	 * @param count
	 * @return
	 */
	private String JsonSql(int count){
		StringBuffer sb=new StringBuffer("SELECT URL_ID FROM filed_zwh_test WHERE URL_ID IN (");
		int index=0;		
		for(index=0;index<count;index++){
			if(index==0){				
				sb.append("?");
			}
			else{
				sb.append(",").append("?");
			}
		}
		sb.append(")");
//		logger.info("----------查询SQL："+sb.toString());
		return sb.toString();
	}	
	/**
	 * 设置map数据
	 * @param rset
	 * @return
	 * @throws SQLException
	 */
	private Map<String,String> getMap(ResultSet rset) throws SQLException{		
		Map<String,String> map=new HashMap<String,String>();
		while(rset.next()){
			String val=rset.getString(1);
			map.put(val,val);
		}
		return map;
	}
	private String jionSql(int count){
		StringBuffer sb=new StringBuffer("SELECT URL_ID FROM extract_url_t WHERE URL_ID IN (");
		int index=0;		
		for(index=0;index<count;index++){
			if(index==0){				
				sb.append("?");
			}
			else{
				sb.append(",").append("?");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	private void createPartialData(List<String[]> list,PreparedStatement stmt,PreparedStatement stmt2,Connection conn) throws SQLException{
		String urlId=null;
		int i=0;
		int sum=list.size();
		ResultSet rset=null;
		for(i=0;i<sum;i++)
		{						
			//需要指定URL生成UUID的字符串作为参数				
			 urlId = CommonConstant.getUUID(list.get(i)[1]).toString();						 
			 stmt.setString(1,urlId);			
			 rset =stmt.executeQuery();
			 java.sql.Date data=new java.sql.Date(System.currentTimeMillis());			 
			while(rset.next()){
				 int count=rset.getInt(1);
				 if(count==0){					
					 stmt2.setString(1,urlId);	
					 stmt2.setString(2,list.get(i)[1]);	
					 stmt2.setString(3,list.get(i)[0]);
					 stmt2.setShort(4, (byte)1);
					 stmt2.setDate(5,data);
					 stmt2.setLong(6,2l);
					 stmt2.setDate(7,data);
					 stmt2.setLong(8,2l);
					 stmt2.addBatch();
				 }
			 }		
		}
		try {
			stmt2.executeBatch();
			conn.commit();
		} catch (Exception e) {
			logger.equals(e.getMessage());
			conn.rollback();
		}
		finally{
			rset.close();		
			stmt2.clearBatch();
		}		
	}
	/**
	 * 批量写数据
	 * @param list
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public boolean jdbcCreatePostgresql2(List<String[]> list) throws Exception{
		Connection conn=DriverManager.getConnection("jdbc:postgresql://192.168.251:5432/duplicatedb?useServerPrepStmts=false&rewriteBatchedStatements=true", "postgres", "615601.xcy*");			
		PreparedStatement stmt=null;
		PreparedStatement stmt2=null;		
		conn.setAutoCommit(false); 
		stmt=conn.prepareStatement("SELECT COUNT(*) FROM extract_url_t WHERE URL_ID=?");
		stmt2=conn.prepareStatement(" INSERT INTO  extract_url_t(URL_ID,URL,URL_TEXT,URL_STATE,CREATE_DATE,CREATE_USER,LAST_MODIFY_DATE,LAST_MODIFY_USER) values(?,?,?,?,?,?,?,?)");
	try {					
			if(list.size()<=CommonConstant.NUMBER){
				long da=System.currentTimeMillis();
				createPartialData(list,stmt,stmt2,conn);	
				logger.info("第1批数据耗时:"+(System.currentTimeMillis()-da));
				
			}
			else{
				int page=list.size()%CommonConstant.NUMBER;
				if(page==0){
					page=list.size()/CommonConstant.NUMBER;
				}
				else{
					page=list.size()/CommonConstant.NUMBER+1;
				}
				int index=0;
				int last=0;
				for(index=0;index<page;index++){
					last=(index+1)*CommonConstant.NUMBER;
					if(last>=list.size()){
						last=list.size();
					}
					long da=System.currentTimeMillis();
					createPartialData(list.subList(index*CommonConstant.NUMBER, last),stmt,stmt2,conn);
					logger.info("第"+(index+1)+"批数据耗时:"+(System.currentTimeMillis()-da));
				}
			}
         } catch (Exception e) {
        	logger.error(e.getMessage());
			return false;
		}
		finally{
			stmt2.close();
			stmt.close();
			conn.close();
		}	
		return true;
	}
	@Override
	public boolean jdbcCreateJSONPostgresql(Iterable<ArchivesVO> listData) {
		// TODO Auto-generated method stub
		return false;
	}
}
