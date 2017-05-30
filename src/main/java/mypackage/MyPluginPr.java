/* 
 * YOUR LICENSE HEADER GOES HERE
 */
package mypackage;


import gate.*;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.*;
import gate.util.GateRuntimeException;
import org.apache.log4j.Logger;

/**
 * Template PR file.
 * 
 * @author Johann Petrak
 */
@CreoleResource(name = "MyPluginPr",
        helpURL = "https://somehost.com/where/the/prdocu/is.html",
        comment = "A short description of what the plugin does.")
public class MyPluginPr  
        extends AbstractLanguageAnalyser 
        // only implement these if they are actually needed!
        implements 
        ControllerAwarePR    // only add if needed!
        // , Benchmarkable   // only when Benchmarking is needed
{

  private static final long serialVersionUID = 1L;
  
  protected String inputASName = "";
  @RunTime
  @Optional
  @CreoleParameter(
          comment = "Input annotation set",
          defaultValue = "")
  public void setInputASName(String ias) {
    inputASName = ias;
  }

  public String getInputASName() {
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

  // Use a logger instead of System.out / System.err to make the output of the
  // PR more configurable.
  private final Logger logger = 
          Logger.getLogger(getClass().getCanonicalName());  
  
  
  // fields needed for the PR
  private int nDocs = 0;
  private int nAnns = 0;

  
  /**
   * Code to run once when/after the instance is created.
   * Should be used sparingly and for one-time setup that only depends 
   * on init time parameters. Can be removed if not needed.
   * @return the resource
   * @throws ResourceInstantiationException if instance cannot be created
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    logger.info(getClass().getName()+": instance has been created");
    return this;
  }
  
  /**
   * Code to run if an existing instance gets re-initialized.
   * Rarely needed and can be removed if not needed.
   */
  @Override
  public void reInit() {
    logger.info(getClass().getName()+": instance has been re-initialized");
  }
  
  /**
   * Code to run before the instance gets destroyed.
   * Rarely needed and can be removed if not needed.
   */
  @Override
  public void cleanup() {
    logger.info(getClass().getName()+": cleaning up instance");
  }
  
  
  // PR-local API methods...
  
  public void resetCounters() {
    nDocs = 0;
    nAnns = 0;
  }
  
  public int getNumberDocs() {
    return nDocs;
  }
  
  public int getNumberAnnotations() {
    return nAnns;
  }

  /**
   * Process the document.
   *
   * The field document is predefined and set by the caller to the document
   * to be processed.
   * <p>
   * This uses the PR-local API methods.
   * 
   * @throws gate.creole.ExecutionException if execution gets aborted.
   */
  @Override
  public void execute() throws ExecutionException {

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
    fireStatusChanged(getClass().getName()+": running on " + document.getName() + "...");

    int thisDocumentCount = 0;
    thisDocumentCount += inputAnns.size();
    // In order to iterate over Annotations use something like
    //for(Annotation ann : inputAnns) {
    //  // process the annotation, however cannot remove it inside a for loop
    //}
   
    nDocs += 1;
    nAnns += thisDocumentCount; 
    
    // The PR should react to the request of being interrupted, which should be
    // handled by throwing an exception (which will also interrupt the pipeline).
    // The interrupt flag is not automatically reset, so we reset it before throwing
    // the exception so that the pipeline can be restarted later.
    if(isInterrupted()) {
      interrupted = false;
      throw new ExecutionException(getClass().getName()+"Has been interrupted");
    }
    
    fireProcessFinished();
    fireStatusChanged(getClass().getName()+": processing complete!");
  }
  
  // This should only be defined if ControllerAwarePR needs to be implemented.
  // The method will get invoked when a pipeline is started. 
  @Override
  public void controllerExecutionStarted(Controller ctrl) {
    logger.info(getClass().getName()+" started");
    resetCounters();
  }

  // This should only be defined if ControllerAwarePR needs to be implemented.
  // The method will get invoked when a pipeline is finishing.
  @Override
  public void controllerExecutionFinished(Controller ctrl) {
    logger.info(getClass().getName()+" finished normally");
    logResults();
  }

  // This should only be defined if ControllerAwarePR needs to be implemented.
  // The method will get invoked when a pipeline is aborted due to an exception.
  @Override
  public void controllerExecutionAborted(Controller ctrl, Throwable t) {
    logger.info(getClass().getName()+" aborted");
    logResults();
  }

  private void logResults() {
    logger.info("Number of documents: "+getNumberDocs());
    logger.info("Number of annotations: "+getNumberAnnotations());
  }
    
  

} // class MyPluginPr
