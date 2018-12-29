/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Orazio
 * @author Alessandro
 */
public class PostThread implements Callable<String> {
    private String url;
    private int timeout;
    private TestResult result;
    private int sequenceNumber;
    private String collectionName;

    public PostThread(String url, int timeout, TestResult result, int sequenceNumber, String collectionName) {
        this.url = url;
        this.timeout = timeout;
        this.result = result;
        this.sequenceNumber = sequenceNumber;
        this.collectionName = collectionName;
    }

    @Override
    public String call() throws Exception {
        String ret;
        HttpURLConnection con = null;
        StringBuilder content = null;
        String urlParameters = "sequence_number=" + sequenceNumber;
        
        if(result != null){
            urlParameters = urlParameters + 
                    "&directory=" + result.getDirectory() + 
                    "&cycle=" + result.getCycle() + 
                    "&mean_add=" + result.getMeanAdd()+ 
                    "&mean_download=" + result.getMeanDownload()+ 
                    "&stddev_add=" + result.getStdAdd()+ 
                    "&stddev_download=" + result.getStdDownload()+ 
                    "&state=" + result.getState()+ 
                    "&timestamp=" + result.getTimestamp()+ 
                    "&collection_name=" + (collectionName == null ? "testResult" : collectionName);
        }
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        URL myurl = new URL(url);
        con = (HttpURLConnection) myurl.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setConnectTimeout(timeout); 
        con.setRequestProperty("User-Agent", "Java client");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(postData);
        } catch (IOException ex) {
            Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
            ret = "{\"success\": false, \"sequence_number\": " + sequenceNumber + "}";
        } 
        
        try {
            if(con.getResponseCode() != 200) {
                ret = "{\"success\": false, \"sequence_number\": " + sequenceNumber + "}";
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
            ret = "{\"success\": false, \"sequence_number\": " + sequenceNumber + "}";
        }  catch (MalformedURLException ex) {
            Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
            ret = "{\"success\": false, \"sequence_number\": " + sequenceNumber + "}";
        } catch (IOException ex) {
            Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
            ret = "{\"success\": false, \"sequence_number\": " + sequenceNumber + "}";
        } finally {
            con.disconnect();
        }
        
        return ret;
    }
    
}
