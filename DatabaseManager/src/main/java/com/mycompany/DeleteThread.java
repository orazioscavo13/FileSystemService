/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This thread (used for drop all databases) sends a DELETE request to a single replica of the db, setting a timeout for the response
 * @author Orazio
 * @author Alessandro
 */
public class DeleteThread implements Callable<String> {
    private String url;
    private int timeout;
    private static final String SUCCESS_FALSE = "{\"success\": false}";

    
    public DeleteThread(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    /**
     * Sends a DELETE request to a single replica manager and waits for response within a specified timeout
     * @return String contining the outcome of the operations
     */
    @Override
    public String call() throws Exception {
        HttpURLConnection con = null;
        StringBuilder content = null;
        String ret;
        URL myurl = new URL(url);
        
        con = (HttpURLConnection) myurl.openConnection();
        con.setRequestMethod("DELETE");
        con.setConnectTimeout(timeout); 
        
        try {
            if(con.getResponseCode() != 200) {
                ret = SUCCESS_FALSE;
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                content = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
                ret = content.toString();
            }
        } catch(SocketTimeoutException e){
            Logger.getLogger(GetThread.class.getName()).log(Level.SEVERE, null, e);
            ret = SUCCESS_FALSE;
        }  catch (MalformedURLException ex) {
            Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
            ret = SUCCESS_FALSE;
        } catch (IOException ex) {
            Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
            ret = SUCCESS_FALSE;
        } finally {
            con.disconnect();
        }
        
        return ret;
    }
}