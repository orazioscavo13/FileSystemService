/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This singleton class implements the quorum protocols for db operations, mantaining a unique sequence number
 * @author Orazio
 * @author Alessandro
 */
public class TransactionManager {
    private static final String BASIC_RESOURCE_IDENTIFIER = "ReplicaManager/webresources/mongodb/"; 
    private static final String SUCCESS_FALSE = "{\"success\": false}";
    private static final String SUCCESS_TRUE = "{\"success\": true}";
    private static final String REQUEST_DELETE = "delete"; 
    private static final String REQUEST_GET = "get"; 
    private static final String REQUEST_POST = "post"; 
    private static final int TIMEOUT = 3000; 
    private static final int DROP_TIMEOUT = 10000; 
    private ArrayList<String> replicaList;
    private static TransactionManager instance;
    private int sequenceNumber = -1;
    
    private TransactionManager() {
        replicaList = new ArrayList<String>();
        replicaList.add("http://localhost:43636/");
    }
    
    public static TransactionManager getInstance() {
        if(instance == null) 
            instance = new TransactionManager();
        
        return instance;
    }
    
    /**
     * sends a GET request to all replicas to get the max sequence number in use in their log files and sets the sequence number property according to it
     */
    public void setSequenceNumber() {
        ArrayList<String> resultList = sendThreads(null, "logfile/maxSequenceNumber", REQUEST_GET);
        int maxSeqNum = 0;
        int seqNum = 0;
        for(int i=0; i<resultList.size(); i++) {
            seqNum = (int)(parseJSON(resultList.get(i))).get("sequence_number");
            if(seqNum > maxSeqNum)
                maxSeqNum = seqNum;
        }
        this.sequenceNumber = maxSeqNum;        
        return;
    }
    
    /**
     * Drops replicas' databases collections. NB: for develop use only.
     * @param path the path of the drop replica manager REST
     * @return string containing the outcome of the operation
     */
    public String dropCollections(String path) {
        // Prima fase
        ArrayList<String> resultList = sendThreads(null, path, REQUEST_DELETE);
        
        // Seconda fase
        return readQuorumDecision(resultList);
    }
    
    /**
     * Strart a 2PC based write operation in the db
     * @param result the result data to be inserted in the new db entry
     * @param writePath the URI for the request to the db
     * @return string containing the outcome of the operation
     */
    public String twoPhaseCommitWrite(TestResult result, String writePath) {
        if(sequenceNumber == -1) setSequenceNumber();
        sequenceNumber++;
        // Prima fase
        ArrayList<String> resultList = sendThreads(result, writePath, REQUEST_POST);
        
        // Seconda fase
        return writeQuorumDecision(resultList);
    }
    
    /**
     * Strart a quorum read operation in the db
     * @param readPath the URI to use for the request to the db
     * @return The value read through the quorum protocol
     */
    public String quorumRead(String readPath) {
        // Prima fase
        ArrayList<String> resultList = sendThreads(null, readPath, REQUEST_GET);
        
        // Seconda fase
        return readQuorumDecision(resultList);
    }
    
