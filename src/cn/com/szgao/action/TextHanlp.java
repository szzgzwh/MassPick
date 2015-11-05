package cn.com.szgao.action;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.lucene4.HanLPIndexAnalyzer;

public class TextHanlp {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String text = "中华人民共和国很辽阔";
		for (int i = 0; i < text.length(); ++i)
		{
		    System.out.print(text.charAt(i) + "" + i + " ");
		}
		System.out.println();
		Analyzer analyzer = new HanLPIndexAnalyzer();
		 StringReader reader = new StringReader(text);
		TokenStream tokenStream = analyzer.tokenStream("field", reader);
		while (tokenStream.incrementToken())
		{
		    CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
		    // 偏移量
		    OffsetAttribute offsetAtt = tokenStream.getAttribute(OffsetAttribute.class);
		    // 距离
		    PositionIncrementAttribute positionAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
		    System.out.println(attribute + " " + offsetAtt.startOffset() + " " + offsetAtt.endOffset() + " " + positionAttr.getPositionIncrement());
		}
	}

}
