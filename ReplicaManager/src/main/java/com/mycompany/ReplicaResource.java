/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.bson.Document;

/**
 * REST Web Service
 *
 * @author Orazio
 * @author Alessandro
 */
@Path("mongodb")
public class ReplicaResource {
    private MongoClient mongo;
    private MongoCredential credential; 
    private MongoDatabase database;
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
     * Connects the replica manager to its database
     * @return true if replica manager connects to its database, else false 
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
     * DELETE method for deleting a collection from the database
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
     * @return string containing the outcome of the operation and the collection's documents
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
     * Retrieves the last document committed in the specified collection
     * @param collectionName
     * @return string containing the outcome of the operation and the last committed document
     */
    @GET
    @Path("collections/{collectionName}/lastCommittedDocument")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastCommittedDocument(@PathParam("collectionName") String collectionName) {
        String ret;
        if(connect()) {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            FindIterable<Document> iterDoc = collection.find(new BasicDBObject());
            iterDoc.sort(new BasicDBObject("_id", -1));
            Iterator it = iterDoc.iterator();
            if(it.hasNext()) { 
                ret = "{\"success\": true, \"lastCommittedDocument\": " + it.next() + "}";
            } else
                ret = SUCCESS_FALSE;
        } else
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * POST method for adding or creating an element in the log file (for first phase of 2PC)
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
        
        if(!LogManager.checkLogFile()) ret = SUCCESS_FALSE;     
        else {
            if(LogManager.addLogEntry(sequenceNumber, collectionName, directory, cycle, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state, timestamp))
                ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
            else
                ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
        }
        
        return ret;
    }
        
    /**
     * POST method for commit an element in the database (for second phase of 2PC)
     * @param sequenceNumber
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/commit")
    @Produces(MediaType.TEXT_PLAIN)
    public String CommitEntry(@FormParam("sequenceNumber") int sequenceNumber) {
        String ret = "";
        LogEntry entry = null;
        LogEntry entryToCommit = null;
        
        ArrayList<LogEntry> entries = LogManager.readEntries();
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
                if(LogManager.writeEntries(entries))
                    ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
                else
                    ret = SUCCESS_FALSE;
            } else 
                ret = SUCCESS_FALSE;
        }
        
        return ret;
    }
    
    /**
     * POST method for abort a database operation (for second phase of 2PC)
     * @param sequenceNumber
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/abort")
    @Produces(MediaType.TEXT_PLAIN)
    public String AbortEntry(@FormParam("sequenceNumber") int sequenceNumber) {
        String ret = "";
        LogEntry entry = null;
        boolean bFound = false;
        
        ArrayList<LogEntry> entries = LogManager.readEntries();
        if(entries == null) {
            ret = SUCCESS_FALSE;
        } else {
            // Ricerca nel log dell'elemento da committare
            Iterator<LogEntry> iterator = entries.iterator();
            while(iterator.hasNext()) {
                entry = iterator.next();
                if(entry.getSequenceNumber() == sequenceNumber) {
                    iterator.remove();
                    bFound=true;
                    break;
                }
            }
            
            // Aggiornamento log
            if(bFound && LogManager.writeEntries(entries))
                ret = "{\"success\": true. \"sequenceNumber\": " + sequenceNumber + "}";
            else
                ret = SUCCESS_FALSE;
            
        }
        
        return ret;
    }
    
    /**
     * Writes a document in the specified database collection
     * @param entry
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
