package com.xiaobai.lucene.crud.curd.util;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneUtils {

    public static Directory d = null;
    public static IndexWriterConfig conf = null;
    public static Version matchVersion = null;
    public static Analyzer analyzer = null;

    static{
        try {
            d = FSDirectory.open(new File(Constant.FILEURL));
            matchVersion = Version.LUCENE_44;
            //注意：该分词器是单字分词
            analyzer = new StandardAnalyzer(matchVersion);

            conf = new IndexWriterConfig(matchVersion, analyzer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return 返回版本信息
     */
    public static Version getMatchVersion() {
        return matchVersion;
    }

    /**
     *
     * @return 返回分词器
     */
    public static Analyzer getAnalyzer() {
        return analyzer;
    }

    /**
     *
     * @return 返回用于操作索引的对象
     * @throws IOException
     */
    public static IndexWriter getIndexWriter() throws IOException{
        IndexWriter indexWriter = new IndexWriter(d, conf);
        return indexWriter;
    }

    /**
     *
     * @return 返回用于读取索引的对象
     * @throws IOException
     */
    public static IndexSearcher getIndexSearcher() throws IOException{
        IndexReader r = DirectoryReader.open(d);
        IndexSearcher indexSearcher = new IndexSearcher(r);
        return indexSearcher;
    }
}
