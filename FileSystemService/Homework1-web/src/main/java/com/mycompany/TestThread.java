/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.util.concurrent.Callable;

/**
 * This thread can be used to upload or download a file from the filesystem, we use this thread during automatic tests
 * @author Orazio
 * @author Alessandro
 */
public class TestThread implements Callable<Long> {
    private String path;
    private String destination;
    
    /**
     * This constructor is used for upload
     * @param path path for the test operation
     * @param destination remote destination for the uploaded file
     */
    public TestThread(String path, String destination) {
        this.path = path;
        this.destination = destination;
    }

    /**
     * This contructor is used for download
     * @param path path of the file to download
     */
    public TestThread(String path) {
        this.path = path;
        this.destination = null;
    }
    
    RequestSenderService sender = new RequestSenderService();

    @Override
    public Long call() throws Exception {
        long time;
        if(destination == null){
            time = sender.downloadFile(path);
        }else{
            time = sender.uploadFile(path, destination);
        }
        return time;
    }
    
}
