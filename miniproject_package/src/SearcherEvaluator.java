//Name: 
//Section: 
//ID: 

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class SearcherEvaluator {
    private List<Document> queries = null;                //List of test queries. Each query can be treated as a Document object.
    private Map<Integer, Set<Integer>> answers = null;    //Mapping between query ID and a set of relevant document IDs

    public List<Document> getQueries() {
        return queries;
    }

    public Map<Integer, Set<Integer>> getAnswers() {
        return answers;
    }

    /**
     * Load queries into "queries"
     * Load corresponding documents into "answers"
     * Other initialization, depending on your design.
     *
     * @param corpus
     */
    public SearcherEvaluator(String corpus) {
        String queryFilename = corpus + "/queries.txt";
        String answerFilename = corpus + "/relevance.txt";

        //load queries. Treat each query as a document.
        this.queries = Searcher.parseDocumentFromFile(queryFilename);
        this.answers = new HashMap<Integer, Set<Integer>>();
        //load answers
        try {
            List<String> lines = FileUtils.readLines(new File(answerFilename), "UTF-8");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\t");
                Integer qid = Integer.parseInt(parts[0]);
                String[] docIDs = parts[1].trim().split("\\s+");
                Set<Integer> relDocIDs = new HashSet<Integer>();
                for (String docID : docIDs) {
                    relDocIDs.add(Integer.parseInt(docID));
                }
                this.answers.put(qid, relDocIDs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns an array of 3 numbers: precision, recall, F1, computed from the top *k* search results
     * returned from *searcher* for *query*
     *
     * @param query
     * @param searcher
     * @param k
     * @return
     */
    public double[] getQueryPRF(Document query, Searcher searcher, int k) {
        /*********************** YOUR CODE HERE *************************/
        int qid = query.getId();
        Set<Integer> relevantdoc = this.answers.get(qid);
        Set<Integer> realrelevantdoc = this.answers.get(qid);

        List<SearchResult> searchResults = searcher.search(query.getRawText(), k);
        Set<Integer> searchResultId = new HashSet<Integer>(); 
        for (SearchResult result : searchResults) {
            searchResultId.add(result.getDocument().getId());
        }

        relevantdoc.retainAll(searchResultId);

        double precision = relevantdoc.size() / (double) searchResults.size();
        double recall = relevantdoc.size() / (double) realrelevantdoc.size();

        double f = 0;
        if (precision + recall != 0) {
            f = (2 * precision * recall) / (precision + recall);
        }
        return new double[]{precision, recall, f};
        /****************************************************************/
    }

    /**
     * Test all the queries in *queries*, from the top *k* search results returned by *searcher*
     * and take the average of the precision, recall, and F1.
     *
     * @param searcher
     * @param k
     * @return
     */
    public double[] getAveragePRF(Searcher searcher, int k) {
        double totalprecision = 0;
        double totalrecall = 0;
        double totalf = 0;

        /*********************** YOUR CODE HERE *************************/
        for (Document query : queries) {
            double[] a = getQueryPRF(query, searcher, k);
            totalprecision += a[0];
            totalrecall += a[1];
            totalf += a[2];
        }
        double avgprecision = totalprecision / (double) queries.size();
        double avgrecall = totalrecall / (double) queries.size();
        double avgf = totalf / (double) queries.size();

        return new double[]{avgprecision, avgrecall, avgf};
        /****************************************************************/
    }
}
