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
    private static final int TIMEOUT = 3000; 
    private ArrayList<String> replicaList;
    private static TransactionManager instance;
    private int sequenceNumber;
    
    private TransactionManager() {
        this.sequenceNumber = 0;
        replicaList = new ArrayList<String>();
        replicaList.add("http://localhost:43636/");
    }
    
    public static TransactionManager getInstance() {
        if(instance == null) 
            instance = new TransactionManager();
        
        return instance;
    }
    
    /**
     * Strart a 2PC based write operation in the db
     * @param result the result data to be inserted in the new db entry
     * @param writePath the URI for the request to the db
     * @return string containing the outcome of the operation
     */
    public String twoPhaseCommitWrite(TestResult result, String writePath) {
        sequenceNumber++;
        // Prima fase
        ArrayList<String> resultList = first2PCphase(result, writePath);
        
        // Seconda fase
        return writeQuorumDecision(resultList);
    }
    
    /**
     * Strart a quorum read operation in the db
     * @param readPath the URI to use for the request to the db
     * @return 
     */
    public String quorumRead(String readPath) {
        // Prima fase
        ArrayList<String> resultList = first2PCphase(null, readPath);
        
        // Seconda fase
        return readQuorumDecision(resultList);
    }
    
    /**
     * Start the first phase of the 2PC, one thread is created for each db replica to send a request
     * @param result the result data to be inserted in the db
     * @param path the URI for the request to the db
     * @return 
     */
    public ArrayList<String> first2PCphase (TestResult result, String path){
        String url = BASIC_RESOURCE_IDENTIFIER + "collections" + (result == null ? ("/" + path) : "");
        ArrayList<Callable<String>> threadList = new ArrayList<Callable<String>>();
        ExecutorService threadPoolService = Executors.newFixedThreadPool(5);
        ArrayList<String> resultList = new ArrayList<String>();
        
        // Esegue 5 thread in parallelo, ognuno dei quali invia una richiesta GET o POST ad un replica manager diverso
        for(int i=0; i<replicaList.size(); i++) {
            try {
                if(result == null) 
                    threadList.add(new GetThread(replicaList.get(i) + url, TIMEOUT));
                else
                    threadList.add(new PostThread(replicaList.get(i) + url, TIMEOUT, result, sequenceNumber, path));
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
     * @param sequenceNumber
     * @param commit
     * @return 
     */
    public String second2PCphase (int sequenceNumber, boolean commit){
        String ret = SUCCESS_TRUE;
        String url = BASIC_RESOURCE_IDENTIFIER + "collections/" + (commit ? "commit" : "abort");
        ArrayList<Callable<String>> threadList = new ArrayList<Callable<String>>();
        ExecutorService threadPoolService = Executors.newFixedThreadPool(5);
        
        // Esegue 5 thread in parallelo, ognuno dei quali invia una richiesta POST (Abort o Commit) ad un replica manager diverso
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

    