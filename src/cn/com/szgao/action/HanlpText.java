package cn.com.szgao.action;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

import com.hankcs.common.HanLPTokenizer;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.dictionary.BaseSearcher;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.hanlp.summary.TextRankSentence;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;

public class HanlpText {

	/**
	 * @param args
	 * @throws ParserException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParserException, IOException {
		// TODO Auto-generated method stub
	
        Parser parser=new Parser("E:/Company_File/cpwsw/xs/00009726-5527-5bc1-8a02-5497e4ef2dff.html");
		//parser.setEncoding("utf-8");
		Node [] tables = parser.extractAllNodesThatAre(TableTag.class); 
		StringBuffer sb=new StringBuffer();	
		for (int i = 0; i < tables.length; i++)    {
			TableTag tableTag = (TableTag)tables[i]; 
			sb.append(tableTag.toPlainTextString());
			
		}
		
       List<String> sentenceList2 = HanLP.extractSummary(sb.toString(),6);		
        for(String v:sentenceList2){
        	System.out.println(v);
        } 
        Suggester suggester = new Suggester();
        List<String> vals=spiltSentence(sb.toString());
        for (String title : vals)
        {
            suggester.addSentence(title);
        }
       ;
        //suggester.addSentence(sb.toString());
        System.out.println(suggester.suggest("被告", 1));  
        System.out.println(suggester.suggest("原告", 1));  
        System.out.println(suggester.suggest("审判结论",1));  
	}
	 static List<String> spiltSentence(String document)
	    {
	        List<String> sentences = new ArrayList<String>();
	        for (String line : document.split("[\r\n]"))
	        {
	            line = line.trim();
	            if (line.length() == 0) continue;
	            for (String sent : line.split("[，,。:：？?！!；;]"))
	            {
	                sent = sent.trim();
	                if (sent.length() == 0) continue;
	                sentences.add(sent);
	            }
	        }

	        return sentences;
	    }
}
