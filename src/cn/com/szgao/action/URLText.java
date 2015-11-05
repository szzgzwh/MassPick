package cn.com.szgao.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class URLText {
	private static Logger logger = LogManager.getLogger(URLText.class.getName());
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserException 
	 */
	public static void main(String[] args) throws IOException, ParserException {
		// TODO Auto-generated method stub		
		   //System.out.println(getHtmlText("http://www.dgcourt.gov.cn/info/CPWSShow.asp?id=986"));		
		    //Document doc = Jsoup.parse("");
		    StringBean sb = new StringBean();           
	        //设置不需要得到页面所包含的链接信息  
	        sb.setLinks(false);  
	        //设置将不间断空格由正规空格所替代  
	        sb.setReplaceNonBreakingSpaces(true);  
	        //设置将一序列空格由一个单一空格所代替  
	        sb.setCollapse(true);  
	        //传入要解析的URL  
	        //sb.setURL("file:///E:\\Company_File\\要处理的数据\\北京市\\北京法院审判信息\\a3043d68-7f2e-51a5-ab6e-5b51226ad8d0.html");
	        // System.out.println(sb.getStrings());
	       System.out.println(getHtmlText("","file:///E:\\Company_File\\要处理的数据\\北京市\\北京法院审判信息\\a3043d68-7f2e-51a5-ab6e-5b51226ad8d0.html"));
	}
    //通过url提取网页数据
	public static String getHtmlText(String path,String html){
		 URL url;
		 URLConnection connection;
		 BufferedReader  buff;
		 StringBuffer sb;
		try {
			 url=new URL(path+html);
			 connection=url.openConnection();
			 connection.setConnectTimeout(2000);
		     connection.setReadTimeout(2000);
		     connection.connect();
		     String encoding =connection.getContentType();
		     if(encoding.indexOf("charset")==-1){
		    	 encoding="gb2312";
		     }
		     else{
		    	 encoding="utf-8";
		     }
		     buff=new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
		     sb=new StringBuffer();
		     String value=null;
		        while((value=buff.readLine())!=null){       	
		        	sb.append(value);
		        }
		        Parser parser = new Parser();
		        parser.setInputHTML(sb.toString());  
		        parser.setEncoding(parser.getURL());  
		        HtmlPage page = new HtmlPage(parser);  
		        parser.visitAllNodesWith(page);       
		        NodeList list = page.getBody(); 
		        sb=new StringBuffer();
		        for (NodeIterator iterator = list.elements(); iterator  
		                .hasMoreNodes();) {  
		            Node node = iterator.nextNode();           
		            sb.append(node.toPlainTextString());
		        }
		        return sb.toString();
		     
		} catch (Exception e) {			
			e.printStackTrace();
		}	
		return null;
	}
	public static String getStringBeanText(String html){
		try {
		    StringBean sb = new StringBean();           
	        //设置不需要得到页面所包含的链接信息  
	        sb.setLinks(false);  
	        //设置将不间断空格由正规空格所替代  
	        sb.setReplaceNonBreakingSpaces(true);  
	        //设置将一序列空格由一个单一空格所代替  
	        sb.setCollapse(true);  
	        //传入要解析的URL  
	        sb.setURL(html);
	        return sb.getStrings();
		} catch (Exception e) {			
			logger.error(e.getMessage());
		}
		return null;
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
			sb.append(value);			
		  }
		  if(null==sb||"".equals(sb.toString())){
			  value=getStringBeanText(path+values);
			  if(null!=value){
				  sb=new StringBuffer();
				  sb.append(value);
			  }
		  }		
		}
		catch (Exception e) {
			value=getHtml(values);
			if(null!=value&&!"".equals(value)){
				sb=new StringBuffer();
				sb.append(value);
			}
			else{
				value=getStringBeanText(path+values);
				if(null!=value&&!"".equals(value)){
					sb=new StringBuffer();
					sb.append(value);
				}
			}
			
		}
		finally{
			tables=null;
			parser=null;			
		}
		return sb==null?null:sb.toString();		     
	}
	//提取网页内容
	 public static String getHtml(String path){		
		File input = new File(path);		
		try {
			Document doc = Jsoup.parse(input,"UTF-8");
			String title=doc.title();
			if(getErrorCode(title)){
				return doc.text();
			}
			else{
				for(String cod:ReadTxt.CODING){
					doc = Jsoup.parse(input,cod);
					title=doc.title();					
					if(getErrorCode(title)){
						return doc.text();
					}
				}
				return null;
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage()+":"+path+":网址访问失败");
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
