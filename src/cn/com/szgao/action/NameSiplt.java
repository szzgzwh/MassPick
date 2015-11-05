package cn.com.szgao.action;

import java.util.List;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

public class NameSiplt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Segment segment = HanLP.newSegment().enableNameRecognize(true);
		   List<Term> termList = segment.seg("dfsdf小在方立忠fsdfsd343");
		    System.out.println(termList);

	}

}
