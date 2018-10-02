package com.xiaobai.lucene.chinese;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;

/**
 * 替换Lucene版本为4.4.0
 * 使用IKAnalyzer中文分词器（常用版本IKAnalyzer2012_FF，这里是2012_u6，只能兼容Lucene版本4.4.0，高版本analyzer.tokenStream方法报错：AbstractMethodError: org.apache.lucene.analysis.Analyzer.createComponents）
 */
public class LuenceFirst {

    // 查看分析器的分词效果
    @Test
    public void testAnanlyzer() throws IOException {
        // 1、创建一个分析器对象
        Analyzer analyzer = new IKAnalyzer(); // 智能中文分析器
        // 2、从分析器对象中获得tokenStream对象
        // 参数1：域的名称，可以为null，或者是""
        // 参数2：要分析的文本
        TokenStream tokenStream = analyzer.tokenStream("", "数据库习近平中存储的数据是结构化数据高富帅，即行数据java，可以用二维表结构来逻辑表达实现的数据。");

        // 3、设置一个引用(相当于指针)，这个引用可以是多种类型，可以是关键词的引用，偏移量的引用等等
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class); // charTermAttribute对象代表当前的关键词
        // 偏移量(其实就是关键词在文档中出现的位置，拿到这个位置有什么用呢？因为我们将来可能要对该关键词进行高亮显示，进行高亮显示要知道这个关键词在哪？)
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        // 4、调用tokenStream的reset方法，不调用该方法，会抛出一个异常
        tokenStream.reset();
        // 5、使用while循环来遍历单词列表
        while (tokenStream.incrementToken()) {
            System.out.println("start→" + offsetAttribute.startOffset()); // 关键词起始位置
            // 6、打印单词
            System.out.println(charTermAttribute);
            System.out.println("end→" + offsetAttribute.endOffset()); // 关键词结束位置
        }
        // 7、关闭tokenStream对象
        tokenStream.close();
    }

}



