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
 * This class is created when service is ran, it creates an istance of ResultReceiver, that is a subscriber to results published in the RabbitMQ queue
 * @author Orazio
 * @author Alessandro
 */
public class QueueListener implements ServletContextListener {
    
    /**
     * This method is executed when the application is started
     * @param event Servlet Context evetn
     */
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