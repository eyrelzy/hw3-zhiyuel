package edu.cmu.lti.f14.hw3.hw3_zhiyuel.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_zhiyuel.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_zhiyuel.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_zhiyuel.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

  /** query id number **/
  public ArrayList<Integer> qIdList;

  /** query and text relevant values **/
  public ArrayList<Integer> relList;

  public ArrayList<Integer> rankList;

  public List<Double> coslist;

  public ArrayList<Map<String, Integer>> queryVectorlist;

  public ArrayList<Map<String, Integer>> docVectorlist;

  private int lines = 0, gold = 0,cnt=1;
  private double goldcos = 0;

  public void initialize() throws ResourceInitializationException {

    qIdList = new ArrayList<Integer>();

    relList = new ArrayList<Integer>();
    rankList = new ArrayList<Integer>();
    coslist = new ArrayList<Double>();
    queryVectorlist = new ArrayList<Map<String, Integer>>();
    docVectorlist = new ArrayList<Map<String, Integer>>();

  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {
System.out.println("Retrival evaluator...");
    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
    
//    if (it.hasNext()) {
//      Document doc = (Document) it.next();
//      cnt++;
//    }
//    log(cnt);
    it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();
      int queryid = doc.getQueryID();
      int rel = doc.getRelevanceValue();
      // Make sure that your previous annotators have populated this in CAS
      FSList fsTokenList = doc.getTokenList();
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
      Map<String, Integer> queryVector = new HashMap<String, Integer>();
      Map<String, Integer> docVector = new HashMap<String, Integer>();
      if (rel == 99) {
        for (Token t : tokenList) {
          queryVector.put(t.getText(), t.getFrequency());
        }
        queryVectorlist.add(queryVector);
      } else {
        for (Token t : tokenList) {
          docVector.put(t.getText(), t.getFrequency());
        }
        double cos = computeCosineSimilarity(queryVectorlist.get(queryid - 1), docVector);
        coslist.add(cos);

        if (rel == 1) {
          gold = lines;
          goldcos = cos;
          System.out.println("goldcos"+goldcos);
        }
      }
      
      if (qIdList.size() >= 1 && qIdList.get(qIdList.size() - 1) != queryid) {
//        System.out.println("lines:"+lines);
        sortSimilarity(coslist);
        log(coslist);
        int rank=findRank(coslist,goldcos);
        rankList.add(rank);
        coslist = new ArrayList<Double>();
      }

      qIdList.add(doc.getQueryID());// 有啥用？？？
      relList.add(doc.getRelevanceValue());

      // Do something useful here
      lines++;
    }
    //log(coslist);

  }

  private void log(List<Double> arr) {
    for (double d : arr) {
      System.out.println(d + "");
    }
  }
  private void log(int a) {
      System.out.println(a + "");
  }
  private void log(double a) {
    System.out.println(a + "");
  }
  private void log(String a) {
    System.out.println(a);
  }

  /**
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    sortSimilarity(coslist);
    log(coslist);
    int rank=findRank(coslist,goldcos);
    rankList.add(rank);
    coslist = new ArrayList<Double>();
    // TODO :: compute the cosine similarity measure

    // TODO :: compute the rank of retrieved sentences

    for (Integer i : rankList) {
      System.out.println("" + i);
    }

    // TODO :: compute the metric:: mean reciprocal rank
    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
  }

  private int findRank(List<Double> cos, double goldcos) {
    for (int i = 0; i < cos.size(); i++) {
      if (goldcos == cos.get(i))
        return i+1;
    }
    return -1;
  }

  private List<Double> sortSimilarity(List<Double> cos) {
    for (int i = 0; i < cos.size(); i++) {
      for (int j = i + 1; j < cos.size(); j++) {
        if (cos.get(i) <= cos.get(j)) {
          swap(cos, i, j);
        }
      }
    }
    return cos;
  }

  private void swap(List<Double> cos, int i, int j) {
    double temp = cos.get(i);
    cos.set(i, cos.get(j));
    cos.set(j, temp);
  }

  /**
   * 
   * @return cosine_similarity
   */
  // pass
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    double cosine_similarity = 0.0;

    // TODO :: compute cosine similarity between two sentences
    for (Map.Entry<String, Integer> qm : queryVector.entrySet()) {
      String key = qm.getKey();
      if (!docVector.containsKey(key)) {
        docVector.put(key, 0);
      }
    }
    for (Map.Entry<String, Integer> dm : docVector.entrySet()) {
      String key = dm.getKey();
      if (!queryVector.containsKey(key)) {
        queryVector.put(key, 0);
      }
    }
    // System.out.println(docVector.size()+"|"+queryVector.size());
    for (Map.Entry<String, Integer> dm : docVector.entrySet()) {
      for (Map.Entry<String, Integer> qm : queryVector.entrySet()) {
        if (dm.getKey().equals(qm.getKey()))
          cosine_similarity += dm.getValue() * qm.getValue();
      }
    }
    double lenq = compute_eucli(queryVector);
    double lend = compute_eucli(docVector);
    // System.out.println("cosine_similarity"+cosine_similarity+",lenq"+lenq+",lend"+lend);
    cosine_similarity = cosine_similarity / (lenq * lend);
    // System.out.println("cosine_similarity"+cosine_similarity+",lenq"+lenq+",lend"+lend);
    return cosine_similarity;
  }

  // pass

  private double compute_eucli(Map<String, Integer> vector) {
    double len = 0;
    for (Map.Entry<String, Integer> v : vector.entrySet()) {
      int val = v.getValue();
      len += Math.pow(val, 2);
    }
    return Math.sqrt(len);
  }

  /**
   * 
   * @return mrr
   */
  private double compute_mrr() {
    double metric_mrr = 0.0;

    // TODO :: compute Mean Reciprocal Rank (MRR) of the text collection

    return metric_mrr;
  }

}
