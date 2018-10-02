package com.xiaobai.lucene;


import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**替换Lucene版本为7.2.x
 * 根据索引搜索
 *TODO
 * @author Snaiclimb
 * @date 2018年3月25日
 * @version 1.8
 */
public class Searcher {

    public static void search(String indexDir, String q) throws Exception {

        // 得到读取索引文件的路径
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        // 通过dir得到的路径下的所有的文件
        IndexReader reader = DirectoryReader.open(dir);
        // 建立索引查询器
        IndexSearcher is = new IndexSearcher(reader);
        // 实例化分析器
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new SmartChineseAnalyzer();
        // 建立查询解析器
        /**
         * 第一个参数是要查询的字段； 第二个参数是分析器Analyzer
         */
        QueryParser parser = new QueryParser("contents", analyzer);//带分词器的查询解析
        // 根据传进来的p查找
        Query query = parser.parse(q);//这里进行了分词，Spring Cloud被分成Spring和Cloud两个词
        // 计算索引开始时间
        long start = System.currentTimeMillis();
        // 开始查询
        /**
         * 第一个参数是通过传过来的参数来查找得到的query； 第二个参数是要出查询的行数
         */
        //第二个参数：查询符合条件的前100条记录
        /**
         * 查询是这样：每篇文章是作为一-个-记-录，也就是一个Document写入索引文件的，所以这里查出的记录数是包含查询关键词的文章数，而！不！是！所有文章中出现这个词的总数！！
         * 索引的原理是倒排索引：记录一个索引词出现在哪些文章中，记录这些文章的唯一编号（ID），而不是一篇文章出现了什么词、多少次
         * 所以这里返回包含"原理"这个词的文章数（即记录数），也就是3篇，对应三个文件，控制台列出的记录也是对应的3个文件名，代表3个记录，不是说这个词一共出现3次，而是它出现在3篇文章里
         * 这里的windows环境的txt文件有些问题：需要使用notepad命令编辑数据，保存为UTF-8格式才可正常搜索到中文关键字，直接鼠标右键建立或默认保存的
         * 文件无法正确查询到中文关键词，需要格外注意！！
         */
        TopDocs hits = is.search(query, 100);//将分词后的关键字封装为查询条件，从索引库查询
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
        // 遍历hits.scoreDocs，得到scoreDoc
        /**
         * ScoreDoc:得分文档,即得到文档 scoreDocs:代表的是topDocs这个文档数组
         *
         * @throws Exception
         */
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullPath"));
        }

        // 关闭reader
        reader.close();
    }

    public static void main(String[] args) {
        String indexDir = "D:\\lucene\\dataindex";
        //我们要搜索的内容
//        String q = "Spring Cloud";//目前这个分词器，无法搜索中文内容
        String q = "原理";
        try {
            search(indexDir, q);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

