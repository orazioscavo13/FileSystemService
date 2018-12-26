/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Orazio
 * @author Alessandro
 */
public class ResultReceiver {
        private final static String QUEUE_NAME = "testResultQueue";
        ConnectionFactory factory;
        Connection connection;
        Channel channel;
        DeliverCallback deliverCallback;
        
        public ResultReceiver() throws IOException, TimeoutException{
            factory = new ConnectionFactory();
            connection = factory.newConnection();
            channel = connection.createChannel();
        }
        
        
        public void initializeReceiver() throws IOException {
            deliverCallback = (String consumerTag, Delivery delivery) -> {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
                    ObjectInput in = null;
                    in = new ObjectInputStream(bis);
                    TestResult result = (TestResult) in.readObject();
                    System.out.println(" [x] Received result");
                    System.out.println(result.getDirectory() + " - Ciclo " + result.getCiclo());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ResultReceiver.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ResultReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            
            factory.setHost("localhost");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        }
}
