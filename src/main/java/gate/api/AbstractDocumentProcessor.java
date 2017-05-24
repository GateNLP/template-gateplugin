/* 
 * Copyright (C) 2015-2016 The University of Sheffield.
 *
 * This file is part of gateplugin-CorpusStats
 * (see https://github.com/johann-petrak/gateplugin-CorpusStats)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package gate.api;

import java.io.Serializable;

import org.apache.log4j.Logger;

import gate.Controller;
import gate.Document;
import gate.Resource;
import gate.creole.ControllerAwarePR;
import gate.creole.ResourceInstantiationException;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.Sharable;
import gate.util.Benchmark;
import gate.util.Benchmarkable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class for all the PRs in this plugin.
 * 
 * This makes it easy to implement a PR where the processing is 
 * divided into the majorsteps: 1) setting up when running is started, 
 * 2) processing all documents, 3) finishing after the last document, if any.
 * <p>
 * Important: this is not yet handling duplication and multi-threaded 
 * processing well.
 */
public abstract class AbstractDocumentProcessor
        extends AbstractLanguageAnalyser
        implements Serializable, ControllerAwarePR, Benchmarkable {

  /**
   *
   */
  private Logger logger = Logger.getLogger(AbstractDocumentProcessor.class.getCanonicalName());

  private int seenDocuments = 0;

  protected Controller controller;

  protected Throwable throwable;
  
  
  private static final Object SYNCOBJECT = new Object();
  
  protected AtomicInteger nDuplicates = null;
  @Sharable
  public void setNDuplicates(AtomicInteger n) {
    nDuplicates = n;
  }
  public AtomicInteger getNDuplicates() {
    return nDuplicates;
  }
  
  
  protected ConcurrentHashMap<String,Object> sharedData = null;
  @Sharable
  public void setSharedData(ConcurrentHashMap<String,Object> v) {
    sharedData = v;
  }
  public ConcurrentHashMap<String,Object> getSharedData() {
    return sharedData;
  }
  
  protected int duplicateId = 0;

  //===============================================================================
  // Implementation of the relevant API methods for DocumentProcessors. These
  // get inherited by the implementing class. This also defines abstract methods 
  // that make it easier to handle the control flow:
  // void process(Document doc) - replaces void execute()
  // void beforeFirstDocument(Controller) - called before the first document is processed
  //     (not called if there were no documents in the corpus, for example)
  // void afterLastDocument(Controller, Throwable) - called after the last document was processed
  //     (not called if there were no documents in the corpus). If Throwable is
  //     not null, processing stopped because of an exception.
  // void finishedNoDocument(Controller, Throwable) - called when processing 
  //     finishes and no documents were processed. If Throwable is not null,
  //     processing finished because of an error.
  //================================================================================
  @Override
  public Resource init() throws ResourceInstantiationException {
    // we always provide the following fields to all PRs which are used for duplicated PRs:
    // nDuplicates is an AtomicInt which gets incremented whenever a resource
    // gets duplicated. 
    synchronized (SYNCOBJECT) {
      if(getNDuplicates() == null) {        
        logger.debug("Creating first instance of PR "+this.getName());
        setNDuplicates(new AtomicInteger(0));
        setSharedData(new ConcurrentHashMap<String,Object>());
        logger.debug("Created duplicate 0 of PR "+this.getName());
      } else {
        int thisn = getNDuplicates().addAndGet(1);
        duplicateId = thisn;
        logger.debug("Created duplicate "+thisn+" of PR "+this.getName());
      }
      afterCreate();
    }
    return this;
  }

  
  @Override
  public void execute() throws ExecutionException {
    if (seenDocuments == 0) {
      beforeFirstDocument(controller);
    }
    seenDocuments += 1;
    process(getDocument());
  }

  @Override
  public void controllerExecutionAborted(Controller arg0, Throwable arg1)
          throws ExecutionException {
    // reset the flags for the next time the controller is run
    controller = arg0;
    throwable = arg1;
    if (seenDocuments > 0) {
      afterLastDocument(arg0, arg1);
    } else {
      processingFinished(arg0, arg1);
    }
  }

  @Override
  public void controllerExecutionFinished(Controller arg0)
          throws ExecutionException {
    controller = arg0;
    if (seenDocuments > 0) {
      afterLastDocument(arg0, null);
    } else {
      processingFinished(arg0, null);
    }
  }

  @Override
  public void controllerExecutionStarted(Controller arg0)
          throws ExecutionException {
    controller = arg0;
    seenDocuments = 0;
    processingStarted(arg0);
  }
  

  //=====================================================================
  // New simplified API for the child classes 
  //=====================================================================
  
  
  /**
   * This gets run right after initialization of the new instance has finished.
   */
  public void afterCreate() {
    // should get overriden by the PR
  }
  
  public int getDocumentsProcessed() {
    return seenDocuments;
  }
  
  /**
   * The new method to implement by PRs which derive from this class.
   * This must return a document which will usually be the same object
   * as it was passed.
   * 
   * @param document 
   * @return  Usually returns the original document wrapped into a list, but
   * can also return the empty list or several new documents. 
   */
  public abstract List<Document> process(Document document);

  /**
   * This can be overridden in PRs and will be run once before
   * the first document seen. 
   * This method is not called if no documents are processed at all: it only
   * runs runs right before the first document is processed.
   * @param ctrl 
   */
  protected void beforeFirstDocument(Controller ctrl) {};

  /**
   * This can be overridden in PRs and will be run after processing has started.
   * This will run once before any document is processed and before the method
   * beforeFirstDocument is invoked, even if no document is being processed at all.
   * 
   * @param ctrl 
   */
  protected void processingStarted(Controller ctrl) { };
  
  /**
   * This runs after documents have been processed.
   * This will not get invoked if no documents have been processed.
   * @param ctrl
   * @param t 
   */
  protected void afterLastDocument(Controller ctrl, Throwable t) {};

  /**
   * This runs after any documents or even no documents have been processed.
   * @param ctrl
   * @param t 
   */
  protected void processingFinished(Controller ctrl, Throwable t) {};
  
  protected void benchmarkCheckpoint(long startTime, String name) {
    if (Benchmark.isBenchmarkingEnabled()) {
      Benchmark.checkPointWithDuration(
              Benchmark.startPoint() - startTime,
              Benchmark.createBenchmarkId(name, this.getBenchmarkId()),
              this, null);
    }
  }

  @Override
  public String getBenchmarkId() {
    return benchmarkId;
  }

  @Override
  public void setBenchmarkId(String string) {
    benchmarkId = string;
  }
  private String benchmarkId = this.getName();

  protected List<Document> documentList(Document... docs)   {
    List<Document> list = new ArrayList<Document>();
    for(Document d : docs) list.add(d);
    return list;
  }
  
  /**
   * Implement high-level API functions that can be used without importing
   * anything.
   * @param methodName
   * @param parms
   * @return 
   */
  protected List<Object> call(String methodName, Object... parms) {
    return new ArrayList<Object>();
  }
}