    /**
     * Creates one thread for each db replica to send a request
     * @param result the result data to be inserted in the db
     * @param path the URI for the request to the db
     * @return List containing the results collected from all replicas
     */
    public ArrayList<String> sendThreads (TestResult result, String path, String requestType){
        String url = BASIC_RESOURCE_IDENTIFIER + "collections" + (result == null ? ("/" + path) : "");
        ArrayList<Callable<String>> threadList = new ArrayList<Callable<String>>();
        ExecutorService threadPoolService = Executors.newFixedThreadPool(replicaList.size());
        ArrayList<String> resultList = new ArrayList<String>();
        
        // Esegue replicaList.size() thread in parallelo, ognuno dei quali invia una richiesta GET/POST/DELETE ad un replica manager diverso
        for(int i=0; i<replicaList.size(); i++) {
            try {
                switch(requestType){
                    case REQUEST_GET:
                        threadList.add(new GetThread(replicaList.get(i) + url, TIMEOUT));
                        break;
                    case REQUEST_POST:
                        threadList.add(new PostThread(replicaList.get(i) + url, TIMEOUT, result, sequenceNumber, path));
                        break;
                    case REQUEST_DELETE:
                        threadList.add(new DeleteThread(replicaList.get(i) + url, DROP_TIMEOUT));
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            ArrayList<Future<String>> futures = (ArrayList<Future<String>>) threadPoolService.invokeAll(threadList);
            awaitTerminationAfterShutDown(threadPoolService);
            for(int j=0; j<replicaList.size(); j++) {
                resultList.add(futures.get(j).get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return resultList;
    }
    
    /**
     * Second phase of the 2PC write, it can be a commit or an abort, one thread is created for each db replica to send a request
     * @param sequenceNumber sequence number of the write db operation
     * @param commit boolean, true for commit, false for abort
     * @return String containing the outcome of the operation
     */
    public String second2PCphase (int sequenceNumber, boolean commit){
        String ret = SUCCESS_TRUE;
        String url = BASIC_RESOURCE_IDENTIFIER + "collections/" + (commit ? "commit" : "abort");
        ArrayList<Callable<String>> threadList = new ArrayList<Callable<String>>();
        ExecutorService threadPoolService = Executors.newFixedThreadPool(replicaList.size());
        
        // Esegue replicaList.size() thread in parallelo, ognuno dei quali invia una richiesta POST (Abort o Commit) ad un replica manager diverso
        for(int i=0; i<replicaList.size(); i++) {
            try {
                threadList.add(new PostThread(replicaList.get(i) + url, TIMEOUT, null , sequenceNumber, null));
            } catch (Exception ex) {
                Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
                ret = SUCCESS_FALSE;
            }
        }
        try {
            ArrayList<Future<String>> futures = (ArrayList<Future<String>>) threadPoolService.invokeAll(threadList);
            awaitTerminationAfterShutDown(threadPoolService);
        } catch (InterruptedException ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
            ret = SUCCESS_FALSE;
        } 
        return ret;
    }
    
    
    /**
     * Collected results from first phase of 2PC are collected end a commit or an abort is started
     * @param resultList the results collected in the first phase of 2PC write 
     * @return string containing the outcome of the operation and the outcome of the commit/abort operation
     */
    public String writeQuorumDecision(ArrayList<String> resultList) {
        int count = 0;
        HashMap<String, Object> object = null;
        for(int i=0; i<resultList.size(); i++) {
            object = parseJSON(resultList.get(i));
            if((boolean)object.get("success")) {
                    count++;
                    if(count > resultList.size()/2){
                        return "{\"success\": true, \"commit_success\": " + second2PCphase((int)object.get("sequence_number"), true) + "}";        
                    }
            }
        }
        return "{\"success\": false, \"abort_success\": " + second2PCphase((int)object.get("sequence_number"), false) + "}";        
    }
    
    /**
     * Scans collected result to reach a read quorum
     * @param resultList collected results from quorum read
     * @return the string which have reached the quorum, else an error 
     */
    public String readQuorumDecision(ArrayList<String> resultList) {
        int count;
        
        for(int i=0; i<resultList.size(); i++) {
            count = 0;
            for(int j=0; j<resultList.size(); j++) {
                if(resultList.get(i).equals(resultList.get(j))) {
                    count++;
                    if(count > resultList.size()/2)
                        return resultList.get(i);
                }
            }
        }
        
        return SUCCESS_FALSE;
    }
    
    
    /**
     * This method is used to wait the collection of db responses from all threads
     * @param threadPool the threadpool containing all the threads 
     */
    public void awaitTerminationAfterShutDown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * This function is used to parse a string containing a JSON object
     * @param object Stringified JSON object
     * @return HashMap containing the object (key-value), the value has to be casted to right type to be used
     */
    public HashMap<String, Object> parseJSON(String object){
        //converting json to Map
        HashMap<String,Object> myMap = new HashMap<String, Object>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            myMap = objectMapper.readValue(object, HashMap.class);
        } catch (IOException ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return myMap;
    }
}

    