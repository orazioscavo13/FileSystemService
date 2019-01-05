/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.Serializable;

/**
 * Log file Entry, used for the first phase of 2PC
 * @author Orazio
 * @author Alessandro
 */
public class LogEntry implements Serializable{
    private TestResult result;
    private int sequenceNumber;
    private String collectionName;

    public LogEntry(TestResult result, int sequenceNumber, String collectionName) {
        this.result = result;
        this.sequenceNumber = sequenceNumber;
        this.collectionName = collectionName;
    }

    public TestResult getResult() {
        return result;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getCollectionName() {
        return collectionName;
    }
    
    
}
