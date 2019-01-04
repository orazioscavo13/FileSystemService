/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * @author Orazio
 * @author Alessandro
 */
public class RequestSenderService {
    private final int port = 43636;
    private final String baseUrl = "http://localhost:";

    public RequestSenderService() {
    }
    
    
     /**
      * 
      * @param path the path for the new directory
      * @return execution time (ms), -1 if the operation fails
      */
    public long createDirectory(String path){
        HttpURLConnection con = null;
        String url = baseUrl + port + "/Homework1-web/webresources/filesystem/directories";
        String urlParameters = "path=" + path;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

        try {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }
            long time = System.currentTimeMillis();
            int result = con.getResponseCode();
            if(result != 200) 
                return -1;
            time = System.currentTimeMillis() - time;
            return time;
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            con.disconnect();
        }
        return -1;
    }
    
    /**
     * 
     * @param path local path of the file to be uploaded
     * @param destination destination for the file in the REST filesystem
     * @return execution time (ms), -1 if the operation fails
     */
    public long uploadFile(String path, String destination){
        //richiesta post (upload file)
        String url = baseUrl + port + "/Homework1-web/webresources/filesystem/files/";
        HttpURLConnection httpConn;
        DataOutputStream UploadRequest = null;
        String boundary =  "*****";
        String crlf = "\r\n";
        String twoHyphens = "--";
        URL uploadUrl = null;
        try {
            uploadUrl = new URL(url);
            httpConn = (HttpURLConnection) uploadUrl.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);

            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            UploadRequest =  new DataOutputStream(httpConn.getOutputStream());
            //Aggiunta di un campo post (destination: *)
            UploadRequest.writeBytes(twoHyphens + boundary + crlf);
            UploadRequest.writeBytes("Content-Disposition: form-data; name=\"destination\""+ crlf);
            UploadRequest.writeBytes("Content-Type: text/plain; charset=UTF-8" + crlf);
            UploadRequest.writeBytes(crlf);
            UploadRequest.writeBytes(destination + crlf);
            UploadRequest.flush();
            //aggiunta file
            File uploadFile = new File(path);
            String fileName = uploadFile.getName();
            String fieldName = "file";
            UploadRequest.writeBytes(twoHyphens + boundary + crlf);
            UploadRequest.writeBytes("Content-Disposition: form-data; name=\"" +
                    fieldName + "\";filename=\"" +
                    fileName + "\"" + crlf);
            UploadRequest.writeBytes(crlf);

            byte[] bytes = Files.readAllBytes(uploadFile.toPath());
            UploadRequest.write(bytes);
            UploadRequest.writeBytes(crlf);
            UploadRequest.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

            UploadRequest.flush();
            UploadRequest.close();
            
            
            long time = System.currentTimeMillis();
            int result = httpConn.getResponseCode();
            if(result != 200) 
                return -1;
            time = System.currentTimeMillis() - time;
            return time;
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    /**
     * 
     * @param path the path of the file to be downloaded
     * @return execution time (ms), -1 if the operation fails
     */
    public long downloadFile(String path){
        HttpURLConnection con = null;
        String url = baseUrl + port + "/Homework1-web/webresources/filesystem/files/download/" + path;
        try {
        URL myurl = new URL(url);
        con = (HttpURLConnection) myurl.openConnection();
        con.setRequestMethod("GET");
        StringBuilder content;
        long time = System.currentTimeMillis();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            time = System.currentTimeMillis() - time;
            String line;
            content = new StringBuilder();
            while ((line = in.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }
        System.out.println(content.toString());
        return time;
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            con.disconnect();
        }
        return -1;
    }

    
    /**
     * This method is used after the test for the final cleanup
     * @param path the path of the directory to be deleted
     */
    public void deleteDirectory(String path) {
        HttpURLConnection con = null;
        String url = baseUrl + port + "/Homework1-web/webresources/filesystem/directories/"+ path;
        try {
            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setRequestMethod("DELETE");
            int result = con.getResponseCode();
            return;
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestSenderService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            con.disconnect();
        }
        return;
    }
    
}
