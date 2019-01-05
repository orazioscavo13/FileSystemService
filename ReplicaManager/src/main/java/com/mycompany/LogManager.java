/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages log file for 2PC db operatiions
 * @author Orazio
 * @author Alessandro
 */
public class LogManager {
    private static final String BASIC_LOG_PATH = "../../../../../../../dbLog";
    private static final String LOG_PATH = BASIC_LOG_PATH + "/replicaLog.log";
    
    /**
     * Writes a LogEntry in the log file
     * @param sequenceNumber sequence number of the db write operation
     * @param collectionName name of the db collection
     * @param directory directory of the test result
     * @param cycle cycle of the test result
     * @param meanAdd Mean Execution time for the add operations in the submitted test
     * @param meanDownload Mean Execution time for the downloadoperations in the submitted test
     * @param stdDevAdd Standard Deviation of Execution time for the add operations in the submitted test
     * @param stdDevDownload Standard Deviation of Execution time for the download operations in the submitted test
     * @param state state of the test result
     * @param timestamp timestamp of the test result
     * @return boolean, true if write succeed
     */
    public static boolean addLogEntry (int sequenceNumber, String collectionName, String directory, int cycle, double meanAdd, double meanDownload, double stdDevAdd, double stdDevDownload, int state, String timestamp) {
        LogEntry logEntry = new LogEntry(new TestResult(cycle, directory, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state, timestamp), sequenceNumber, collectionName);
        
        ArrayList<LogEntry> entries = readEntries();
        if(entries!=null){
            entries.add(logEntry);
        } else {
            entries = new ArrayList<LogEntry>();
            entries.add(logEntry);
        }
        
        return writeEntries(entries);
    }
    
    /**
     * Check if the log file exists, if not creates it.
     * @return boolean, false if the log file creation failed.
     */
    public static boolean checkLogFile() {
        boolean ret = true;
        
        if(!Files.exists(Paths.get(LOG_PATH))){
            try{
                Files.createDirectories(Paths.get(BASIC_LOG_PATH));
                File file = new File(LOG_PATH);
                file.createNewFile();
                writeEntries(new ArrayList<LogEntry>());
            }catch(IOException ioe){
              System.out.println("Error while creating a new log file :" + ioe);
              ret = false;
            }
        }
        
        return ret;
    }
    
    /**
     * get the max sequence number in use in the log file, -1 if none
     * @return 
     */
    public static int getMaxSequenceNumber() {
        ArrayList<LogEntry> entries = readEntries();
        if(entries != null) {
            int maxSeqNum = 0;
            int auxSeqNum = 0;

            for (LogEntry entry : entries) { 
                auxSeqNum = entry.getSequenceNumber();
                if(auxSeqNum > maxSeqNum)
                    maxSeqNum = auxSeqNum; 
            }
            return maxSeqNum;
        } 
        
        return -1;
    }
    
    /**
     * Reads all entries from log file
     * @return ArrayList of LogEntry containing the entries of the log file
     */
    public static ArrayList<LogEntry> readEntries() {
        
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ArrayList<LogEntry> entries = null;
        
        try {
            fis = new FileInputStream(LOG_PATH);
            ois = new ObjectInputStream(fis);
            entries = (ArrayList<LogEntry>) ois.readObject();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                ois.close();
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return entries;
    }
    
    /**
     * Writes an ArrayList of LogEntry in the log file
     * @param entries the Arraylist of LogEntry
     * @return boolean, true if write succeed
     */
    public static boolean writeEntries (ArrayList<LogEntry> entries) {
        boolean ret = true;
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        
        try {
            fout = new FileOutputStream(LOG_PATH);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(entries);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } catch (IOException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } finally {
            try {
                oos.close();
                fout.close();
            } catch (IOException ex) {
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
        }
        
        return ret;
    }
}
