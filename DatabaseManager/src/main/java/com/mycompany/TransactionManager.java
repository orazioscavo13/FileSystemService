/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
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
        replicaList.add("localhost:43636/");
        replicaList.add("localhost:43636/");
        replicaList.add("localhost:43636/");
        replicaList.add("localhost:43636/");
        replicaList.add("localhost:43636/");
    }
    
    public static TransactionManager getInstance() {
        if(instance == null) 
            instance = new TransactionManager();
        
        return instance;
    }
    
    public String twoPhaseCommitWrite(TestResult result, String writePath) {
        // Prima fase
        ArrayList<String> resultList = first2PCphase(result, writePath);
        
        // Seconda fase
        return writeQuorumDecision(resultList);
    }
    
    public String quorumRead(String readPath) {
        // Prima fase
        ArrayList<String> resultList = first2PCphase(null, readPath);
        
        // Seconda fase
        return readQuorumDecision(resultList);
    }
    
    public ArrayList<String> first2PCphase (TestResult result, String path){
        String url = BASIC_RESOURCE_IDENTIFIER + "collections/" + path;
        ArrayList<Callable<String>> threadList = new ArrayList<Callable<String>>();
        ExecutorService threadPoolService = Executors.newFixedThreadPool(5);
        ArrayList<String> resultList = new ArrayList<String>();
        
        // Esegue 5 thread in parallelo, ognuno dei quali invia una richiesta GET o POST ad un replica manager diverso
        for(int i=0; i<replicaList.size(); i++) {
            try {
                if(result == null) 
                    threadList.add(new GetThread(replicaList.get(i) + url, TIMEOUT));
                else
                    threadList.add(new PostThread(replicaList.get(i) + url, TIMEOUT, result));
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
     * TODO TODO TODO TODO TODO TODO TODO TODO TODO
     * @param result
     * @return 
     */
    public String writeQuorumDecision(ArrayList<String> result) {
        return "CIAO";
        // TODO ANCHE I POST THREAD E GET THREAD!
    }
    
    /**
     * Scans collected result to reach a read quorum
     * @param resultList
     * @return the string which have reached the quorum, else an error 
     */
    public String readQuorumDecision(ArrayList<String> resultList) {
        int count;
        
        for(int i=0; i<resultList.size(); i++) {
            count = 0;
            for(int j=0; j<resultList.size(); j++) {
                if(i!=j && resultList.get(i).equals(resultList.get(j))) {
                    count++;
                    if(count > resultList.size()/2)
                        return resultList.get(i);
                }
            }
        }
        
        return SUCCESS_FALSE;
    }
    
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
}
