package com.xiaobai.lucene.crud.curd.test;

import com.xiaobai.lucene.crud.curd.domain.Article;
import com.xiaobai.lucene.crud.curd.util.LuceneUtils;
import com.xiaobai.lucene.crud.curd.util.Type;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
//        query = buildBooleanQuery();
        query = buildWeightingQuery();
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

    /**
     * 权重索引
     */
    @Test
    public void testAddWeightingIndex() {//每次不是从头创建索引，而是在已有的索引目录增量添加
        try {//如果要重新建立索引，可用更新操作，先删除所有此前的，再从头创建添加
            indexWriter.addDocument(buildWeighting());
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Document buildWeighting() {
        Document document = new Document();
        IntField idField = new IntField("id", 100, Field.Store.YES);
        //StringField不进行分词（整体算一个）--------------------------------------->不分词的按整体查，所以可以查到"马化腾",注意这个！！！
        StringField authoField = new StringField("author", "xiaobai", Field.Store.YES);
        StringField linkField = new StringField("link", "www.baidu.com", Field.Store.YES);
        //TextField进行分词
        TextField titleField = new TextField("title", "国庆节，最近比较上火，深悟人生，特立箴言，还是努力不够。往者不可谏，来者犹可追。", Field.Store.YES);
        //==============================================
        //注意：这里可以添加权重值，默认是1f，添加之后检索的时候排名就会靠前
        titleField.setBoost(4f);
        TextField contentField = new TextField("content", "国庆节，农历8月23，离职休养月余。\n" +
                "\n" +
                "睹大学后生博客学识之高度，为人做学问之器量效率，学识深度与广博，人生经历与认知，深以为不如。\n" +
                "\n" +
                "忆己年余病笃，怠惰，迷拙，蹉跎，又弗如远甚。\n" +
                "\n" +
                "以至今日亦倦怠慵懒不思改悟，无忧患，徒慰己自欺，无才而傲物，以绝知耻而后勇之心，遂不能务实有效，奋起直追。\n" +
                "\n" +
                "深悟人生，特立箴言：\n" +
                "\n" +
                " \n" +
                "\n" +
                "\n" +
                "首先，承认天外有天，天生见不清越不过的高山\n" +
                "\n" +
                "\n" +
                "其次，承认自己的智力水平，懒惰，和努力程度\n" +
                "\n" +
                "\n" +
                "再次，肯定和坚守自己的努力，悟性和持之以恒\n" +
                "\n" +
                "\n" +
                "最后，放弃嫉妒，勉力执着和自我安慰，快乐知足地奋斗和生活，温暖，自信，正能量！\n" +
                "\n" +
                "\n" +
                "往者不可谏，来者犹可追。\n" +
                "\n" +
                " \n" +
                "\n" +
                "西元2018.10.2，病笃家中，键盘手书。", Field.Store.YES);
        contentField.setBoost(8f);
        document.add(idField);
        document.add(authoField);
        document.add(linkField);
        document.add(titleField);
        document.add(contentField);
        return document;
    }

    private Query buildWeightingQuery() throws ParseException {
        //封装查询条件(使用BooleanQuery对象，连接多个查询条件)
        BooleanQuery query = new BooleanQuery();

        //条件一（所属单位）
        //词条查询
        TermQuery query1 = new TermQuery(new Term("content", "中"));//无法匹配"中国",只能单个字匹配
        //Occur.SHOULD相当于or
        query.add(query1, BooleanClause.Occur.MUST);//Occur.MUST相当于sql语句的and

        //条件二（图纸类别）
        //词条查询
        Query query2 = NumericRangeQuery.newIntRange("id", 1, 100, true, true);
        query.add(query2, BooleanClause.Occur.MUST);

        //条件三（文件名称和文件描述）
        //多个字段进行检索的时候，查询使用QueryPaser
        //要是直接new QueryParser()，也可以，但是只能查询一个字段
        // select * from table where title=? or content=?
        QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_CURRENT,new String[]{"title","content"},LuceneUtils.getAnalyzer());
        Query query3 = queryParser.parse("国家");//匹配两个字段中有一个带"第一"的
        query.add(query3, BooleanClause.Occur.MUST);//相当于sql语句的and
        return query;
    }

    /**
     * 排序
     * @throws IOException
     */
    @Test
    public void testSortQuery() throws IOException {
        Sort sort = new Sort();
        // 升序
        SortField sortField=new SortField("id", SortField.Type.INT);
        // 降序
//        SortField sortField = new SortField("id", SortField.Type.INT,true);
        // 设置排序的字段...
        sort.setSort(sortField);
        TopDocs topDocs = indexSearcher.search(query, 100, sort);
        System.out.println("总记录数：" + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println("相关度得分："+scoreDoc.score);//前面设置了排序，相关度失效，这里返回NaN
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(document.get("content"));
            System.out.println(document.get("author"));
            System.out.println(document.get("link"));
        }
    }

    /**
     * 过滤
     * @throws IOException
     */
    @Test
    public void testFilterQuery() throws IOException {
        /**
         * 1:需要根据那个字段进行过滤
         * 2:字段对应的最小值
         * 3:字段对应的最大值
         * 4:是否包含最小值
         * 5：是否包含最大值...
         */
        // filter 是一个抽象类，定义不同的filter 相当于是不同的过滤规则...
        Filter filter = NumericRangeFilter
                .newIntRange("id", 8, 100, false, true);
        TopDocs topDocs = indexSearcher.search(query, filter, 100);
        System.out.println("总记录数：" + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println("相关度得分："+scoreDoc.score);//不影响相关度，仍按相关度高低降序展示
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(document.get("content"));
            System.out.println(document.get("author"));
            System.out.println(document.get("link"));
        }
    }

    /**
     * 高亮
     * 查询是通过IndexSearch提供的(分页)
     * @throws IOException
     */
    @Test
    public void testHighlighterQuery() throws IOException {
//        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
        Analyzer analyzer = new IKAnalyzer();
        int start = 0;
        int count = 100;
        try {
            IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher();

            // ===========================================================
            // 这里是第二种query方式，不是termQuery
            QueryParser queryParser = new MultiFieldQueryParser(
                    Version.LUCENE_CURRENT, new String[] { "title",
                    "content" }, analyzer);
            Query query = queryParser.parse("国庆节");//IKAnalyzer无法查询"国庆"，可查询"国庆节"，只查出了带有"国庆节"的，并高亮整个词（<font color='red'><b>国庆节</b></font>）而不是每个字（<font color='red'><b>国</b></font><font color='red'><b>庆</b></font><font color='red'><b>节</b></font>），查不出带"国"的，是分词所致，"国庆"不是其分词，也不按单字分词
//            Query query = queryParser.parse("国庆");//StandardAnalyzer可以查询"国庆",是按字分词，带"国"的也被匹配了
            TopDocs topDocs = indexSearcher.search(query, 100);
            System.out.println("总记录数：" + topDocs.totalHits);

            /**
             * 添加设置文字高亮begin 使用lucene自带的高亮器进行高亮显示
             */
            // html页面高亮显示的格式化，默认是<b></b>
            Formatter formatter = new SimpleHTMLFormatter(
                    "<font color='red'><b>", "</b></font>");
            // 执行查询条件，因为高亮的值就是查询条件
            Scorer scorer = new QueryScorer(query);//将查询条件分词后全部高亮
            Highlighter highlighter = new Highlighter(formatter, scorer);

            // 设置文字摘要，此时摘要大小
            int fragmentSize = 100;
            Fragmenter fragmenter = new SimpleFragmenter(fragmentSize);
            highlighter.setTextFragmenter(fragmenter);
            /** 添加设置文字高亮end */
            // 表示返回的结果集
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            List<Article> list = new ArrayList<Article>();

            int min = Math.min(scoreDocs.length, start + count);
            for (int i = start; i < min; i++) {
                System.out.println("相关度得分：" + scoreDocs[i].score);
                // 获取查询结果的文档的惟一编号，只有获取惟一编号，才能获取该编号对应的数据
                int doc = scoreDocs[i].doc;
                // 使用编号，获取真正的数据
                Document document = indexSearcher.doc(doc);

                /** 获取文字高亮的信息 begin */
                // 获取文字的高亮，一次只能获取一个字段高亮的结果，如果获取不到，返回null值
                // 高亮之后的title
                // 注意：如果这个字段当中没有包含搜索关键字，你对这个字段的值进行高亮，返回的是null
                String title = highlighter.getBestFragment(//获取结果摘要
                        analyzer, "title",//这个分词器用于在摘要中分词查询关键词，将它们高亮
                        document.get("title"));//高亮和摘要都是在获取搜索结果之后的处理
                // 如果null表示没有高亮的结果，如果高亮的结果，应该将原值返回
                if (title == null) {
                    title = document.get("title");
                    if (title != null && title.length() > fragmentSize) {
                        // 截串，从0开始
                        title = title.substring(0, fragmentSize);
                    }
                }
                System.out.println("-------title:" + title);
                // 高亮之后的content
                // 注意：如果这个字段当中没有包含搜索关键字，你对这个字段的值进行高亮，返回的是null
                String content = highlighter.getBestFragment(
                        analyzer, "content",
                        document.get("content"));//获取摘要，将摘要赋值给内容，只打印摘要
                // 如果null表示没有高亮的结果，如果高亮的结果，应该将原值返回
                if (content == null) {
                    content = document.get("content");
                    if (content != null && content.length() > fragmentSize) {
                        // 截串，从0开始
                        content = content.substring(0, fragmentSize);
                    }
                }
                System.out.println("--------content:" + content);
                /** 获取文字高亮的信息 end */
                Article article = new Article();
                article.setId(Integer.parseInt(document.get("id")));
                article.setAuthor(document.get("author"));
                article.setLink(document.get("link"));
                article.setTitle(title);//高亮之后的
                article.setContent(content);//高亮之后的
                list.add(article);
            }
            for(Article article : list) {
                System.out.println(article.getId());
                System.out.println(article.getTitle());
                System.out.println(article.getContent());
                System.out.println(article.getAuthor());
                System.out.println(article.getLink());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IndexWriter getIndexWriter() {
        return indexWriter;
    }
    public static IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }
}
