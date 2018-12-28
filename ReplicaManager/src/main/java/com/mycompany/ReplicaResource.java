/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.bson.Document;

/**
 * REST Web Service
 *
 * @author Orazio
 */
@Path("mongodb")
public class ReplicaResource {
    private MongoClient mongo;
    private MongoCredential credential; 
    private MongoDatabase database;
    private static final String BASIC_LOG_PATH = "../dbLog";
    private static final String LOG_PATH = BASIC_LOG_PATH + "/replicaLog.log";
    private static final String SUCCESS_FALSE = "{\"success\": false}";
    private static final String SUCCESS_TRUE = "{\"success\": true}";
    

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ReplicaResource
     */
    public ReplicaResource() {
         
    }
    
    /**
     * TODO: RITORNARE BOOLEAN
     * @return 
     */
    public boolean connect(){
        boolean ret = true;
        
        try {
            // Creating a Mongo client 
            mongo = new MongoClient( "localhost" , 27017 ); 

            // Creating Credentials 
            credential = MongoCredential.createCredential("FSDatabaseManagerAdmin", "FileSyistemDB", "password".toCharArray()); 
            System.out.println("Connected to the database successfully");  

            // Accessing the database 
            database = mongo.getDatabase("FileSyistemDB");
        } catch(MongoClientException e) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, e);
            ret = false;
        }
        
        return ret;
    }
      
    /**
     * DELETE method for deleting a collection
     * @param collectionName
     * @return string containing the outcome of the operation
     */
    @DELETE
    @Path("collections/{collectionName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteCollection(@PathParam("collectionName") String collectionName) {
        String ret;
        if(connect()) {
            MongoCollection<Document> collection = database.getCollection(collectionName);

            collection.drop(); 
            ret = SUCCESS_TRUE;
        } else 
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * Retrieves all documents in the specified collection
     * @param collectionName
     * @return an instance of java.lang.String
     */
    @GET
    @Path("collections/{collectionName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCollection(@PathParam("collectionName") String collectionName) {
        String ret;
        if(connect()) {
            BasicDBObject searchQuery = new BasicDBObject();
            MongoCollection<Document> collection = database.getCollection(collectionName);
            FindIterable<Document> iterDoc = collection.find();
            Iterator it = iterDoc.iterator(); 

            String out = "";
            while (it.hasNext()) { out = out + it.next() + (it.hasNext() ? ", " : ""); }
            ret = "{\"success\": true, \"documents\": " + out + "}";
        } else
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * Retrieves the last document inserted in the specified collection
     * @return an instance of java.lang.String
     */
    @GET
    @Path("collections/{collectionName}/lastCommittedResult")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastTestResult(@PathParam("collectionName") String collectionName) {
        String ret;
        if(connect()) {
            BasicDBObject searchQuery = new BasicDBObject();
            MongoCollection<Document> collection = database.getCollection(collectionName);
            FindIterable<Document> iterDoc = collection.find(new BasicDBObject());
            iterDoc.sort(new BasicDBObject("_id", -1));
            Iterator it = iterDoc.iterator();
            if(it.hasNext()) { 
                ret = "{\"success\": true, \"lastCommittedResult\": " + it.next() + "}";
            } else
                ret = SUCCESS_FALSE;
        } else
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * POST method for adding or creating an element in the DB
     * @param sequenceNumber
     * @param collectionName
     * @param directory
     * @param cycle
     * @param meanAdd
     * @param meanDownload
     * @param stdDevAdd
     * @param stdDevDownload
     * @param state
     * @param timestamp
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections")
    @Produces(MediaType.TEXT_PLAIN)
    public String AddEntry(@FormParam("sequenceNumber") int sequenceNumber, @FormParam("collectionName") String collectionName, @FormParam("directory") String directory, @FormParam("cycle") int cycle, @FormParam("mean_add") double meanAdd, @FormParam("mean_download") double meanDownload, @FormParam("stddev_add") double stdDevAdd, @FormParam("stddev_download") double stdDevDownload, @FormParam("state") int state, @FormParam("timestamp") String timestamp) {
        String ret = "";
        
        if(!checkLogFile()) ret = SUCCESS_FALSE;     
        else {
            if(addLogEntry(sequenceNumber, collectionName, directory, cycle, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state, timestamp))
                ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
            else
                ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
        }
        
        return ret;
    }
        
    /**
     * POST method for adding or creating an element in the DB
     * @param sequenceNumber
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/commit")
    @Produces(MediaType.TEXT_PLAIN)
    public String CommitEntry(@FormParam("sequenceNumber") int sequenceNumber) {
        String ret = "";
        LogEntry entry = null;
        LogEntry entryToCommit = null;
        
        ArrayList<LogEntry> entries = readEntries();
        if(entries == null) {
            ret = SUCCESS_FALSE;
        } else {
            // Ricerca nel log dell'elemento da committare
            Iterator<LogEntry> iterator = entries.iterator();
            while(iterator.hasNext()) {
                entry = iterator.next();
                if(entry.getSequenceNumber() == sequenceNumber) {
                    entryToCommit = entry;
                    iterator.remove();
                    break;
                }
            }
            
            // Scrittura sul database
            if(entryToCommit != null && writeDbEntry(entryToCommit)) {
                
                // Aggiornamento log se la scrittura sul database ha avuto successo
                if(writeEntries(entries))
                    ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
                else
                    ret = SUCCESS_FALSE;
            } else 
                ret = SUCCESS_FALSE;
        }
        
        return ret;
    }
    
    
    /**
     * Reads entries from log file
     * @return the entries of the log file
     */
    public ArrayList<LogEntry> readEntries() {
        
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ArrayList<LogEntry> entries = null;
        
        try {
            fis = new FileInputStream(LOG_PATH);
            ois = new ObjectInputStream(fis);
            entries = (ArrayList<LogEntry>) ois.readObject();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                ois.close();
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return entries;
    }
    
    /**
     * Writes an array list of entries in the log file
     * @param entries
     * @return boolean, true if success
     */
    public boolean writeEntries (ArrayList<LogEntry> entries) {
        boolean ret = true;
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        
        try {
            fout = new FileOutputStream(LOG_PATH);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(entries);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } catch (IOException ex) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } finally {
            try {
                oos.close();
                fout.close();
            } catch (IOException ex) {
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
        }
        
        return ret;
    }
    
    /**
     * Writes an entry in the log file
     * @param sequenceNumber
     * @param collectionName
     * @param directory
     * @param cycle
     * @param meanAdd
     * @param meanDownload
     * @param stdDevAdd
     * @param stdDevDownload
     * @param state
     * @param timestamp
     * @return boolean, true if success
     */
    public boolean addLogEntry (int sequenceNumber, String collectionName, String directory, int cycle, double meanAdd, double meanDownload, double stdDevAdd, double stdDevDownload, int state, String timestamp) {
        LogEntry logEntry = new LogEntry(new TestResult(cycle,directory,meanAdd, meanDownload, stdDevAdd, stdDevDownload, state), sequenceNumber, collectionName);
        
        ArrayList<LogEntry> entries = readEntries();
        if(entries!=null){
            entries.add(logEntry);
        } else {
            entries = new ArrayList<LogEntry>();
            entries.add(logEntry);
        }
        
        return writeEntries(entries);
    }
    
    /**
     * Creates the log file if it does not exist. 
     * @return false if the log file creation failed.
     */
    public boolean checkLogFile() {
        if(!Files.exists(Paths.get(LOG_PATH))){
            try{
                Files.createDirectories(Paths.get(BASIC_LOG_PATH));
                File file = new File(LOG_PATH);
                file.createNewFile();
            }catch(IOException ioe){
              System.out.println("Error while creating a new log file :" + ioe);
              return false;
            }
        }
        
        return true;
    }
    
    /**
     * 
     * @param entry
     * @param collectionName
     * @return true if the write operation succeed
     */
    public boolean writeDbEntry(LogEntry entry) {
        boolean ret = true; 
        
        if(connect()) {
            // Create collection (if it does not exist) and insert a document
            MongoCollection<Document> collection = database.getCollection(entry.getCollectionName());
            Document document = new Document()
            .append("directory", entry.getResult().getDirectory())
            .append("cycle", entry.getResult().getCycle())
            .append("meanAdd", entry.getResult().getMeanAdd())
            .append("meanDownload", entry.getResult().getMeanDownload())
            .append("stdDevAdd", entry.getResult().getStdAdd())
            .append("stdDevDowdnload", entry.getResult().getStdDownload())
            .append("state", entry.getResult().getState())
            .append("timestamp", entry.getResult().getTimestamp());

            try {
                collection.insertOne(document);
            } catch (MongoWriteException | MongoWriteConcernException e){
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, e);
                ret = false;
            } catch (MongoException e) {
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, e);
                ret = false;
            } 
        } else ret = false;

        return ret;
    }
}
