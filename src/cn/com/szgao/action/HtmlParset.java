package cn.com.szgao.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.summary.TextRankSentence;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

public class HtmlParset {

	/**
	 * @param args
	 * @throws ParserException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParserException, IOException{
		// TODO Auto-generated method stub

		 // Parser parser =  parser = Parser.createParser("dfsdf", "utf-8"); 
		/*  Parser parser = new Parser("http://www.bjcourt.gov.cn/cpws/paperView.htm;jsessionid=B4DD18FE699AD7462A5D3B393D64FE25?id=100158352622");
		  Node [] tables = parser.extractAllNodesThatAre (TableTag.class);
		  String value=null;
			if(tables!=null&&tables.length>0) {	
				TableTag tableTag = (TableTag)tables[0];	
				//value = tableTag.getStartPosition()+""+ new String(tableTag.toHtml().getBytes("utf-8"));
				     
			}*/
		//不完整格式的HTML信息
	/*	Document doc=Jsoup.connect("http://www.bjcourt.gov.cn/cpws/paperView.htm;jsessionid=B4DD18FE699AD7462A5D3B393D64FE25?id=100161822667").get();
		System.out.println(doc.toString());*/
		
		//Parser parser =Parser.createParser(html,"utf-8");
		Parser parser=new Parser("file:///E:/Company_File/cpwsw/ms/0000eae8-8891-5ae1-bac8-22adaa165606.html");
		//parser.setEncoding("utf-8");
		Node [] tables = parser.extractAllNodesThatAre(TableTag.class); 
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i < tables.length; i++)    {
			TableTag tableTag = (TableTag)tables[i]; 
			//打印出结束标签所在的未知       
			//System.out.println("END POS:"+tableTag.getEndTag().getEndPosition());  
			//补齐未结束的标签并打印   
			//System.out.println(tableTag.toString());
			
			sb.append(tableTag.toPlainTextString().trim());
			//System.out.println(tableTag.toPlainTextString());
			//System.out.println(new String(tableTag.toPlainTextString().getBytes("utf-8")));  
		}    
		//System.out.println(sb.toString());
		//TextRankSentence.getTopSentenceList(sb.toString(), 6);
		//System.out.println(HanLP.segment(sb.toString()));
	/*	List<Term> termList = NLPTokenizer.segment("中国科学院计算技术研究所的宗成庆教授正在教授自然语言处理课程");
		System.out.println(termList);*/	
		/*Segment segment=null;
		segment=HanLP.newSegment().enablePlaceRecognize(false);
		List<Term> termList = segment.seg("fsdfsdf小强sb lh 李四，张三");
	    System.out.println(termList);*/
		CustomDictionary.insert("法院","nz 1024");
		List<String> sentenceList = HanLP.extractKeyword(sb.toString(),6);		
		for(String v:sentenceList)
		System.out.println(v);
		
	}

}
