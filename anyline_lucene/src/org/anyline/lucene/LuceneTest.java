/**
 * 
 */
/**
 * @author Administrator
 *
 */
package org.anyline.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneTest {
	public static void main(String[] args) throws IOException, ParseException {
		Analyzer analyzer = new StandardAnalyzer();

		// Store the index in memory:
		Directory directory = new RAMDirectory();
		// To store an index on disk, use this instead:
		// Directory directory = FSDirectory.open("/tmp/testindex");

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, config);

		String[] texts = new String[] {"温馨提示：如本文未解决您的问题或者其他方面的问题，请添加我们统一服务微信公众号"};
		for (String text : texts) {
			Document doc = new Document();
			doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
			writer.addDocument(doc);
		}

		writer.close();

		// Now search the index:
		DirectoryReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);

		QueryParser parser = new QueryParser("fieldname", analyzer);

		Query query = parser.parse("stupid");
		// if you parse("the")，then hits.length=0,because "the" is
		// stopWord,others like "to" "be",also stopWord
		ScoreDoc[] hits = searcher.search(query, 5).scoreDocs;
		System.out.println(hits.length);

		// Iterate through the results:
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = searcher.doc(hits[i].doc);
			System.out.println(hitDoc.get("fieldname"));
		}
		reader.close();
		directory.close();
	}

}