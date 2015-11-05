package cn.com.szgao.action;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.TableTag;

public class ExtractionText {

	private static Logger logger = LogManager.getLogger(ExtractionText.class.getName());
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String value= getText("","file:///E:/Company_File/%E8%A6%81%E5%A4%84%E7%90%86%E7%9A%84%E6%95%B0%E6%8D%AE/cpwsw/html%E6%96%87%E4%BB%B6/ms/000da2be-def4-536e-a9a3-3bab136af682.html");
		System.out.println(value);
		String[] values=getSentences(value);
		List<String> defendant=null;		
		Map<String,List<String>> map=new HashMap<String,List<String>>();
		for(String val:ReadTxt.KEYWORDKE){			
			 defendant=getPlaintiffText(values,val);
			if(null!=defendant&&defendant.size()>0){
				map.put(val, defendant);
			}
		}
		for(Map.Entry<String, List<String>> ma:ExtractionText.getExtractName(value).entrySet())
		        System.out.println(ma.getKey()+"==="+ma.getValue());	
	}
	public static String getRepacle(String value,String key){
		boolean reuslt=false;
		boolean reuslt2=false;
		StringBuffer keyRepacle=new StringBuffer("被");
		for(String keys:ReadTxt.REPACLE){
			if(keys.equals(key)){
				reuslt=true;
				keyRepacle.append(keys);
				break;
			}
		}
		StringBuffer keyRepacle2=new StringBuffer();
		String repKey=null;
		for(String keys:ReadTxt.REPACLE2){
			if(keys.equals(key)){
				reuslt2=true;
				keyRepacle2.append(keys).append("人");
				repKey=keys;
				break;
			}
		}
		String repalce=null;
        if(reuslt){
        	repalce=value.replace(keyRepacle.toString(),"");
        }
        if(reuslt2){
        	repalce=value.replace(keyRepacle2.toString(),repKey);
        }
		return repalce==null?value:repalce;
	}
	public static List<String> getResult(Map<String,List<String>> map,List<String> values,String key){
		if(null==map||map.size()==0)
			return  values;
		List<String> values2=null;
		int size1=0;
		int size2=0;
		List<String> list=null;
		String value1=null;
		String value2=null;
		for(Map.Entry<String,List<String>> ma:map.entrySet()){
			 if(ma.getKey().indexOf(key)>=0){
				 values2=ma.getValue();				
				 size1=values2.size();
				 size2=values.size();				
				 for(int index=0;index<size1;index++){
					 for(int index2=0;index2<size2;index2++){
						 value1=values2.get(index);
						 value2=values.get(index2);
						 if(null==list)
							 list=new ArrayList<String>();
						 if(value1.equals(value2)){
							 list.add(value2);
						 }
					 }					
				 }
			 }
		}
		if(null!=list&&list.size()>0)
		values.removeAll(list);
		return values;
	}
	//提取网页数据
	public static String getText(String path,String values){
		Parser parser;
		StringBuffer sb=null;
		Node [] tables=null;
		String value=null;
	try {
		parser = new Parser();
		parser.setURL(path+values);
		parser.setEncoding("utf-8");
		tables = parser.extractAllNodesThatAre(TableTag.class); 
		sb=null;
		for (int i = 1; i < tables.length; i++){
			if(sb==null)
				sb=new StringBuffer();
			TableTag tableTag = (TableTag)tables[i]; 			
			value=tableTag.toPlainTextString();			
			sb.append(getReplaceAll(value));			
		  }
		  if(null==sb||"".equals(sb.toString())){
			  value=URLText.getStringBeanText(path+values);
			  if(null!=value){
				  sb=new StringBuffer();
				  sb.append(getReplaceAll(value));
			  }
		  }		
		}
		catch (Exception e) {
			value=URLText.getHtml(values);
			if(value!=null||!"".equals(value)){
				sb=new StringBuffer();
				sb.append(getReplaceAll(value));
			}else{
				value=URLText.getStringBeanText(path+values);
				if(null!=value&&!"".equals(value)){
					sb=new StringBuffer();
					sb.append(getReplaceAll(value));
				}
			}
		}
		finally{
			tables=null;
			parser=null;			
		}
		return sb==null?null:sb.toString();		     
	}
	 //根据。,，,；分段
	 public static String[] getSentences(String input) {
	        if (input == null) {
	            return null;
	        } else {	            
	            return input.split("[。,，,；]");
	        }
	 
	 }
	/*//根据。,，,；分段
	 public static String[] getSentences2(String input) {
	        if (input == null) {
	            return null;
	        } else {	            
	            return input.split("[。,.]");
	        }
	 
	 }*/
	 //根据关键字分段
	 public static List<String> getPlaintiffText(String[] values,String exData){		 
		 List<String> lists=new ArrayList<String>();
		 String value=null;
		 for(String val:values){			 
			 value=getRepacle(val,exData);		
			 if(value.indexOf(exData)>=0){	
				 if(getReuslt(lists,value,exData))
				 lists.add(value.substring(value.indexOf(exData),value.length()));
			 }
		 }
		 List<String> sets= getListSorting2(lists,exData);
		 return sets;
	 } 
	 public static boolean getReuslt(List<String> list,String value,String exData){
		 if(null==list||list.size()==0)
			 return true;		 
		 for(String val:list){
			 if(val.equals(value.substring(value.indexOf(exData),value.length()))){
				return false;
			 }
		 }		 
		 return true;
	 }
	 public static Set<String>  getListSorting(List<String> list,String exData){
		 Set<String> listExtract=new HashSet<String>();
		 int size=list.size();
		 int sum=0;
		 SimilarityText sim=new SimilarityText();
		 for(int index=0;index<size;index++){
			 sum=0;
			 for(int index2=index+1;index2<size;index2++){
				 String val=list.get(index).replaceAll(exData, "");			
				 String val2=list.get(index2).replaceAll(exData, "");		
				 if(val2.indexOf(val)>=0){
					 sum++;
				 }		
				 if(sim.getSimilarityRatio(val, val2)>=0.3){
					 sum++;
				 }
				 
			 }
			 if(sum>1){
				 String value=list.get(index);
				 if(value.indexOf("公司")>=0){
					 value=value.substring(0,value.indexOf("公司")+2);
				 }
				 listExtract.add(value);
			 }
		 }				
		 return listExtract;
	 }
	 //提取数据方法
	 public static List<String>  getListSorting2(List<String> list,String exData){
		 if(null==list||list.size()==0)
			 return null;
		 List<String> listExtract=new ArrayList<String>();
		 Map<String,Integer> map=new HashMap<String,Integer>();
		 int size=list.size();
		 List<String> listName=null;
		 List<String> listComps=null;
		 String result=null;
		 String[] valueSiplt=null;
		 String repacle=null;
		 for(int index=0;index<size;index++){			 
			 listComps=TextName.getSpiltComPanys(getReplaces(list.get(index)));				 
			 result=getCompanys(listComps);
			 if(null==result){
				  repacle=getReplaces(list.get(index));
				 if(repacle.length()<=1)
					 continue;
				 if(repacle.length()<=3){
					 listName=new ArrayList<>();
					 listName.add(repacle);
					 result=getSurName(listName);
					 if(null==result||"".equals(repacle)){continue;}
					 Integer count=map.get(repacle);
					 if(null==count){
						 listExtract.add(repacle); 
						 map.put(repacle, 1);
					 }
					 continue;	 
				 }					 
				 listName=TextName.getSpiltNames(repacle);				
				 result=getSurName(listName);
				 if(null!=result){
					 String value=getReplaces(result);
					 if(value.indexOf("、")==-1){						
						 if(value.length()<=1||value.length()>4)
							 continue;
						 Integer count=map.get(result);
						 if(null==count){
							 listExtract.add(value.replace(exData,"")); 
							 map.put(result, 1);
						 }		
					 }
					 else{
						 valueSiplt=value.split("、");
						 for(String val:valueSiplt){
							 if("".equals(val))
								 continue;
							 if(val.length()<=1||val.length()>4)
								 continue;
							 Integer count=map.get(val);
							 if(null==count){
								 listExtract.add(val.replace(exData,"")); 
								 map.put(val, 1);
							 }		
						 }
					 }
					 			
				 }
					 
			 }
			 else{
				 valueSiplt=result.split("、");
				 for(String val:valueSiplt){
					 if("".equals(val))
						 continue;					
					 Integer count=map.get(val);
					 if(null==count){
						 listExtract.add(val.replace(exData,"")); 
						 map.put(val, 1);
					 }		
				 }				 				 
			 }
			    
		 }				
		 return listExtract;
	 }
	 //接接多个姓名
	 public static String getSurName(List<String> list){
		 if(null==list||list.size()==0)
			 return null;
		 String[] listSurName=ReadTxt.getDataSurNames();
		 int index=0;
		 int size=0;
		 StringBuffer sb=null;
		 String surname =null;
		 for(String val:list){			
			 for(String val2:listSurName){				 
				 index=val.indexOf(val2);
				 if(index==-1)
					 continue;
				 surname=val.substring(0,val2.length());
				 if(surname.equals(val2)){
					 if(sb==null)
						 sb=new StringBuffer(val);
					 else
						 sb.append("、") .append(val);
					 break;
				 }		
			 }			
		 }
		 return sb==null?null:sb.toString();
	 }
	 //接接多个公司
	 public static String getCompanys(List<String> list){
		 if(null==list||list.size()==0)
			 return null;
		 StringBuffer sb=null;
		 String value=null;
		 for(String val:list){
			 for(String val2:ReadTxt.getDataCompanys()){				
				 if(val.indexOf(val2)>=0){
					 value=val.substring(0,val.lastIndexOf(val2)+val2.length());					
					 //if(!value.equals(val2)){break;}*/
					 if(null==sb)
						 sb=new StringBuffer(value);
					 else{
						if(sb.toString().indexOf(value)==-1){
							sb.append("、").append(value);
						} 
					 }
						
				 }
			 }
		 }
		 return sb==null?null:sb.toString();
	 }
	 //过虑关键字
	 public static String getReplaces(String value){
		 String replace=value;
		 for(String val:ReadTxt.KEYWORDKE){
			 replace=replace.replace(val,"");
		 }
		 return replace;
	 }
	 //拼接人
	 public static String getPersonName(Map<String,List<String>> map,int status) {
		 if(null==map||map.size()==0)
			 return null;
		 StringBuffer sb=null;		
		 String[] keys=null;
		 List<String> list=null;
		 Map<String,String> maps=new LinkedHashMap<String,String>();
		 if(status==1)
			 keys=ReadTxt.PLAINTIFF;
		 else
			 keys=ReadTxt.DEFENDANT;
		 for(String key:keys){
			 list=map.get(key);
			 if(null!=list&&list.size()>0){
				 for(String val:list){
					 maps.put(val, val);
				 }
			 }
		 }
		 for(String value:maps.keySet()){			 
			 if(null==sb)
				 sb=new StringBuffer().append(value);
			     
			 else
			     sb.append("、").append(value);
		 }
		 return sb==null?null:sb.toString();
	 }
	 //提取人
	 public static Map<String,List<String>> getExtractName(String value){
		 if(null==value||"".equals(value))
			 return null;
		    Map<String,List<String>> map=new LinkedHashMap<String,List<String>>();
		    String[] values=getSentences(value);
			List<String> defendant=null;		
			for(String val:ReadTxt.KEYWORDKE){			
				 defendant=getPlaintiffText(values,val);
				if(null!=defendant&&defendant.size()>0){
					map.put(val, defendant);
				}
			}			
			List<String> theFirst=null;
			List<String> nexTime=null;
			List<String> list=null;
			String key=null;
			for(String val:ReadTxt.KEYHEAVY){
				theFirst=map.get(val);
				if(null==theFirst||theFirst.size()==0){
					continue;
				}
				key=val.substring(1,val.length());
				nexTime=map.get(key);
				if(null==nexTime||nexTime.size()==0){
					continue;
				}
				list=getTocompare(theFirst,nexTime);
				if(null!=list&&list.size()>0){
					map.put(key,list);
				}
			}			
		 return map;
	 } 
	 //比较两个集合数据
	 public static List<String> getTocompare(List<String> theFirst,List<String> nexTime){
		 List<String> list=new ArrayList<>();
		 for(String val:theFirst){
			 for(String val2:nexTime){
				 if(val.equals(val2)){
					 list.add(val);
					 break;
				 }
			 }
		 }
		 if(list.size()==0){
			 return null;
		 }
		 for(String val:list){
			 nexTime.remove(val);
		 }
		 return nexTime;
	 } 
	 public static String getReplaceAll(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			    sb=new StringBuffer();
			    value=value.replaceAll("[\n,\t,\r,&nbsp; ,：,：,“,”, ]","");
				value=value.replaceAll("[×,X,x,╳,＊,\\*]","某");
				value=value.replaceAll("\\<(.*)\\>","");
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
	 }
	
}
