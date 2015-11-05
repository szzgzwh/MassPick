package cn.com.szgao.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.dto.RecordData;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;

public class WodText {
	private static Logger logger = LogManager.getLogger(WodText.class.getName());
	static ApplicationContext application=new ClassPathXmlApplicationContext("classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory=(SessionFactory)application.getBean("sessionFactory");	
	static Map<String,List<RecordData>> MAPS=new HashMap<String,List<RecordData>>();
	public static String[] PLAINTIFF={"第三人","诉讼代理人","辩护人","上诉人","申诉人","申请执行人","申请人","执行人","原审被告","赔偿请求人","原公诉机关","公诉机关","执行机构","原告","复议机关","申请复议人","一审原告","委托代理人","法定代表人","起诉人","移送执行机构","二审上诉人","原审第三人","负责人","抗诉机关","申请再审人","委托代理","四被上诉人委托代理人","两上诉人的委托代理人"};
	public static String[] DEFENDANT={"被上诉人","被执行人","被申诉人","被申请人","被申请执行人","原审被告人","原审原告","罪犯","被告","赔偿义务机关","一审被告","二审被上诉人"};
	public static String[] BOOKCLASS = { "民事调解书", "民事裁定书", "民事判决书", "民事决定书", "刑事判决书","刑事裁定书","刑事决定书", "行政判决书","行政决定书", "行政裁定书", "执行裁定书", "执行判决书", "执行决定书","国家赔偿裁定书","国家赔偿判决书","国家赔偿决定书", "驳回申诉通知书", "决定书" , "通知书"};//文书类型
	public static String[] CAUSE={"驳回申诉通知","赔偿决定书","争议一案","纠纷一案","通告一案","违法一案","执行一案","赔偿一案","劫罪一案","确认一案","危险驾驶","诈骗","盗窃","死亡","强奸","聚众斗殴","寻衅滋事","贩卖毒品","运输毒品","故意伤害","涉嫌诽谤","抢劫","绑架","勒索","杀人","纠纷","非法拘禁","运输毒品","破坏电力设备","一案","违法","非法","犯罪"};
	public static String[] CAUSE1={"指控原审","案由：","案由:","公诉机关指控：","受理了",")原告","）原告","。原告","指控被告人","本院受理","。本院在","。上诉人"};
	public static String[] CAUSE2={"本案现已审理终结。","向本院提起公诉。","一案","请本院判处。","关于确认调解协议的申请","犯交通肇事罪","提请本院惩处。"};
	public static String[] APPROVAL1={"如下协议：","如下协议:","判决如下：","判决如下:","裁定如下：","裁定如下:"};
	public static String[] APPROVAL2={"。上述协议","如不服本判决，","本裁定自即日起发","审判长","审判员"};
	public static String[] DATESTATUS={"提交时间","提交日期","发布时间","发布日期","编辑时间","编辑日期","发表时间","录入时间","更新时间","点击率","点击数","发表于","阅读","点击","日期","作者","时间","今天是","小"};
	public static String[] CAUSENUM = {"(","（","〔"};
	static long count = 0;
	static long ERRORSUM=0;
	static long INPUTSUM=0;
	static long REPEATSUM=0;
	/**
	 * 裁判文书
	 * 更新couchbase
	 * 抓取word文档修改court
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File filepor=new File("E:\\Company_File\\log4j-0814\\Java1\\batchImport.log");   
		if(filepor.exists()){
			filepor.delete();
		}
		filepor=null;

		long da=System.currentTimeMillis();
		
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");    
		File file=new File("F:\\DataSheet\\DetailedPage\\DistrictCourtWord\\杭州市建德市人民法院\\00c885dc-0bbb-58a0-b019-44e53c7c8dae.doc");
		try {
			getDocData(file);	
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally{
			file = null;
		}
		logger.info(count + ":数量");
		logger.info("所有文件总耗时"+(((System.currentTimeMillis()-da)/1000)/60)+"分钟");
	}
	
	/**
	 * 遍历wod文件数据
	 */
	public static void getDocData(File file) throws Exception {
		ICollectDataService serivcesApi=new CollectDataService();
		ArchivesVO arch= null;		
		List<ArchivesVO> listarchs=null;
		String fileName=null;
		boolean result=false;
		InputStream is=null;
		WordExtractor word=null;
		listarchs = new ArrayList<ArchivesVO>();
				if(file.isFile()){	
				arch = new ArchivesVO();
				count++;
				is = new FileInputStream(file);			
				word=new WordExtractor(is);		//得到word文档的信息
				String wod=word.getText();
					wod=wod.replaceAll("\n"," ");
					wod=wod.replaceAll("\r"," ");
					logger.info("文件路径："+file.getPath());
					fileName=file.getName();
					Map<String,List<String>>list =new HashMap<String, List<String>>();
					String value=WodText.getReplaceAll(wod);
					logger.info("去掉特殊字符："+value);
					list=ExtractthepeopleText.getPersonName(value);
					arch.setPlaintiff(WodText.getKeyName(list,1));//原告
					arch.setDefendant(WodText.getKeyName(list,2));//被告
					arch.setCourtName(getReplaceAll(WodText.getCourtName(value)));//提取法院
					arch.setCaseCause(getReplaceAll(WodText.CaseCause(value)));//提取案由
					arch.setApprovalDate(WodText.getConcludeDate(value));//审结日期
					arch.setApproval(getReplaceAll(WodText.getApproval(value)));//判决结果
					arch.setSuitType(WodText.getSuitType(value));//文书类型
					arch.setCaseNum(WodText.getCaseNum(value));//案号	
					arch.setTitle(getReplaceAll(WodText.getTitle(value)));//标题   
					arch.setUuid(fileName.substring(0,fileName.lastIndexOf(".")));
					showLog(arch);
					listarchs=new ArrayList<ArchivesVO>();
					listarchs.add(arch);
				    result=serivcesApi.updateJsonData(listarchs);//写入CB库
					if(!result){
						 logger.info(file.getPath()+":更新失败");
					}
					count += listarchs.size();
					listarchs = null;
					return;
				}
		File[] files=file.listFiles();
		for(File fi:files){
			if(fi.isFile()){
				arch = new ArchivesVO();
					is = new FileInputStream(fi);			
					word=new WordExtractor(is);		//得到word文档的信息
					String wod=word.getText();
						wod=wod.replaceAll("\n"," ");
						wod=wod.replaceAll("\r"," ");
						logger.info("文件路径："+fi.getPath());
						fileName=fi.getName();
						Map<String,List<String>>list =new HashMap<String, List<String>>();
						String value=WodText.getReplaceAll(wod);
//						logger.info("去掉特殊字符："+value);
						list=ExtractthepeopleText.getPersonName(value);
						arch.setPlaintiff(WodText.getKeyName(list,1));//原告
						arch.setDefendant(WodText.getKeyName(list,2));//被告
						arch.setCourtName(getReplaceAll(WodText.getCourtName(value)));//提取法院
						arch.setCaseCause(getReplaceAll(WodText.CaseCause(value)));//提取案由
						arch.setApprovalDate(WodText.getConcludeDate(value));//审结日期
						arch.setApproval(getReplaceAll(WodText.getApproval(value)));//判决结果
						arch.setSuitType(WodText.getSuitType(value));//文书类型
						arch.setCaseNum(WodText.getCaseNum(value));//案号	
						arch.setTitle(getReplaceAll(WodText.getTitle(value)));//标题  
						arch.setUuid(fileName.substring(0,fileName.lastIndexOf(".")));
						showLog(arch);
						listarchs.add(arch);
					if(listarchs.size()>=2000){
						result = serivcesApi.updateJsonData(listarchs);//写入CB库
						if(!result){
							logger.info(fi.getPath()+":更新失败");
						}		
						count += listarchs.size();
						listarchs=null;	
						listarchs=new ArrayList<ArchivesVO>();
					}
			} else if (fi.isDirectory()) {
				//这一步骤是为了遍历文件夹中有多个文件夹
				logger.info(fi.getName());
				getDocData(fi);
			} else {
				continue;
			}
		}
		if(null!=listarchs&&listarchs.size()>0){
			result=serivcesApi.updateJsonData(listarchs);
			if(!result){
				logger.info("更新失败");
			}		
			count += listarchs.size();
			listarchs=null;	
		}
	}
	//
	/**
	 * 获取wod文件内容
	 * 已放弃
	 */
	public static String getContent(File file){
		InputStream is=null;
		WordExtractor word=null;
		try {
			is = new FileInputStream(file);			
			//得到word文档的信息
			word=new WordExtractor(is);		
			String wod=word.getText();
			if(null==wod||"".equals(wod)){return null;}
			wod=wod.replaceAll("\\n"," ");
			wod=wod.replaceAll("\\r"," ");
			return wod;
		} catch (Exception e) {			
			e.printStackTrace();
			logger.error("读取"+file.getPath()+"异常:"+e.getMessage());
		} 
		finally{			
			if(null!=word){try {
				word.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}}	
			if(is!=null){try {
				is.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}}
		}	
		return null;
	} 
	
	/**
	 * 裁判文书，word文档提取数据
	 * 已放弃
	 * @param doc
	 * @return
	 */
	public static ArchivesVO  getextract(String doc){
		if(null==doc||"".equals(doc)){return null;}
		ArchivesVO arch=new ArchivesVO();
		Map<String,List<String>>list =new HashMap<String, List<String>>();
		String value=WodText.getReplaceAll(doc);
		logger.info("去掉特殊字符："+value);
		list=ExtractthepeopleText.getPersonName(value);
		arch.setPlaintiff(WodText.getKeyName(list,1));//原告
		arch.setDefendant(WodText.getKeyName(list,2));//被告
		arch.setCourtName(getReplaceAll(WodText.getCourtName(value)));//提取法院
		arch.setCaseCause(getReplaceAll(WodText.CaseCause(value)));//提取案由
		arch.setApprovalDate(WodText.getConcludeDate(value));//审结日期
		arch.setApproval(getReplaceAll(WodText.getApproval(value)));//判决结果
		arch.setSuitType(WodText.getSuitType(value));//文书类型
		arch.setCaseNum(WodText.getCaseNum(value));//案号	
		arch.setTitle(getReplaceAll(WodText.getTitle(value)));//标题
	   return arch;	
	}
	//打印信息
    public static void showLog(ArchivesVO arch){
    	if(null==arch){return;}
    	logger.info("<<------------------------------------------------------>>");
		logger.info("原告相关人:"+arch.getPlaintiff());
		logger.info("被告相关人:"+arch.getDefendant());
		logger.info("法院:"+arch.getCourtName());
		logger.info("案由:"+arch.getCaseCause());
		logger.info("审结日期:"+arch.getApprovalDate());
		logger.info("判决结果:"+arch.getApproval());
		logger.info("文书类型:"+arch.getSuitType());
		logger.info("案号:"+arch.getCaseNum());
		logger.info("标题:"+arch.getTitle());
		logger.info("UUID:"+arch.getUuid());
		logger.info("<<------------------------------------------------------>>");
    }
    //获取案号
    public static String getCaseNum(String value){
    	int secondIndex = value.indexOf("号");
   	 try{
   		 for(String val : CAUSENUM){
   			int firstIndex= value.indexOf(val);
   			if(firstIndex == -1){continue;}
   		if(firstIndex < secondIndex){
   			System.out.println("firstIndex"+firstIndex+"-----secondIndex:"+secondIndex);
   		value = value.substring(firstIndex, secondIndex+1);
   		return value;
   		}
   		 }
   	} catch (Exception e) {
   		logger.error(e.getMessage());
   	}
   	return null;
   	}
  //提取行书类型
  	public static String getSuitType(String value){	
  			int index;
  			for(String val : BOOKCLASS){
  				index = value.indexOf(val);
  				if(index >= 0){
  					return val;
  				}
  			}
  		return null;
  	}
    //提取标题
    public static String getTitle(String value){		
		try {
			int index=value.indexOf("书");
			if(index==-1){return null;}
			return value.substring(0,index+1);
		} catch (Exception e) {
			logger.error(e.getMessage()+":获取标题失败");
		}
		return null;
	}
  //提取法院名称
  		public static String getCourtName(String value){
  			try{
  			    int idnex=value.indexOf("院");
  			    if(idnex==-1){return null;}
  				return value.substring(0,idnex+1);
  			}
  			catch(Exception e){
  				logger.error("提取法院名称出错:"+e.getMessage());
  			}			
  			return null;
  		} 
  	//提取案由
  		public static String CaseCause(String value){
  			try {
  				int index=0;
  				String firTxt=null;
  				String lastxt=null;
  				int count=0;
  				for(String val:WodText.CAUSE){
  					 index=value.indexOf(val);
  					if(index>=0){
  					   if(val.equals("驳回申诉通知")||val.equals("赔偿决定书")){
  						   int index2=getDateIndex(value);
  							if(index==-1){
  								return null;
  							}
  						   firTxt=value.substring(index2,value.length());
  						   firTxt=firTxt.substring(firTxt.indexOf("号")+1,firTxt.length());
  						   value=firTxt;
  						   if(val.equals("赔偿决定书"))
  						      val="国家赔偿";
  					   }
  					   index=value.indexOf(val);
  					   if(index<0)
  						   index=value.indexOf("国家赔偿");
  					   firTxt=value.substring(0,index);
  					   count=firTxt.lastIndexOf("。");
  					   if(count==-1){
  						   firTxt.lastIndexOf("号");
  					   }
  					   firTxt=firTxt.substring(count+1,firTxt.length());
  					   index=value.indexOf(val);
  					   if(index<0){
  						   if(val.equals("驳回申诉通知"))
  							   val="国家赔偿";
  					   }					   
  					   lastxt=value.substring(value.indexOf(val),value.length());
  					   lastxt=lastxt.substring(lastxt.indexOf(val),lastxt.indexOf("。")+1);				   
  					   return new StringBuffer(firTxt).append(lastxt).toString();
  					}
  				}
  				
  			} catch (Exception e) {
  				logger.error("提取案由出错:"+e.getMessage());
  			}
  			return null;
  		}  
  		/*public static String CaseCause(String value){
  			int firstIndex ;
  			int secondIndex;
  			for(String val : CAUSE1){
  				 firstIndex = value.indexOf(val);
  				 if(firstIndex >=0){
  					 for(String val2:CAUSE2){
  						secondIndex=value.indexOf(val2);
//  						if(secondIndex >= 0){
  						if(secondIndex == -1){continue;}
  							if(secondIndex>firstIndex){
  								System.out.println(val+"-----案由1----:"+firstIndex);
  							 System.out.println(val2+"----案由2-----:"+secondIndex);
  							value=value.substring(firstIndex+val.length(), secondIndex+val2.length());
  							System.out.println(value);
  							return value;
  						}
  					 }
  				 }
  			}
  			return null;
  		}*/
  	//取第一段
  		public static int getDateIndex(String value){
  			int index=0;
  			value=value.substring(0,value.indexOf("。"));		
  			for(String date:DATESTATUS){
  				index=value.indexOf(date);
  				if(index>=0){
  					 return index+date.length();
  				}
  			}
  			return -1;
  		}
  	//取第一段
  		public static int getDateIndex2(String value){
  			int index=0;
  			//value=value.substring(0,value.indexOf("。"));		
  			for(String date:DATESTATUS){
  				index=value.indexOf(date);
  				if(index>=0){
  					 return index+date.length();
  				}
  			}
  			return -1;
  		}
  		//判决结果
  		public static String getApproval(String value){
  			int firstIndex;
  			int secondIndex;
  			for(String val : APPROVAL1){
  				firstIndex = value.indexOf(val);
  				if(firstIndex >= 0){
  					for(String val2 : APPROVAL2){
  						secondIndex = value.lastIndexOf(val2);
  						if(secondIndex>firstIndex){
  							value= value.substring(firstIndex+val.length(), secondIndex);
  							return value;
  						}
  					}
  				}
  			}
  			return null;
  		}
  	//取审结日期
  		public static String getConcludeDate(String date){
  			String[] data={"二〇","一九","二○","二０","二0","二O","二0","二Ｏ","二�","20","19"};		
  	    	int[] splt={9,10,11,12};
  	    	String value;
  	    	String[] datas;
  	    	boolean result=false;
  	    	try{
  	    	for(int index=0;index<data.length;index++){           		
  	    		if(date.lastIndexOf(data[index])<0){
  	    			continue;
  	    		}
  	    		value=date.substring(date.lastIndexOf(data[index]));
  	    		for(int index2=0;index2<splt.length;index2++){
  	            	datas=value.split("");
  	            	String da=datas[splt[index2]];
  	            	if("日".equals(da)){
  	            		value=value.substring(0,splt[index2]);           	                 	
  	            		result=true;;
  	            		break;
  	            	}
  	    		}
  	    		if(result)
  	    			return value==null?null:value.replaceAll("�","0");
  	    	  }               	
  	    	}
  	    	catch(Exception  e){
  	    		  logger.error("取审结日期出错:"+e.getMessage());
  	    	} 
  	    	finally{
  	    		datas=null;
  	    		splt=null;
  	    		data=null;
  	    	}
  			return null;
  		} 
  		 //根据关键字提取人
	    public static String getKeyName(Map<String,List<String>> map,int status){
	    	if(map==null){return null;}
	    	String[] keys=null;
	    	 if(status==1)
				 keys=WodText.PLAINTIFF;
			 else
				 keys=WodText.DEFENDANT;
	    	 Set<String> setNames=null;
	    	 List<String> list=null;
	    	 String[] vals=null;
	    	 for(String key:keys){
	    		 list=map.get(key);
	    		 if(null!=list){
	    			 for(String val:list){
	    				 if(null==setNames){setNames=new HashSet<String>();}
	    				 if(val.indexOf("、")>=0){
	    					 vals=val.split("、");
	    					 for(String va:vals){
	    						 setNames.add(va);	 
	    					 }
	    				 }
	    				 else
	    				 setNames.add(val);
	    			 }
	    		 }
	    	 }
	    	 if(setNames==null||setNames.size()==0){return null;}
	    	 StringBuffer sb=null;
	    	 for(String val:setNames){
	    		 if(sb==null)
	    			 sb=new StringBuffer(val);
	    		 else
	    			 sb.append("、").append(val);
	    	 }
	    	 return sb==null?null:sb.toString();
	    }
  //去掉无用字符
    public static String getReplaceAll(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
//			    sb=new StringBuffer();
			    value=value.replaceAll(",","，");
			    value=value.replaceAll("�","O");
			    value=value.replaceAll("[×,X,Ｘ,x,╳,＊,\\*]","某");
			    value=value.replaceAll("[\n,\t,\r,\\s,&nbsp; ,“,”,・ ,<,/>,</,>,a-z,A-Z,-,+,=,},{,.,#,\",',-,%,^,*]","");
				value=getSpecialStringALL(value);
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
	 }
    //去掉特殊字符
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
}
