/*
 * LICENSE AND COPYRIGHT HEADER
 */

package mypackage;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.LanguageAnalyser;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import java.io.File;
import org.junit.Test;
import gate.Utils;
import gate.creole.ExecutionException;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 */
public class TestMyPluginPr {
  
  /**
   * To test the plugin, we need to initialize GATE and load the plugin first.
   * 
   * @throws GateException 
   */
  @BeforeClass
  public static void init() throws GateException {
    gate.Gate.init();
    // if needed, a plugin that comes with GATE can be loaded here by name as well
    // gate.Utils.loadPlugin("ANNIE");
    
    // Load our plugin
    Utils.loadPlugin(new File("."));
  }
  
  @Test
  public void testPluginApi1() throws ResourceInstantiationException, ExecutionException {
    // create an instance of the PR
    MyPluginPr mypr = 
            (MyPluginPr)Factory.createResource(
            "mypackage.MyPluginPr",  // the full name of the resource class 
            Utils.featureMap(),  // init parameters, if any
            Utils.featureMap(),  // resource features, if any
            "myPluginPr1");           // name to give to the created PR
    
    // create a temporary document
    Document doc1 = Factory.newDocument("some text for the document");
    // add a few annotations of type "Token" to the document in the default
    // annotation set
    AnnotationSet defAS = doc1.getAnnotations();
    Utils.addAnn(defAS, 0, 4, "Token", Utils.featureMap()); 
    Utils.addAnn(defAS, 6, 10, "Token", Utils.featureMap());
    // now use the PR API to count the Tokens and check if it counts correctly
    mypr.setDocument(doc1);
    mypr.execute();
    assertEquals(1,mypr.getNumberDocs());
    assertEquals(2,mypr.getNumberAnnotations());
    // check if resetting the counters works
    mypr.resetCounters();
    assertEquals(0,mypr.getNumberDocs());
    assertEquals(0,mypr.getNumberAnnotations());
    
    // Change the runtime parameter for the annotation type to "SomeAnn"
    // count again and make sure we did not count the annotations, but 
    // only the documents
    mypr.setInputAnnotationType("SomeAnn");
    mypr.execute();
    assertEquals(1,mypr.getNumberDocs());
    assertEquals(0,mypr.getNumberAnnotations());
    // actually add one annotation of that type to the document
    Utils.addAnn(defAS, 0, 4, "SomeAnn", Utils.featureMap()); 
    // count again and check
    mypr.resetCounters();
    mypr.execute();
    assertEquals(1,mypr.getNumberDocs());
    assertEquals(1,mypr.getNumberAnnotations());
    
  }
    
  
}
