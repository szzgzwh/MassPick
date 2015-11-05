package cn.com.szgao.action;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.ParserException;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


public class Text2 {
	 static Map<String, Integer> keywords = new LinkedHashMap<String, Integer>();
	 static String text=null;
	 static{
		    //keywords.put("法院",10000);	
		    keywords.put("驳回申诉通知书",10000);
		    keywords.put("原告",10000);
		    keywords.put("被告",10000);			   
		    keywords.put("上诉人",10000);		    	   
		    keywords.put("被上诉人",10000);		   
		    keywords.put("公诉",10000);	        
	        keywords.put("法定代表人",10000);
	        keywords.put("申请人",10000);
	        keywords.put("申请执行人",10000);
	        keywords.put("被执行人",10000);
	        keywords.put("委托代理人",10000);	       
	        keywords.put("诉讼类型",10000);
	        keywords.put("驳回申诉通知书",10000);
	        keywords.put("裁定书",10000);
	        keywords.put("判决书",10000);
	        keywords.put("决定书",10000);
	        //keywords.put("通知书",10000);
	        keywords.put("起诉日期",10000);	       
	        keywords.put("审批结论",10000);
	        keywords.put("判决如下",10000);
	        /*keywords.put("一、",10000);
	        keywords.put("二、",10000);
	        keywords.put("三、",10000);
	        keywords.put("四、",10000);
	        keywords.put("五、",10000);
	        keywords.put("六、",10000);	   
	        keywords.put("七、",10000);	 
	        keywords.put("八、",10000);	
	        keywords.put("九、",10000);	*/
	        keywords.put("裁定如下",10000);	 
	        keywords.put("决定如下",10000);	
	        keywords.put("终审裁定",10000);
	        keywords.put("终审判决",10000);		        
	        keywords.put("审结日期",10000);
	        //keywords.put("一〇",10000);
	        // keywords.put("纠纷",10000);
	 }
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserException 
	 */
	public static void main(String[] args) throws IOException, ParserException {
		
		Parser parser=new Parser("file:///E:/Company_File/cpwsw/zx/fffde93b-2b52-53fd-8d8c-4e5e572171cf.html");
		parser.setEncoding("utf-8");
		Node [] tables = parser.extractAllNodesThatAre(TableTag.class); 
		StringBuffer sb=new StringBuffer();
		String value=null;
		for (int i = 1; i < tables.length; i++)    {
			TableTag tableTag = (TableTag)tables[i]; 
			//打印出结束标签所在的未知       
			//System.out.println("END POS:"+tableTag.getEndTag().getEndPosition());  
			//补齐未结束的标签并打印   
			//System.out.println(tableTag.toString());
			value=tableTag.toPlainTextString();
			//value=value.replaceAll("&nbsp;","");
			value=value.replaceAll("[\\（,\\）,\n,\t,&nbsp; ,]","");
			//value=value.replaceAll("[\\）]","");
			//value=value.replaceAll(" ","");
			//value=value.replaceAll("","");
			value=value.trim();
			sb.append(value);			
			//System.out.println(value);  			
		} 
		text=sb.toString();
		/*String values=text.substring(0, text.lastIndexOf("公告"));
		values=values.substring(values.lastIndexOf("。"), values.length());
		String[] cc=values.split("");
    	System.out.println(values.split("").toString());*/
		
		System.out.println(summarise(sb.toString(), keywords.size()));
	}
	public static String summarise(String input, int numSentences) {
        // get the frequency of each word in the input
        //Map<String,Integer> wordFrequencies = segStr(input);
        
        // now create a set of the X most frequent words
        Set<String> mostFrequentWords = getMostFrequentWords(100,null).keySet();
        Iterator<String> it = mostFrequentWords.iterator();
        // break the input up into sentences
        // workingSentences is used for the analysis, but
        // actualSentences is used in the results so that the 
        // capitalisation will be correct.
        String[] workingSentences = getSentences(input.toLowerCase());
        String[] actualSentences = getSentences(input);
 
     /*   Map<String, Set<String>> maps=new HashMap<String, Set<String>>();
        Set<String> set=null;
        String words =null;
        while(it.hasNext()){
        	  words = (String) it.next();
        	  for (int i = 0; i < workingSentences.length; i++) {
        		  if (workingSentences[i].indexOf(words) >=0) {
        			  maps.put(words,addSets(maps.get(words),actualSentences[i].substring(workingSentences[i].indexOf(words),workingSentences[i].length())));
        		  }
        	  }
        }
        StringBuffer results = new StringBuffer("");
        for(Map.Entry<String,Set<String>> ma:maps.entrySet()){
        	results.append(ma.getKey()+":"+ma.getValue()+"\n");
        }
        if(true)
        return results.toString();*/
        // iterate over the most frequent words, and add the first sentence 
        // that includes each word to the result
        Set<String> outputSentences = new LinkedHashSet<String>();  
        Map<String,List<String>> maps=new HashMap<String,List<String>>();
        boolean com=false;
        while (it.hasNext()) {
            String word = (String) it.next();
            for (int i = 0; i < workingSentences.length; i++) {
            	//System.out.println(workingSentences[i]+"=========="+word);
            	//System.out.println(outputSentences.size()+":"+workingSentences[i]+"=========="+word);
            	//System.out.println(":"+workingSentences[i].indexOf(word)+":"+workingSentences[i]+"=========="+word);
                if (workingSentences[i].indexOf(word) >=0) {
                	if(word.equalsIgnoreCase("判决如下")||word.equalsIgnoreCase("裁定如下")||word.equalsIgnoreCase("决定如下")){
                		String value=text.substring(text.lastIndexOf(word), text.lastIndexOf("公告"));
                		//System.out.println(value.substring(0,value.lastIndexOf("。")));
                		value=value.substring(0,value.lastIndexOf("。"));
                		outputSentences.add(value);
                	}
                	else if(word.equalsIgnoreCase("驳回申诉通知书")){
                		String value=text.substring(text.indexOf("号")+1, text.lastIndexOf("公告"));
                		char[] texts=value.toCharArray();
                		StringBuffer sb=new StringBuffer();
                		for(char te:texts){
                			if(!"12288".equals((int)te+"")){
                				sb.append(te);
                			}
                		}
                		//System.out.println(sb.toString());
                		outputSentences.add(word);
                		value=sb.toString().substring(0,sb.toString().lastIndexOf("。")).trim();
                		outputSentences.add(value);
                		com=true;                		
                		break;
                	}
                	else if(word.equalsIgnoreCase("裁定书")||word.equalsIgnoreCase("判决书")){
                		String value = null;        
                		try{
                			//outputSentences.add(word);
                			String style;
                			String[] styles={"刑事","民事","行政","执行"};
                		if(actualSentences[i].indexOf("裁定书")>=0){
                			//System.out.println(actualSentences[i].indexOf("裁定书"));
                			 style=actualSentences[i].substring(actualSentences[i].indexOf("裁定书")-2, actualSentences[i].indexOf("裁定书"));
                			 boolean result=false;
                			 for(String va:styles){
                				 if(va.equalsIgnoreCase(style)){
                					 result=true;
                					 break;
                				 }
                			 }               			        			
                			 if(result)
                				  value=actualSentences[i].substring(actualSentences[i].indexOf("裁定书")-2, actualSentences[i].indexOf("裁定书")+3);     
                			 else
                			      value=actualSentences[i].substring(actualSentences[i].indexOf("裁定书")-4, actualSentences[i].indexOf("裁定书")+3);                   		
                    		 outputSentences.add(value);                        		 
                		}
                		if(actualSentences[i].indexOf("判决书")>=0){                		
                			 style=actualSentences[i].substring(actualSentences[i].indexOf("判决书")-2, actualSentences[i].indexOf("判决书"));
                			 boolean result=false;
                			 for(String va:styles){
                				 if(va.equalsIgnoreCase(style)){
                					 result=true;
                					 break;
                				 }
                			 }
                			 //String value;
                			 if(result)
                				  value=actualSentences[i].substring(actualSentences[i].indexOf("判决书")-2, actualSentences[i].indexOf("判决书")+3);     
                			 else
                			      value=actualSentences[i].substring(actualSentences[i].indexOf("判决书")-4, actualSentences[i].indexOf("判决书")+3);                   		
                    		 outputSentences.add(value);                       		 
                		 }
                		}
                		catch(Exception e){
                			System.out.println(word+":"+e.getMessage());
                		}               		
                		com=true;
                	}
                	else if("决定书".equalsIgnoreCase(word)){		
                		try{
                			if(!com){
		                		String value=actualSentences[i].substring(actualSentences[i].indexOf("决定书"), actualSentences[i].indexOf("决定书")+3);                   		
		                		outputSentences.add(value);		                		
                			}
                		}
                		catch(Exception e){
                			System.out.println(word+":"+e.getMessage());
                		}
                	}
                	else{
                		String val=actualSentences[i].substring(workingSentences[i].indexOf(word),workingSentences[i].length());
                		val=val.replace("\t","");
                        outputSentences.add(val.trim());                     
                	}
                    break;
                }               
                if (outputSentences.size() >= numSentences) {
                    break;
                }
            }
            if(word.equalsIgnoreCase("审结日期")){
            	String[] data={"二〇","一九","二○"};
            	int[] splt={9,10,11,12};
            	String value;
            	String[] datas;
            	boolean result=false;
            	try{
            	for(int index=0;index<data.length;index++){           		
            		if(text.lastIndexOf(data[index])<0){
            			continue;
            		}
            		value=text.substring(text.lastIndexOf(data[index]));
            		for(int index2=0;index2<splt.length;index2++){
            			//System.out.println(value);
                    	datas=value.split("");
                    	String da=datas[splt[index2]];
                    	if("日".equals(da)){
                    		value=value.substring(0,splt[index2]);
                    		outputSentences.add(word+":"+value);                    	
                    		result=true;;
                    		break;
                    	}
            		}
            		if(result)
            			break;
            	  }               	
            	}
            	catch(Exception  e){
            		System.out.println(word+":"+e.getMessage());
            	}            	
            }
           if (outputSentences.size() >= numSentences) {
                break;
            }
 
        }
 
        List<String> reorderedOutputSentences = reorderSentences(outputSentences, input);
        StringBuffer result = new StringBuffer("");
        it = reorderedOutputSentences.iterator();
        while (it.hasNext()) {
            String sentence = (String) it.next();
            /*sentence=sentence.replaceAll("\n","");
            sentence=sentence.replaceAll("&nbsp;","");
            sentence=sentence.replaceAll(" ","");
            sentence=sentence.trim();*/
            if(sentence==null)
            	continue;
           /* for(String va:sentence.split("；"))
              result.append(va);*/
            result.append(sentence);
            // This isn't always correct - perhaps it should be whatever symbol the sentence finished with
            if (it.hasNext()) {
            	 result.append("\n");
            }
        }       
        return result.toString();
    }
	private static List<String> reorderSentences(Set<String> outputSentences, final String input) {
        // reorder the sentences to the order they were in the 
        // original text
        ArrayList<String> result = new ArrayList<String>(outputSentences);
 
       /* Collections.sort(result, new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                String sentence1 = (String) arg0;
                String sentence2 = (String) arg1;
 
                int indexOfSentence1 = input.indexOf(sentence1.trim());
                int indexOfSentence2 = input.indexOf(sentence2.trim());
                int result = indexOfSentence1 - indexOfSentence2;
 
                return result;
            }
 
        });*/
        return result;
    }
	public static Map<String, Integer> getMostFrequentWords(int num,Map<String,Integer> wodros){
		 
        //Map<String, Integer> keywords = new LinkedHashMap<String, Integer>();
        int count=0;
        // 词频统计
        List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>();
        Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                return obj2.getValue() - obj1.getValue();
            }
        });
        // 高频词输出
        for (int j = 0; j < info.size(); j++) {
            // 词-->频
            if(info.get(j).getKey().length()>1){
                if(num>count){
                    keywords.put(info.get(j).getKey(), info.get(j).getValue());
                    count++;
                }else{
                    break;
                }
            }
        }
        return keywords;
    }
	 public static Map<String, Integer> segStr(String content){
	        // 分词
	        Reader input = new StringReader(content);
	        // 智能分词关闭（对分词的精度影响很大）
	        IKSegmenter iks = new IKSegmenter(input, true);
	        Lexeme lexeme = null;
	        Map<String, Integer> words = new LinkedHashMap<String, Integer>();
	        try {
	            while ((lexeme = iks.next()) != null) {
	                if (words.containsKey(lexeme.getLexemeText())) {
	                    words.put(lexeme.getLexemeText(), words.get(lexeme.getLexemeText()) + 1);
	                } else {
	                    words.put(lexeme.getLexemeText(), 1);
	                }
	            }
	        }catch(IOException e) {
	            e.printStackTrace();
	        }	       
	        
	        return words;
	    }
	 public static String[] getSentences(String input) {
	        if (input == null) {
	            return new String[0];
	        } else {
	            // split on a ".", a "!", a "?" followed by a space or EOL
	        	//"(\\.|!|\\?)+(\\s|\\z)"
	            return input.split("[。,、]");
	        }
	 
	    }
	 public static Set<String> addSets(Set<String> sets,String value){		
		 if(null==sets||sets.size()==0){
			 sets=new HashSet<String>();			
		 }
		 sets.add(value);
		 return sets;
	 }
	 public static String joinDate(String[] values){
		 if(null==values)
			 return null;
		 for(String val:values){
			 
		 }
		 return null;
	 }
}
