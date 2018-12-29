/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Orazio
 * @author Alessandro
 */
public class GetThread implements Callable<String> {
    private String url;
    private int timeout;
    private static final String SUCCESS_FALSE = "{\"success\": false}";

    public GetThread(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    /**
     * Sends a get request to a single replica manager and waits for response within a specified timeout
     * @return String contining the outcome of the operations
     * @throws Exception 
     */
    @Override
    public String call() throws Exception {
        HttpURLConnection con = null;
        String ret;
        URL myurl = new URL(url);
        try{
            con = (HttpURLConnection) myurl.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeout); //set timeout to 5 seconds
            ret = con.getResponseMessage();
            if(con.getResponseCode() != 200) 
                ret = SUCCESS_FALSE;
        }catch(SocketTimeoutException e){
            Logger.getLogger(GetThread.class.getName()).log(Level.SEVERE, null, e);
            ret = SUCCESS_FALSE;
        }
        return ret;
    }
}