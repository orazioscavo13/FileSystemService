/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

/**
 * 
 * @author Orazio
 * @author Alessandro
 */
public class GetThread implements Callable<String> {
    private String url;
    private int timeout;
    private HttpURLConnection con = null;


    public GetThread(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    @Override
    public String call() throws Exception {
        /*
        try {
            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setRequestMethod("GET");
            StringBuilder content;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
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
        */
       return "CIAO MERDE";
    }
    
}
