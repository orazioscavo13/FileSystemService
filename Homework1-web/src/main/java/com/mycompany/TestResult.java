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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Orazio
 * @author Alessandro
 */
public class TestResult implements Serializable{
    private int ciclo;
    private String directory;
    private double meanAdd;
    private double meanDownload;
    private double stdAdd;
    private double stdDownload;
    private String timestamp;
    private int state;

    /**
     * 
     * @param ciclo
     * @param directory
     * @param meanAdd
     * @param meanDownload
     * @param stdAdd
     * @param stdDownload
     * @param state 
     */
    public TestResult(int ciclo, String directory, double meanAdd, double meanDownload, double stdAdd, double stdDownload, int state) {
        this.ciclo = ciclo;
        this.directory = directory;
        this.meanAdd = meanAdd;
        this.meanDownload = meanDownload;
        this.stdAdd = stdAdd;
        this.stdDownload = stdDownload;
        this.state = state;
        this.timestamp = (new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss")).format(new Date());
    }

    public int getCiclo() {
        return ciclo;
    }

    public String getDirectory() {
        return directory;
    }

    public double getMeanAdd() {
        return meanAdd;
    }

    public double getMeanDownload() {
        return meanDownload;
    }

    public double getStdAdd() {
        return stdAdd;
    }

    public double getStdDownload() {
        return stdDownload;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getState() {
        return state;
    }

    /**
     * 
     * @return Serialized Object as a byte
     * @throws IOException 
     */
    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        out = new ObjectOutputStream(bos);   
        out.writeObject(this);
        out.flush();
        return bos.toByteArray();
    }
}
