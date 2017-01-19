package org.anyline.lucene;

import java.util.List;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

public class Test {
	public static void main(String[] args) {
		String text = "香桥缘过桥米线";
		System.out.println(StandardTokenizer.segment(text));
		StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(true);
		System.out.println(StandardTokenizer.segment(text));
		
		List<Term> termList = HanLP.segment(text);
		System.out.println(termList);
	}
}