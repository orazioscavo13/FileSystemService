/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.util.concurrent.Callable;

/**
 *
 * @author Orazio & Alessandro
 */
public class TestThread implements Callable<Long> {
    private String path;
    private String destination;
    private int type; //0->download / 1->upload

    public TestThread(String path, String destination) {
        this.path = path;
        this.destination = destination;
    }

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
