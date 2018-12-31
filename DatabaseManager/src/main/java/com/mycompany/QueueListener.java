/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
 
/**
 * 
 * @author Orazio
 * @author Alessandro
 */
public class QueueListener implements ServletContextListener {
 
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ResultReceiver receiver;
        try {
            receiver = new ResultReceiver();
            receiver.initializeReceiver();
        } catch (IOException | TimeoutException ex) {
            Logger.getLogger(QueueListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        
    }
 
}