/* 
 * YOUR LICENSE HEADER GOES HERE
 */


package mypackage;


import gate.*;
import gate.api.AbstractDocumentProcessor;
import gate.creole.metadata.*;
import gate.util.GateRuntimeException;
import java.util.List;
import org.apache.log4j.Logger;

@CreoleResource(name = "MyPluginPr",
        helpURL = "https://somehost.com/where/the/prdocu/is.html",
        comment = "A short description of what the plugin does.")
public class MyPluginPr  extends AbstractDocumentProcessor {

  private static final long serialVersionUID = 1L;
  
  protected String inputASName = "";
  @RunTime
  @Optional
  @CreoleParameter(
          comment = "Input annotation set",
          defaultValue = "")
  public void setInputAnnotationSet(String ias) {
    inputASName = ias;
  }

  public String getInputAnnotationSet() {
    return inputASName;
  }
  
  
  protected String inputType = "";
  @RunTime
  @CreoleParameter(
          comment = "The input annotation type",
          defaultValue = "Token")
  public void setInputAnnotationType(String val) {
    this.inputType = val;
  }

  public String getInputAnnotationType() {
    return inputType;
  }

  private int nDocs = 0;
  private int nAnns = 0;

  
  /**
   * What to do right after the PR got created and initialized.
   */
  @Override
  public void afterCreate() {
    
  }
  
  /**
   * What to do before the first document is processed.
   */
  @Override
  protected void beforeFirstDocument(Controller ctrl) {
    nDocs = 0;
    nAnns = 0;
  }
    
  private Logger logger = Logger.getLogger(AbstractDocumentProcessor.class.getCanonicalName());  

  /**
   * Process the document.
   *
   * @return 
   */
  @Override
  public List<Document> process(Document document) {

    // Implement the processing for each document here.
    // This short example code just counts the number of annotations with the 
    // type and in the set specified as parameters that occur in the document.
    
    AnnotationSet inputAS;
    if (inputASName == null || inputASName.isEmpty()) {
      inputAS = document.getAnnotations();
    } else {
      inputAS = document.getAnnotations(inputASName);
    }

    AnnotationSet inputAnns = null;
    if (inputType == null || inputType.isEmpty()) {
      throw new GateRuntimeException("Input annotation type must not be empty!");
    }
    inputAnns = inputAS.get(inputType);

    // this will show/replace a progress message in the GATE GUI
    fireStatusChanged("MyPluginPr: running on " + document.getName() + "...");

    int thisDocumentCount = 0;
    for(Annotation ann : inputAnns) {
      thisDocumentCount += 1;
    }
   
    nDocs += 1;
    nAnns += thisDocumentCount; 
    
    
    fireProcessFinished();
    fireStatusChanged("MyPluginPr: processing complete!");
    return documentList(document);
  }
  

  @Override
  protected void afterLastDocument(Controller ctrl, Throwable t) {
    showResults();
  }

  @Override
  protected void processingFinished(Controller ctrl, Throwable t) {
    showResults();
  }

  private void showResults() {
    logger.info("Documents as counted by the base class: "+getDocumentsProcessed());
    logger.info("Number of documents: "+nDocs);
    logger.info("Number of annotations: "+nAnns);
  }
  

} // class MyPluginPr
