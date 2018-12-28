/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author Orazio
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
