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
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a subscriber for the data produced by automatic test, waits for data coming from rabbitMQ queue
 * @author Orazio
 * @author Alessandro
 */
public class ResultReceiver {
        private final static String QUEUE_NAME = "testResultQueue";
        private final static String RESULT_COLLECTION_NAME = "testResult";
        private TransactionManager transactionManager;
        private ConnectionFactory factory;
        private Connection connection;
        private Channel channel;
        private DeliverCallback deliverCallback;
        
        public ResultReceiver() throws IOException, TimeoutException{
            factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            Map<String, String> env = System.getenv();
            factory.setUsername(env.get("RABBITMQ_USERNAME"));
            factory.setPassword(env.get("RABBITMQ_PASSWORD"));
            connection = factory.newConnection();
            channel = connection.createChannel();
            transactionManager = TransactionManager.getInstance();
        }
        
        public void initializeReceiver() throws IOException {
            // Callback chiamata ogni qual volta viene ricevuto qualcosa dalla coda RabbitMQ
            deliverCallback = (String consumerTag, Delivery delivery) -> {
                try {
                    // Deserializziamo l'elemento prelevato dalla coda
                    ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
                    ObjectInput in = null;
                    in = new ObjectInputStream(bis);
                    TestResult result = (TestResult) in.readObject();
                    
                    // Salvataggio dell'elemento all'interno del db tramite 2PC
                    System.out.println("[x] Received result: " + result.toString());
                    transactionManager.twoPhaseCommitWrite(result, RESULT_COLLECTION_NAME);
                } catch (ClassNotFoundException | IOException ex) {
                    Logger.getLogger(ResultReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        }
}
