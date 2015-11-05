package cn.com.szgao.action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

import com.hankcs.hanlp.HanLP;

public class TextFile {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserException 
	 */
	public static void main(String[] args) throws IOException, ParserException {
		// TODO Auto-generated method stub

		FileInputStream file=new FileInputStream("E:/Company_File/cpwsw/ms/000021f1-54dd-53c9-af32-6d96e58c7962.html");
		BufferedReader bu=new BufferedReader(new InputStreamReader(file));
		String value=null;
		StringBuffer sb=new StringBuffer();
		while((value=bu.readLine())!=null){
			sb.append(value);
		}
        Parser myParser;
        myParser = Parser.createParser(sb.toString(),"utf-8");
        HtmlPage visitor = new HtmlPage(myParser);

        myParser.visitAllNodesWith(visitor);

        String textInPage = visitor.getTitle();
        System.out.println(textInPage);
        NodeList nodelist ;
        nodelist = visitor.getBody();
        System.out.print(nodelist.asString().trim());
	}

}
