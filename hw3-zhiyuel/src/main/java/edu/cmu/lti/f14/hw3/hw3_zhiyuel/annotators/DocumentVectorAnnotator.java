package edu.cmu.lti.f14.hw3.hw3_zhiyuel.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_zhiyuel.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_zhiyuel.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_zhiyuel.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Document doc = (Document) iter.get();
      // every line in meta data is a document
      // among them rel=99 is a query document
      
      
      List<Token> a=new ArrayList<Token>();
      createTermFreqVector(jcas, doc,a);
      
      FSList fsTokenList =Utils.fromCollectionToFSList(jcas,a);
      //update the token list in cas??????
      doc.setTokenList(fsTokenList);
//      doc.setTokenList(null);
//      System.out.println("===================");
      
    }

  }
  /**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   *
   * @param doc input text
   * @return    a list of tokens.
   */

  List<String> tokenize0(String doc) {
    List<String> res = new ArrayList<String>();
    
    for (String s: doc.split("\\s+"))
      res.add(s);
    return res;
  }

  /**
   * 
   * @param jcas
   * @param doc
   */

  private void createTermFreqVector(JCas jcas, Document doc, List<Token> aToken) {

    String docText = doc.getText();
    int id = doc.getQueryID();
    int rel = doc.getRelevanceValue();
    
    // TO DO: construct a vector of tokens and update the tokenList in CAS
    List<String> arr=tokenize0(docText);
    
    
   // String[] spam = docText.split(" ");
    Map<String, Integer> freq = new HashMap<String, Integer>();
    for (int i = 0; i < arr.size(); i++) {
      //change to lowercase for the first capital letter!
//      if (!freq.containsKey(spam[i].toLowerCase())) {
//        freq.put(spam[i].toLowerCase(), 1);
//      } else {
//        for (Map.Entry<String, Integer> m : freq.entrySet()) {
//          if (spam[i].equals(m.getKey())) {
//            freq.put(m.getKey(), (int) m.getValue() + 1);
//          }
//        }
//      }
      if(!freq.containsKey(arr.get(i))){
        freq.put(arr.get(i), 1);
      }
      else{
        for (Map.Entry<String, Integer> m : freq.entrySet()) {
        if (arr.get(i).equals(m.getKey())) {
          freq.put(m.getKey(), (int) m.getValue() + 1);
        }
      }
      }
    }

    for (Map.Entry<String, Integer> m : freq.entrySet()) {
//      System.out.println(id+"|"+rel+"|"+m.getKey() + "," + m.getValue());
      Token t=new Token(jcas);
      t.setText(m.getKey());
      t.setFrequency(m.getValue());
      aToken.add(t);
      t.addToIndexes();
    }

  }

}
