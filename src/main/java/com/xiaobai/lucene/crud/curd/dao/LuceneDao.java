package com.xiaobai.lucene.crud.curd.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.xiaobai.lucene.crud.curd.domain.Article;
import com.xiaobai.lucene.crud.curd.util.ArticleUtils;
import com.xiaobai.lucene.crud.curd.util.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 * 相关度得分：
 * 内容一样，搜索关键字一样，得分也是一样的
 * 我们可以人工干预得分，就是通过ArticleUtils类中的titleField.setBoost(4f);这样
 * 得分跟搜索关键字在文章当中出现的频率、次数、位置有关系
 * @author chenchi
 *
 */
public class LuceneDao {
    /**
     * 增删改索引都是通过IndexWriter对象来完成
     */
    public void addIndex(Article article) {//每次不是从头创建索引，而是在已有的索引目录增量添加
        try {//如果要重新建立索引，可用更新操作，先删除所有此前的，再从头创建添加
            IndexWriter indexWriter = LuceneUtils.getIndexWriter();
            indexWriter.addDocument(ArticleUtils.articleToDocument(article));
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 删除索引，根据字段对应的值删除
     * @param fieldName
     * @param fieldValue
     * @throws IOException
     */
    public void deleteIndex(String fieldName, String fieldValue) throws IOException {
        IndexWriter indexWriter = LuceneUtils.getIndexWriter();
        //使用词条删除
        Term term = new Term(fieldName, fieldValue);
        indexWriter.deleteDocuments(term);
        indexWriter.close();
    }

    /**
     * 先删除符合条件的记录，再创建一个新的纪录
     * @param fieldName
     * @param fieldValue
     * @param article
     * @throws IOException
     */
    public void updateIndex(String fieldName, String fieldValue, Article article) throws IOException {
        IndexWriter indexWriter = LuceneUtils.getIndexWriter();

        Term term = new Term(fieldName, fieldValue);
        Document doc = ArticleUtils.articleToDocument(article);
        /**
         * 1.设置更新的条件
         * 2.设置更新的内容和对象
         */
        indexWriter.updateDocument(term, doc);
        indexWriter.close();
    }

    /**
     * 查询是通过IndexSearch提供的(分页)
     */
    public List<Article> findIndex(String keywords, int start, int count) {
        try {
            IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher();
            /**
             *  第二种查询：字符串搜索..
             *  使用查询字符串：QueryParser+ MultiFieldQueryParser的查询方式
             *  1、QueryParser：只在一个字段中查询
             *  2、MultiFieldQueryParser：可以在多个字段查询
             *  用来查询可以分词的字段，只要你输入的一段文本中包含分词，就会检索出来
             */
            //===========================================================
            //这里是第二种query方式，不是termQuery---------------------->可按搜索文本分词后的字匹配（可以分词的字段）
            QueryParser queryParser = new MultiFieldQueryParser(//文档多字段检索，其中"title"字段被赋予的相关度权重高，搜索靠前
                    LuceneUtils.getMatchVersion(), new String[] { "title",
                    "content" }, LuceneUtils.getAnalyzer());
            Query query = queryParser.parse(keywords);//这里（创建索引时）content，title字段均按字分词，是否分词（TextField分词、StringField不分词）对查询有不同影响：前者按词拆分匹配，后者按整个字段匹配
            TopDocs topDocs = indexSearcher.search(query, 100);
            System.out.println("总记录数：" + topDocs.totalHits);
            //表示返回的结果集
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            List<Article> list = new ArrayList<Article>();

            int min = Math.min(scoreDocs.length, start + count);
            for (int i = start; i < min; i++) {
                System.out.println("相关度得分："+scoreDocs[i].score);
                //获取查询结果的文档的惟一编号，只有获取惟一编号，才能获取该编号对应的数据
                int doc = scoreDocs[i].doc;
                //使用编号，获取真正的数据
                Document document = indexSearcher.doc(doc);
                Article article = ArticleUtils.documentToArticle(document);
                list.add(article);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
