package cn.com.szgao.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

public class HanPlText {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*String[] testCase = new String[]{
		        "签约仪式前，秦光荣、李纪恒、仇和等一同会见了参加签约的企业家。",
		        "王国强、高峰、汪洋、张朝阳光着头、韩寒、小四张三三关于",
		        "张浩和胡健康复员回家了",
		        "王总和小丽结婚了",
		        "编剧邵钧林和稽道青说",
		        "这里有关天培的有关事迹",
		        "龚学平等领导,邓颖超生前",
		        };
		Segment segment = HanLP.newSegment().enableNameRecognize(true);
		for (String sentence : testCase)
		{
		    List<Term> termList = segment.seg(sentence);
		    System.out.println(termList);
		}*/
		String ff="被告人李云龙（化名“余海平”，乳名“龙龙”），男，1979年10月7日出生于江西省万安县，汉族，初中文化，无业，家住万安县百加镇黄南村黄南16组。";
		System.out.println(ff);
		String[] vals={"（","("};
		int index=0;
		int index2=0;
		StringBuffer sb=null;
		Map<String,String> map=new HashMap<String,String>();
		map.put("（", "）");
		map.put("(", ")");
		for(String val:vals){
			while((index=ff.indexOf(val))!=-1){
				sb=new StringBuffer();
				index2=ff.indexOf(map.get(val));
				if(index2==-1){					
					break;
				}
				else{
					if(index2<index){break;}
					sb.append(ff.substring(0,index));
					sb.append(ff.substring(index2+1,ff.length()));
					ff=sb.toString();
					System.out.println(ff);
				}
			}
		}
		System.out.println(ff);
	}

}
