/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
    private static final String BASIC_RESOURCE_IDENTIFIER = "ReplicaManager-1.0-SNAPSHOT/webresources/mongodb/"; 
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
        initReplicaList();
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
        ArrayList<String> resultList = sendThreads(null, "logfile/maxSequenceNumber", REQUEST_GET, false);
        int maxSeqNum = 0;
        int seqNum = 0;
        for(int i=0; i<resultList.size(); i++) {
            seqNum = (int)(parseJSON(resultList.get(i))).get("sequence_number");
            if(seqNum > maxSeqNum)
                maxSeqNum = seqNum;
        }
        this.sequenceNumber = maxSeqNum;    
        System.out.println("Fixed new sequence number at DatabaseManager Startup: " + maxSeqNum);
        return;
    }
    
    /**
     * Drops replicas' databases collections. NB: for develop use only.
     * @param path the path of the drop replica manager REST
     * @return string containing the outcome of the operation
     */
    public String dropCollections(String path) {
        // Prima fase
        ArrayList<String> resultList = sendThreads(null, path, REQUEST_DELETE, false);
        
        // Seconda fase
        return deleteQuorumDecision(resultList);
    }
    
    /**
     * Return the number of replicas managed
     * @return string containing the outcome of the operation
     */
    public String getReplicas() {
        return "{\"success\": true, \"replicas\": " + replicaList.size() + "}";
    }
    
    /**
     * Start a 2PC based write operation in the db
     * @param result the result data to be inserted in the new db entry
     * @param writePath the URI for the request to the db
     * @return string containing the outcome of the operation
     */
    public String twoPhaseCommitWrite(TestResult result, String writePath) {
        if(sequenceNumber == -1){
            setSequenceNumber();
        }
        sequenceNumber++;
        // Prima fase
        ArrayList<String> resultList = sendThreads(result, writePath, REQUEST_POST, false);
        
        // Seconda fase
        return writeQuorumDecision(resultList);
    }
    
    /**
     * Start a quorum read operation in the db
     * @param readPath the URI to use for the request to the db
     * @return The value read through the quorum protocol
     */
    public String quorumRead(String readPath) {
        // Prima fase
        ArrayList<String> resultList = sendThreads(null, readPath, REQUEST_GET, true);
        
        // Seconda fase
        return readQuorumDecision(resultList);
    }
    
    /**
     * Select randomly a number of replicas equals to the quorum for quorum read
     * @return the list of selected replicas
     */
    ArrayList<String> selectReplicas(){
        ArrayList<String> ret = new ArrayList<String>();
        int []mask = new int[replicaList.size()];
        Arrays.fill(mask, 0);
        Random generator = new Random();
        int count = 0;
        
        while(count <= replicaList.size()/2){
            int rand = generator.nextInt(replicaList.size());
            if(mask[rand] == 0){
                mask[rand] = 1;
                ret.add(replicaList.get(rand));
                count++;
            }
        }
        
        return ret;
    }
    
    /**
     * Creates one thread for each db replica to send a request
     * @param requestType request method
     * @param isRead this flag is true if a quorum read is starting
     * @param result the result data to be inserted in the db
     * @param path the URI for the request to the db
     * @return List containing the results collected from all replicas
     * @return 
     */
    public ArrayList<String> sendThreads (TestResult result, String path, String requestType, boolean isRead){
        String url = BASIC_RESOURCE_IDENTIFIER + "collections" + (result == null ? ("/" + path) : "");
        ArrayList<Callable<String>> threadList = new ArrayList<Callable<String>>();
        ArrayList<String> resultList = new ArrayList<String>();
        ArrayList<String> selectedReplicaList = new ArrayList<String>();
        
        if(isRead == false){
            selectedReplicaList = replicaList;
        }else{
            selectedReplicaList = selectReplicas();
        }
        
        ExecutorService threadPoolService = Executors.newFixedThreadPool(selectedReplicaList.size());
        
        // Esegue selectedReplicaList.size() thread in parallelo, ognuno dei quali invia una richiesta GET/POST/DELETE ad un replica manager diverso
        for(int i=0; i<selectedReplicaList.size(); i++) {
            try {
                switch(requestType){
                    case REQUEST_GET:
                        threadList.add(new GetThread(selectedReplicaList.get(i) + url, TIMEOUT));
                        break;
                    case REQUEST_POST:
                        threadList.add(new PostThread(selectedReplicaList.get(i) + url, TIMEOUT, result, sequenceNumber, path));
                        break;
                    case REQUEST_DELETE:
                        threadList.add(new DeleteThread(selectedReplicaList.get(i) + url, DROP_TIMEOUT));
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            ArrayList<Future<String>> futures = (ArrayList<Future<String>>) threadPoolService.invokeAll(threadList);
            awaitTerminationAfterShutDown(threadPoolService);
            for(int j=0; j<selectedReplicaList.size(); j++) {
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
        long max = -1;
        int index = -1;
        boolean flag = false;
        HashMap<String, Object> object = null;
        
        for(int i=0; i<resultList.size(); i++) {
            object = parseJSON(resultList.get(i));
            if((boolean)object.get("success")) {
                flag = true;
                if((int)object.get("number") > 0){
                    if((long)object.get("maxTimestamp") > max){
                        max = (long)object.get("maxTimestamp");
                        index = i;
                    } 
                }   
            }
        }
        
        if(index != -1){
            return resultList.get(index);
        }else{
            if(flag == true){
                return resultList.get(0);
            }else{
                return SUCCESS_FALSE;
            }
        }
    }
    
    /**
     * Scans collected result to reach a delete quorum
     * @param resultList collected results from quorum delete
     * @return the string which have reached the quorum, else an error 
     */
    public String deleteQuorumDecision(ArrayList<String> resultList) {
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

    private void initReplicaList() {
        Map<String, String> env = System.getenv();
        int replicasNumber = Integer.parseInt(env.get("REPLICAS_NUMBER"));
        replicaList = new ArrayList<String>();
        for(int i = 1; i <= replicasNumber; i++ ){
            replicaList.add("http://replicamanager_" + i + ":8080/");
        }
        System.out.println("ReplicaList:");
        for(String s: replicaList){
            System.out.println(s);
        }
        
        return;
    }
}

    