package cn.com.szgao.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

public class ExtractthepeopleText {
	private static Logger logger = LogManager.getLogger(ExtractthepeopleText.class.getName());
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String thml=URLText.getText("","E:/Company_File/要处理的数据/江西省/赣州市中级人民法院/6f2bc773-54b3-56e9-bb39-54f238c5ffe4.html");
		//thml=getHtml("file:///E:/Company_File/%E8%A6%81%E5%A4%84%E7%90%86%E7%9A%84%E6%95%B0%E6%8D%AE/%E5%B9%BF%E8%A5%BF%E7%9C%81/%E4%B8%87%E7%A7%80%E5%8C%BA%E4%BA%BA%E6%B0%91%E6%B3%95%E9%99%A2/0a762834-9656-5ec5-b430-5025c8ef5c8c.html");
		thml=getReplaceAll(thml);
		System.out.println(thml);
		List<String> defendant=null;	
		String[] values=getSentences2(thml);
		Map<String,List<String>> map=new LinkedHashMap<String,List<String>>();
		String[] values2=null;
		String value=null;
		for(String val:ReadTxt.KEYWORDKE){
			 value=getListTakelong(values,val);
			 values2=getSentences(value);
			 if(null==values2){continue;}
			 defendant=getPlaintiffText(values2,val);
			if(null!=defendant&&defendant.size()>0){
				map.put(val, defendant);
			}
		}
		System.out.println("原告："+ExtractthepeopleText.getKeyName(map,1));
		System.out.println("被告:"+ExtractthepeopleText.getKeyName(map,2));
		/*for(Map.Entry<String,List<String>> ma:map.entrySet()){
			System.out.println(ma.getKey()+":"+ma.getValue());
		}*/
		
	}	
	 //根据。分段
	 public static String[] getSentences2(String input) {
	        if (input == null) {
	            return null;
	        } else {	        	
	        	String[] inputs=input.split("[。]");
	        	String[] inputs2=new String[inputs.length];
	        	int index=0;
	        	for(String val:inputs){
	        		if(null!=val&&!"".equals(val)){
	        			inputs2[index++]=val;
	        		}
	        	}
	        	inputs=null;
	            return inputs2;
	        }
	 
	 }
	 //匹配关键字取段
     public static String getListTakelong(String[] valueSipts,String key){
    	 if(null==valueSipts){return null;}
    	 String keys=null;
    	 StringBuffer sb=null;
    	 try {
    		 int index=0;
        	 int count=0;
        	 for(String val:valueSipts){
        		 if(null==val||"".equals(val)){continue;}
        		 //val=getReplaceAll(val);  
        		 val=getSpecialStringALL(val);
        		 val=getRpacleRllKey(val,key);
        		 if((val.length()-key.length())>=2){
        			 if(index==0){
        				index=val.lastIndexOf(key);
        				if(index!=-1){
        					if(sb==null){sb=new StringBuffer();}
        					keys=val.substring(index, val.length());
        					index=2;
        					sb.append(keys);
        					continue;
        				}
        				else{
        					index=0;
        					++count;
        					if(count>=2){index=1;}
        					continue;
        				}    					
        			 }
        			 else
        			 keys=val.substring(0,key.length());   			
        			 if(key.equals(keys)){  				 
        				 if(sb==null)
        					 sb=new StringBuffer(val);
        				 else
        					 sb.append("。").append(val);
        			 }
        		 }
        	 }
        	 return sb==null?null:sb.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
    	 return null;
     }
     //根据关键字分段
   	 public static List<String> getPlaintiffText(String[] values,String key){		 
   		 List<String> list=null;
   		 String keys=null;
   		 try {
   			for(String val:values){			 
   	   			if((val.length()-key.length())>=2){
   	   			 keys=val.substring(0,key.length());
   	   			 if(key.equals(keys)){  				 
   	   				 if(list==null){list=new ArrayList<String>();}
   	   				 list.add(val);	 
   	   			 }
   	   		   }
   	   		 }
   	   		 if(list==null||list.size()==0){return null;}
   	   		 List<String> compNamelsit=null;
   	   		 List<String> resultList=null;
   	   		 String reuslt=null;
   	   		 for(String val:list){  			 
   	   			resultList=getSpiltComPanys(getParenthesesAll(val));
   	   			if(null!=resultList&&resultList.size()>0){
   	   			    if((reuslt=getCompanys(resultList))==null){
   	   			    	resultList=getSpiltNames(getParenthesesAll(val));
   	   	   				if(null!=resultList&&resultList.size()>0){
   	   	   					 if((reuslt=getSurName(resultList))==null){continue;};
   	   	   					   if(compNamelsit==null){compNamelsit=new ArrayList<String>();}  					  
   	   	   					   if(restultList(compNamelsit,reuslt)){compNamelsit.add(reuslt);}
   	   	   					   else
   	   	   	   			        compNamelsit.add(reuslt);
   	   	   	   			}
   	   			    }
   	   				if(compNamelsit==null){compNamelsit=new ArrayList<String>();} 
   	   				if(compNamelsit.size()>0){
   	   					if(restultList(compNamelsit,reuslt)){compNamelsit.add(reuslt);}
   	   				}
   	   				else
   	   				compNamelsit.add(reuslt);
   	   			}  			
   	   		 }  			 
   	   		 return compNamelsit;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
   		 return null;
   	 } 
   //拆分人名
 	public static List<String> getSpiltNames(String value){	
 		if(null==value||"".equals(value)){return null;}
 		List<String> list=null;
 		try {
 			int index=value.indexOf("、");
 	         if(index==-1){
 	         	list=new ArrayList<>();
 	         	list.add(value);
 	         	return list;
 	         }
 	         else{
 	        	 String[] values=value.split("、");
 	        	 for(String val:values){
 	        		 if(null!=val&&!"".equals(val)){
 	        			 if(null==list){list=new ArrayList<String>();}
 	        			 list.add(val);
 	        		 }
 	        	 }
 	         }
 	 		return list;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
         return null;
 	}
    //拆分公司
 	public static List<String> getSpiltComPanys(String value){
 	 try {
	 		if(null==value||"".equals(value)){return null;}
	 		List<String> list=null;
	         int index=value.indexOf("、");
	         if(index==-1){
	         	list=new ArrayList<>();
	         	list.add(value);
	         	return list;
	         }
	         else{
	        	 String[] values=value.split("、");
	        	 for(String val:values){
	        		 if(null!=val&&!"".equals(val)){
	        			 if(null==list){list=new ArrayList<String>();}
	        			 list.add(val);
	        		 }
	        	 }
         }
 			return list;
 		} catch (Exception e) {
 			logger.error("拆分公司出错:"+e.getMessage());
 		}
 		 return null;
 		
 	}
	 //过虑关键字
	 public static String getParenthesesAll(String value){
		 String replace=value;
		 for(String val:ReadTxt.KEYWORDKE){
			 replace=replace.replace(val,"");
		 }
		/* replace=replace.replaceAll("\\（(.*)\\）","");
		 replace=replace.replaceAll("\\((.*)\\)","");*/
		 replace=replace.replaceAll("[\\(,\\),）,（]","");//去掉单括号
		 return replace;
	 }
	 
     //接接多个公司
	 public static String getCompanys(List<String> list){
		 if(null==list||list.size()==0)
			 return null;
		 try {
			 StringBuffer sb=null;
			 String value=null;
			 List<String> valKey=null;
			 for(String val:list){
				 for(String val2:ReadTxt.getDataCompanys()){				
					 if((val.length()-val2.length())>=2){
						 value=val.substring((val.length()-val2.length()),val.length());
						 if(val2.equals(value)){
							 if(val.length()-val2.length()>20){break;}
							 if(null==valKey){valKey=new ArrayList<String>();}
							 if(valKey.size()>0){
								 if(restultList(valKey,val)){valKey.add(val);break;}
								 break;
							 }
							 else{valKey.add(val);break;}						 
						 }
					 }
				 }
			 }
			 if(null==valKey){return null;}
			 for(String val:valKey){
				 if(sb==null)
					 sb=new StringBuffer(val);
				 else
					 sb.append("、").append(val);
			 }
			 return sb==null?null:sb.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		 return null;
	 }
	//接接多个姓名
	 public static String getSurName(List<String> list){
		 if(null==list||list.size()==0)
			 return null;
		 try {
			 String[] listSurName=ReadTxt.getDataSurNames();
			 StringBuffer sb=null;
			 String surname =null;
			 List<String> nameList=null;
			 for(String val:list){			
				 for(String val2:listSurName){				 
					 if((val.length()-val2.length())>=1){
						 if(val2.length()>=2){surname=val.substring(0,val2.length());}
						 else
						 surname=String.valueOf(val.charAt(0));
						 if(val2.equals(surname)&&(val.length()-val2.length())<=3){
							 if(null==nameList){nameList=new ArrayList<String>();}
							 if(nameList.size()>0){
								 if(restultList(nameList,val)){nameList.add(val);break;}
								 break;
							 }
							 else{nameList.add(val);break;}
						 }
					 }
				 }			
			 }
			 if(null==nameList){return null;}
			 for(String val:nameList){
				 if(sb==null)
					 sb=new StringBuffer(val);
				 else
					 sb.append("、").append(val);
			 }
			 return sb==null?null:sb.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		 return null;
	 }
	 //比较集合是否存在相同数据
	 public static boolean restultList(List<String> list,String value){
		 for(String val:list){
			 if(val.equals(value)){return false;}
		 }
		 return true;
	 }
	 //根据。,，,；分段
	 public static String[] getSentences(String input) {
	        if (input == null) {
	            return null;
	        } else {	            
	            return input.split("[。,，,；,，]");
	        }
	 
	 }
	 
	   //判断是否存在乱码
	    public static boolean getErrorCode(String title){
	    	for(String er:ReadTxt.ERCOEDING){
	    		if(title.lastIndexOf(er)>=0){
	    			return false;
	    		}
	    	}
	    	return true;
	    }
	    //去掉无用字符
	    public static String getReplaceAll(String value){
			 StringBuffer sb=null;				  
			   if(value!=null&&!"".equals(value)){
//				    sb=new StringBuffer();
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
	    //提取所有人
	    public static Map<String,List<String>> getPersonName(String html){
	    	if(null==html || "".equals(html)){return null;}
	    	html=getReplaceAll(html);
	    	List<String> defendant=null;	
			String[] values=getSentences2(html);
			Map<String,List<String>> map=new LinkedHashMap<String,List<String>>();
			String[] values2=null;
			String value=null;
			for(String val:ReadTxt.KEYWORDKE){
				 value=getListTakelong(values,val);
				 values2=getSentences(value);
				 if(null==values2){continue;}
				 defendant=getPlaintiffText(values2,val);
				if(null!=defendant&&defendant.size()>0){
					map.put(val, defendant);
				}
			}
	    	return map;
	    }
	    //根据关键字提取人
	    public static String getKeyName(Map<String,List<String>> map,int status){
	    	if(map==null){return null;}
	    	String[] keys=null;
	    	 if(status==1)
				 keys=ReadTxt.PLAINTIFF;
			 else
				 keys=ReadTxt.DEFENDANT;
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
	    //过滤关键字
	    public static String getRpacleRllKey(String value, String key){
	    	if(null==value||"".equals(value)){return null;}
	    	for(String val:ReadTxt.REPACLEALLKEY){
	    		value=value.replaceAll(val+key,"");
	    	}
	    	return interception(value);
	    }
	    
	    //截取括号中的数据
	    public static String interception(String value){
	    	if(null==value||"".equals(value)){return null;}
	    	int index=0;
	    	int index2=0;
	    	StringBuffer sb=null;
	    	for(String val:ReadTxt.PARENTH){
				while((index=value.indexOf(val))!=-1){
					sb=new StringBuffer();
					index2=value.indexOf(ReadTxt.MAP.get(val));
					if(index2==-1){					
						break;
					}
					else{
						if(index2<index){break;}
						sb.append(value.substring(0,index));
						sb.append(value.substring(index2+1,value.length()));
						value=sb.toString();						
					}
				}
			}
	    	return value;
	    } 

}
