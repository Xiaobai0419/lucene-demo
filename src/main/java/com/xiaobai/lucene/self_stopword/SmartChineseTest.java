package com.xiaobai.lucene.self_stopword;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.util.Iterator;

/**
 * 替换Lucene版本为7.2.x
 */
public class SmartChineseTest {

//    private void print(Analyzer analyzer) throws Exception {
//        String text = "Lucene自带多种分词器，其中对中文分词支持比较好的是smartcn。";
//        TokenStream tokenStream = analyzer.tokenStream("content", text);
//        CharTermAttribute attribute = tokenStream.addAttribute(CharTermAttribute.class);
//        tokenStream.reset();
//        while (tokenStream.incrementToken()) {
//            System.out.println(new String(attribute.toString()));
//        }
//    }
//
//    @Test
//    public void testStandardAnalyzer() throws Exception {
//        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
//        print(standardAnalyzer);
//    }
//
//    @Test
//    public void testSmartChineseAnalyzer() throws Exception {
//        SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
//        print(smartChineseAnalyzer);
//    }
//
//    @Test
//    public void testMySmartChineseAnalyzer() throws Exception {
//        CharArraySet charArraySet = new CharArraySet(0, true);
//        // 系统默认停用词
//        Iterator<Object> iterator = SmartChineseAnalyzer.getDefaultStopSet().iterator();
//        while (iterator.hasNext()) {
//            charArraySet.add(iterator.next());
//        }
//        // 自定义停用词
//        String[] myStopWords = { "对", "的", "是", "其中" };
//        for (String stopWord : myStopWords) {
//            charArraySet.add(stopWord);
//        }
//        SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer(charArraySet);
//        print(smartChineseAnalyzer);
//    }
}
