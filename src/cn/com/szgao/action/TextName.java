package cn.com.szgao.action;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.tags.TableTag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

public class TextName {

	private static Logger logger = LogManager.getLogger(TextName.class.getName());
	/**
	 * @param args
	 */
	//机构分词器
	public static  Segment  INSTITUTIONS = HanLP.newSegment().enableOrganizationRecognize(true);
	public static   Segment SERMENTNAMES = HanLP.newSegment().enableNameRecognize(true);
	public static String[] data={"二〇","一九","二○","二０","二0","二O","二0","二Ｏ","二�","20","19"};		
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		  PropertyConfigurator.configure("F:\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties"); 
		  String value= ExtractionText.getText("","E:/Company_File/要处理的数据/江西省/东乡县人民法院20150414-1/1cc2f417-3379-5157-95cf-2d2efff6bbd2.html");
		  System.out.println(value);
		  System.out.println("法院名:"+getAtherthe(value));
		  System.out.println("案由:"+StringCause(value));
		  System.out.println("审结日期:"+getConcludeDate(value));
		  System.out.println("判决结果:"+getTheVerdictData(value));
		  System.out.println("文书类型:"+getRunningClass(value));
		  System.out.println("取标题:"+getTitle2("E:/Company_File/要处理的数据/cpwsw/html文件/ms/00187138-a80b-5aae-9790-cabb3907a7fd.html"));
		  System.out.println("文书型号:"+getSentenceNo3(value));
	}
	//拆分人名
	public static List<String> getSpiltNames(String value){	
		List<Term> termList = SERMENTNAMES.seg(value);
		List<String> list=new ArrayList<String>();
		for(Term t:termList){
	    	if(t.length()>1&&t.length()<=4){	    		
	    		if(list.size()==0)
		    		list.add(t.toString());
		    		else{
		    			boolean com=true;
		    			for(String val:list){
		    				if(val.equalsIgnoreCase(t.toString())){
		    					com=false;
		    					break;
		    				}	    						    				
		    			}
		    			if(com){
		    				list.add(t.toString());
		    			}
		    				
		    		}
	    	}
	    		
	    }
		return list;
	}
	//拆分公司
	public static List<String> getSpiltComPanys(String value){
		if(null==value||"".equals(value)){return null;}
		List<String> list=null;
        int index=value.indexOf("、");
        if(index==-1&&value.length()<=8){
        	list=new ArrayList<>();
        	list.add(value);
        	return list;
        }
		Segment segment = HanLP.newSegment().enableOrganizationRecognize(true);		
		List<Term> termList = segment.seg(value);
		try {
			for(Term t:termList){
		    	if(t.length()>3){
		    		if(null==list){list=new ArrayList<>();}
		    		if(list.size()==0){		    			
		    			list.add(t.toString());		    		
		    		}	    		
		    		else{
		    			boolean com=true;
		    			for(String val:list){
		    				if(val.equals(t.toString())){
		    					com=false;
		    					break;
		    				}
		    				
		    			}
		    			if(com)
		    				list.add(t.toString());
		    		}
		    	}	    		
		    }
			return list;
		} catch (Exception e) {
			logger.error("拆分公司出错:"+e.getMessage());
		}
		  return null;
		
	}
	//提取法院名称
	public static String getAtherthe(String value){
		 String gatherthe=null;
		 String[] gatherthes=null;
		 StringBuffer sb=null;
		 String[] valuesplit=value.split("。");
		 int index=0;
		try{
			for(String val:valuesplit){
				index=getDateIndex2(val);
				if(index>=0){
					value=val;
					break;
				}
			}
			 if(index==-1){return null;}
			 gatherthe=value.substring(index,value.length());
			 gatherthe=gatherthe.substring(0,gatherthe.indexOf("书")+1);
			 gatherthes=gatherthe.split("[0-9,\\-,:,_,：]");
			 sb=new StringBuffer();
			 for(String val:gatherthes){
				 if(null!=val)
					 sb.append(val);
			 }
			 if(sb.toString().lastIndexOf("}")>=0){
				 gatherthe=sb.toString().substring(sb.toString().lastIndexOf("}")+1,sb.toString().length());
				 sb=new StringBuffer(gatherthe);
			 }
			 gatherthe=sb.toString().substring(0,sb.toString().lastIndexOf("院")+1);			     
			return gatherthe;
		}
		catch(Exception e){
			logger.error("提取法院名称出错:"+e.getMessage());
		}
		finally{
			sb=null;
			gatherthes=null;
		}
		return null;
	} 
	//提取法院名称
		public static String getAtherthe2(String value){

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
	//提取行书类型
	public static String getRunningClass(String value){	
		if(null==value||"".equals(value)){return null;}
		try {
			String[] valueSipt =value.split("。");
			int index=0;
			for(String val:valueSipt){
				index=getDateIndex2(val);
				if(index>=0){
					value=val;
					break;
				}
			}
			if(index==-1){return null;}
			//value=value.substring(0,value.indexOf("。"));
			index=value.lastIndexOf("书");
			if(index==-1){return null;}
			value=value.substring(0,index+1);
			for(String val:ReadTxt.BOOKCLASS){
				index=value.lastIndexOf(val);
				if(index>=0){
					value=value.substring(index+val.length(),value.length());
					return value;
				}
			}
		} catch (Exception e) {
			logger.error("提取行书类型出错:"+e.getMessage());
		}		
		return null;
	}
	//提取案由
	public static String StringCause(String value){
		try {
			int index=0;
			String firTxt=null;
			String lastxt=null;
			int count=0;
			for(String val:ReadTxt.CAUSE){
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
	//取审结日期
	public static String getConcludeDate(String date){
		//date=ExtractionText.getReplaceAll(date);
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
//    		for(String val : data){
//    			int index = value.lastIndexOf(val);
//    			if(index == -1){return null;}
//    			value=value.substring(index);
//    			int index2 = value.indexOf("日");
//    			if(index2 == -1){return null;}
//    			value = value.substring(0, index2);
//    			return value;
//    		}
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
	//提取判决结果
	public static String getTheVerdictData(String value){
		try {
			String text=null;
			int index=0;
			for(String key:ReadTxt.THEVERDICT){
				index=value.lastIndexOf(key);
				if(index>=0){
				  text=value.substring(index, value.length());
				index=text.lastIndexOf("公告");
				if(index>=0)
				  text=text.substring(0, index);	
				text=text.substring(0,text.lastIndexOf("。"));
	    		if(null!=text)
	    			return text;
				}
			}			
    		
		} catch (Exception e) {
			  logger.error("提取判决结果出错:"+e.getMessage());
		}
		return null;
	}
	//取文书编号
	public static String getSentenceNo(String path,String html){
		try {
			String valthml=URLText.getStringBeanText(path+html);
			if(null==valthml||"".equals(valthml)){return null;}
			valthml=ExtractionText.getReplaceAll(valthml);
			String[] valueSipt =valthml.split("。");
			int index=0;
			for(String val:valueSipt){
				index=getDateIndex2(val);
				if(index>=0){
					valthml=val;
					break;
				}
			}
			if(index==-1){return null;}
			index=valthml.lastIndexOf("书");
			if(index==-1){
				return null;
			}
			valthml=valthml.substring(index+1, valthml.length());
			index=valthml.indexOf("号");
			if(index==-1){return null;}
			return valthml.substring(0, index+1);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
		
	}
	//取文书编号
	public static String getSentenceNo3(String valthml){
		try {				
			if(null==valthml||"".equals(valthml)){return null;}
			String[] valueSipt =valthml.split("。");
			int index=0;
			for(String val:valueSipt){
				index=getDateIndex2(val);
				if(index>=0){
					valthml=val;
					break;
				}
			}
			if(index==-1){return null;}
			index=valthml.lastIndexOf("书");
			if(index==-1){
				return null;
			}
			valthml=valthml.substring(index+1, valthml.length());
			index=valthml.indexOf("号");
			if(index==-1){return null;}
			return valthml.substring(0, index+1);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;	
	}
	//取文书编号
	public static String getSentenceNo2(String docTxt){
	 try{
		if(null==docTxt||"".equals(docTxt)){return null;}			
		docTxt=docTxt.substring(0,docTxt.indexOf("。"));
		int index=docTxt.lastIndexOf("书");
		if(index==-1){
			return null;
		}
		int lastIdnex=0;
		String value=docTxt.substring(index+1,docTxt.length());
		lastIdnex=value.indexOf("号");
		if(lastIdnex==-1){
			return null;
		}
		value=value.substring(0,lastIdnex+1);
		return value;
	} catch (Exception e) {
		logger.error(e.getMessage());
	}
	return null;
	}
	//取第一段
	public static int getDateIndex(String value){
		int index=0;
		value=value.substring(0,value.indexOf("。"));		
		for(String date:ReadTxt.DATESTATUS){
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
		for(String date:ReadTxt.DATESTATUS){
			index=value.indexOf(date);
			if(index>=0){
				 return index+date.length();
			}
		}
		return -1;
	}
	//取标题
	public static String getTitle(String html){		
		Document doc;
		try {
			doc = Jsoup.connect(html).get();			
			return  doc.title();
		} catch (IOException e) {			
			logger.error(e.getMessage()+":"+html+":网址访问失败");
		}
		return null;
	}
	public static String getTitle2(String path){		
		File input = new File(path);		
		try {
			//Document doc = Jsoup.parse(input, "UTF-8");
			Document doc = Jsoup.parse(input,"UTF-8");
			String title=doc.title();
			if(getErrorCode(title)){
				return ExtractionText.getReplaceAll(title);
			}
			else{
				for(String cod:ReadTxt.CODING){
					doc = Jsoup.parse(input,cod);
					title=doc.title();
					if(getErrorCode(title)){
						return ExtractionText.getReplaceAll(title);
					}
				}
				return null;
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage()+":"+path+":网址访问失败");
		}
		return null;
	}
	public static String getTitle3(String value){		
		try {
			int index=value.indexOf("书");
			if(index==-1){return null;}
			return value.substring(0,index+1);
		} catch (Exception e) {
			logger.error(e.getMessage()+":获取标题失败");
		}
		return null;
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
}
