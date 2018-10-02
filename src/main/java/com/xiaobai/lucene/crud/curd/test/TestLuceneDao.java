package com.xiaobai.lucene.crud.curd.test;

import java.io.IOException;
import java.util.List;

import com.xiaobai.lucene.crud.curd.dao.LuceneDao;
import com.xiaobai.lucene.crud.curd.domain.Article;
import org.junit.Test;

public class TestLuceneDao {
    private LuceneDao dao = new LuceneDao();

    @Test
    public void addIndex() {
        for (int i = 0; i <= 25; i++) {
            Article article = new Article();
            article.setId(i);
            article.setTitle("腾讯qq");
            article.setAuthor("马化腾");
            article.setContent("腾讯网(www.QQ.com)是中国浏览量最大的中文门户网站,是腾讯公司推出的集新闻信息、互动社区、娱乐产品和基础服务为一体的大型综合门户网站。腾讯网服务于全球华人...");
            article.setLink("http://www.qq.com/");
            dao.addIndex(article);//添加时设置了title字段权重高于content字段
        }
    }

    @Test
    public void findIndex() {
        String keywords = "第一";//按字分词查询，带"一"即可匹配，查询title和content两个字段，有一个字段有的即可匹配，其中title字段在添加时即被设置了高权重（查询时是否可设置按某字段高权重查询？）
        List<Article> list = dao.findIndex(keywords, 0, 130);
        for (Article article : list) {
            System.out.println(article.getId());
            System.out.println(article.getTitle());
            System.out.println(article.getContent());
            System.out.println(article.getAuthor());
            System.out.println(article.getLink());
        }
    }

    @Test
    public void deleteIndex(){//删除这条更新的记录后，再执行查询，查不到任何记录，再执行更新操作，发现又能查到这条记录了，说明了updateIndex是先删除后新增的逻辑，即使没有记录也一定会新增！！
        try {
            dao.deleteIndex("author", "陈驰");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateIndex(){//不断执行这个操作，发现查询到的记录数越来越多，说明并不是将title带qq的更新为这里的记录，而是每次删除所有title带qq的，新增一个下面的记录
        String fieldName = "title";
        String fieldValue = "qq";

        Article article = new Article();
        article.setId(1);
        article.setAuthor("陈驰");
        article.setLink("http://www.baidu.com");
        article.setTitle("天下第一");
        article.setContent("天下第一一一一一一");
        try {//1.删除了所有title中带"qq"的，也就是26条记录 2.按词条添加这条记录
            dao.updateIndex(fieldName, fieldValue, article);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //update测试：并不是将title带"qq"的均更新为新词条，而是删除所有title带"qq"的，再新增新词条
    //这里是查询前130条查出52条记录：26条title为"第一"的记录，26条title为"腾讯qq"的记录。执行这个操作，此后查询前130条，发现只有27条记录，下面这条更新的记录排名最前且权重最高，后面26条均为title为"第一"的记录，说明这里的逻辑是title带"qq"的26条记录被悉数删除，然后仅新增一条下面的记录
    @Test
    public void updateIndex1(){
        String fieldName = "title";
        String fieldValue = "qq";

        Article article = new Article();
        article.setId(1);
        article.setAuthor("陈驰1");
        article.setLink("http://www.baidu.com");
        article.setTitle("天下第一");//因为按"title","content"两个字段查询，且添加时"title"权重设置高，所以这里"title","content"两个字段均带有"第一"关键字的这条记录权重最高，打印权重值也说明了这一点
        article.setContent("天下第一一一一一一");
        try {//1.删除了所有title中带"qq"的，也就是26条记录 2.按词条添加这条记录
            dao.updateIndex(fieldName, fieldValue, article);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //高权重测试：执行deleteIndex删除所有记录，再执行上面的addIndex插入26条title为"腾讯qq"的记录，再执行这里的addIndex1插入26条title为"第一"的记录，因为插入时均设置title字段权重比content字段高（更新词条时也可设置，且每个词条可以单独设置成不同的权重值），所以查询时title为"第一"的均排在前面，且打印出的相关度得分高，因为查询前30条，所以查出记录数52，但只显示前30条，改为查询前100条则可全部显示
    @Test
    public void addIndex1() {
        for (int i = 0; i <= 25; i++) {
            Article article = new Article();
            article.setId(i);
            article.setTitle("第一");
            article.setAuthor("新增记录：陈驰");
            article.setContent("腾讯网(www.QQ.com)是中国浏览量最大的中文门户网站,是腾讯公司推出的集新闻信息、互动社区、娱乐产品和基础服务为一体的大型综合门户网站。腾讯网服务于全球华人...");
            article.setLink("http://www.qq.com/");
            dao.addIndex(article);
        }
    }
}
