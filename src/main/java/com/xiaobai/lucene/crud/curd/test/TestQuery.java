package com.xiaobai.lucene.crud.curd.test;

import com.xiaobai.lucene.crud.curd.util.LuceneUtils;
import com.xiaobai.lucene.crud.curd.util.Type;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestQuery {

    static {
        try {
            //static块可以初始化后定义的static域（类加载的init过程），但不可操作（Illegal forward reference,非法引用前置），因为类加载的初始化过程尚未完成
            indexWriter = LuceneUtils.getIndexWriter();
            indexSearcher = LuceneUtils.getIndexSearcher();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static IndexWriter indexWriter;

    private static IndexSearcher indexSearcher;

    private Query query;

    private Query buildQuery(Type type) throws ParseException {
        switch (type) {
            case TERM:
                /**
                 *  第一种查询，TermQuery
                 *  这是关键字查询
                 *  如果按照author查，因为author没有分词，所以查"马化腾"可以查询出来
                 *  如果按照content查，因为content分词了，如果是单字分词器，只能通过某一个字查出来，比如"中"
                 */
//                return new TermQuery(new Term("content","中"));//无法支持查"中国"
                return new TermQuery(new Term("author","陈驰1"));//因为author没有分词，是按整体查，所以无法支持查"陈驰",只能完整精确匹配
            case PARSER:
                /**
                 *  第二种查询：字符串搜索..
                 *  使用查询字符串：QueryParser+ MultiFieldQueryParser的查询方式
                 *  1、QueryParser：只在一个字段中查询
                 *  2、MultiFieldQueryParser：可以在多个字段查询
                 *  用来查询可以分词的字段，只要你输入的一段文本中包含分词，就会检索出来
                 */
                String[] fields={"content","title"};//可匹配所有这两个字段中有一个带"第一"的，两个字段都有"第一"的相关度高，排名靠前
                QueryParser queryParser=new
                        MultiFieldQueryParser(LuceneUtils.getMatchVersion(),fields,LuceneUtils.getAnalyzer());
                return queryParser.parse("第一");
            case ALL:
                /**
                 *  第三种查询：查询所有..
                 */
                return new MatchAllDocsQuery();
            case RANGE:
                /**
                 *  第四种查询：范围查询，可以使用此查询来替代过滤器...
                 */
                // 我们完成一种需求有两种方式，我们推荐用这种...性能比filter要高
                return NumericRangeQuery.newIntRange("id", 1, 26, true, true);//闭区间，结果包含两端
            case REGEX:
                /**
                 *  第五种查询：通配符。。。
                 */
                // ?代表单个任意字符，* 代表多个任意字符...
                return new WildcardQuery(new Term("content", "第*"));//中文搜索只支持单个字加*，无法匹配多个字加*
            case FUZZY:
                /**
                 *  第六种查询：模糊查询..。。。
                 *  1:需要根据查询的条件
                 *  2:最大可编辑数 取值范围0,1,2 允许我的查询条件的值，可以错误（或缺少）几个字符...
                 */
                return new FuzzyQuery(new Term("author", "新增记录：陈"), 1);//可匹配author为"新增记录：陈驰"，如果条件为"新增记录"，因为缺少字符多于1，故无法匹配
            case PHRASE:
                /**
                 * 第七种查询:短语查询
                 * 所有查询短语必须查询同一字段，否则：java.lang.IllegalArgumentException: All phrase terms must be in the same field: :一
                 */
                PhraseQuery query=new PhraseQuery();
                //(1)直接指定角标...
//                query.add(new Term("title","第"),2);
//                query.add(new Term("title","一"),3);//可匹配title为"天下第一",无法匹配"第一"
                // (2)设置两个短语之间的最大间隔数...
                //设置间隔数范围越大，它被匹配的结果就越多，性能也就越慢..
                query.add(new Term("title","第"));
                query.add(new Term("title","一"));
                query.setSlop(0);//可匹配title为"天下第一"、"第一"，后者相关度高，排名靠前
//                query.add(new Term("title","天"));
//                query.add(new Term("title","一"));//可匹配title为"天下第一",无法匹配"第一"
//                query.setSlop(2);//设置<2时无法匹配
                return query;
            case BOOLEAN:
                /**
                 * 第八种查询:布尔查询
                 */
                BooleanQuery queryB = new BooleanQuery();//变量queryB名称不允许与上文query重复
                // id 1~10
//                Query query1 = NumericRangeQuery.newIntRange("id", 1, 10, true, true);
//                Query query2 = NumericRangeQuery.newIntRange("id", 0, 15, true, true);//查不到id为0的
//                Query query1 = NumericRangeQuery.newIntRange("id", 0, 10, true, true);
//                Query query2 = NumericRangeQuery.newIntRange("id", 0, 15, true, true);//可以查到0-10的
                Query query1 = NumericRangeQuery.newIntRange("id", 0, 15, true, true);
                Query query2 = NumericRangeQuery.newIntRange("id", 2, 6, true, true);//仍可查到0-15
                // 必须满足第一个条件...
                queryB.add(query1, BooleanClause.Occur.MUST);
                // 可以满足第二个条件
                queryB.add(query2, BooleanClause.Occur.SHOULD);
                return queryB;
            default:
                return new TermQuery(new Term("content","中"));
        }
    }

    @Before
    public void initQuery() throws ParseException {//修改Query类型
//        query = buildQuery(Type.BOOLEAN);
//        query = buildQuery(Type.ALL);
//        query = buildQuery(Type.FUZZY);
//        query = buildQuery(Type.PARSER);
//        query = buildQuery(Type.PHRASE);
//        query = buildQuery(Type.RANGE);
//        query = buildQuery(Type.REGEX);
//        query = buildQuery(Type.TERM);
        query = buildBooleanQuery();
    }

    @Test
    public void testQuery() throws IOException {
        TopDocs topDocs = indexSearcher.search(query, 100);
        System.out.println("总记录数：" + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println("相关度得分："+scoreDoc.score);
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(document.get("content"));
            System.out.println(document.get("author"));
            System.out.println(document.get("link"));
        }
    }

    /**
     * 模拟京东高级搜索
     * @return
     * @throws ParseException
     */
    private Query buildBooleanQuery() throws ParseException {
        //封装查询条件(使用BooleanQuery对象，连接多个查询条件)
        BooleanQuery query = new BooleanQuery();

        //条件一（所属单位）
        //词条查询
        TermQuery query1 = new TermQuery(new Term("content", "中"));//无法匹配"中国",只能单个字匹配
        //Occur.SHOULD相当于or
        query.add(query1, BooleanClause.Occur.MUST);//Occur.MUST相当于sql语句的and

        //条件二（图纸类别）
        //词条查询
        Query query2 = NumericRangeQuery.newIntRange("id", 1, 10, true, true);
        query.add(query2, BooleanClause.Occur.MUST);

        //条件三（文件名称和文件描述）
        //多个字段进行检索的时候，查询使用QueryPaser
        //要是直接new QueryParser()，也可以，但是只能查询一个字段
        // select * from table where title=? or content=?
        QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_CURRENT,new String[]{"title","content"},LuceneUtils.getAnalyzer());
        Query query3 = queryParser.parse("第一");//匹配两个字段中有一个带"第一"的
//        QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_CURRENT,new String[]{"author"},LuceneUtils.getAnalyzer());
//        Query query3 = queryParser.parse("陈驰1");//无法使用MultiFieldQueryParser匹配没有分词的author字段，即使完全精确匹配也无法匹配上，即使只匹配这一个字段也无法匹配上，原因待查
//        QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_CURRENT,new String[]{"title","content"},LuceneUtils.getAnalyzer());
//        Query query3 = queryParser.parse("天下");
        query.add(query3, BooleanClause.Occur.MUST);//相当于sql语句的and
        return query;
    }

    public static IndexWriter getIndexWriter() {
        return indexWriter;
    }
    public static IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }
}
