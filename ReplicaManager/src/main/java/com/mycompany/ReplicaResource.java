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
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            credential = MongoCredential.createCredential("FSDatabaseManagerAdmin", "FileSystemDB", "password".toCharArray()); 
            System.out.println("Connected to the database successfully");  

            // Accessing the database 
            database = mongo.getDatabase("FileSystemDB");
        } catch(MongoClientException e) {
            Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, e);
            ret = false;
        }
        
        return ret;
    }
      
    /**
     * DELETE method for deleting all collection from the database
     * @return string containing the outcome of the operation
     */
    @DELETE
    @Path("collections/drop")
    @Produces(MediaType.TEXT_PLAIN)
    public String dropCollections() {
        String ret;
        if(connect()) {
            try{
                mongo.dropDatabase("FileSystemDB");
                ret = SUCCESS_TRUE;
            }catch(MongoException e){
                ret = SUCCESS_FALSE;
                Logger.getLogger(ReplicaResource.class.getName()).log(Level.SEVERE, null, e);
            }
        } else 
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * DELETE method for deleting a collection from the database
     * @param collectionName name of the db collection
     * @return string containing the outcome of the operation
     */
    @DELETE
    @Path("collections/{collection_name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteCollection(@PathParam("collection_name") String collectionName) {
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
     * @param collectionName name of the db collection
     * @return string containing the outcome of the operation and the collection's documents
     */
    @GET
    @Path("collections/{collection_name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCollection(@PathParam("collection_name") String collectionName) {
        String ret;
        if(connect()) {
            BasicDBObject searchQuery = new BasicDBObject();
            MongoCollection<Document> collection = database.getCollection(collectionName);
            FindIterable<Document> iterDoc = collection.find().projection(fields(excludeId()));
            Iterator it = iterDoc.iterator(); 
            Document temp;
            String out = "";
            int count = 0;
            long maxTimestamp = 0;
            Date date = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
            dateFormat.setLenient(false);
            while (it.hasNext()){ 
                temp = (Document) it.next();
                out = out + temp.toJson() + (it.hasNext() ? ", " : ""); 
                try {
                    date = dateFormat.parse((String) temp.get("timestamp"));
                    if(date.getTime() > maxTimestamp){
                        maxTimestamp = date.getTime();
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                count++;
            }
            ret = "{\"success\": true, \"maxTimestamp\": " + maxTimestamp + ", \"number\":" + count + ", \"documents\": [" + out + "]}";
        } else
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * Return the max sequence number in use in the log file
     * @return string containing the outcome of the operation and the collection's documents
     */
    @GET
    @Path("collections/logfile/maxSequenceNumber")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMaxSequenceNumber() {
        LogManager.checkLogFile();
        int maxSeqNum = LogManager.getMaxSequenceNumber();
        return "{\"success\": true, \"sequence_number\":" + ((maxSeqNum == -1) ? "0" : maxSeqNum) + "}";
    }
    
    /**
     * Retrieves the last document committed in the specified collection
     * @param collectionName name of the db collection
     * @return string containing the outcome of the operation and the last committed document
     */
    @GET
    @Path("collections/{collection_name}/lastCommittedDocument")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastCommittedDocument(@PathParam("collection_name") String collectionName) {
        String ret;
        if(connect()) {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            FindIterable<Document> iterDoc = collection.find(new BasicDBObject()).sort(new BasicDBObject("_id", -1)).projection(fields(excludeId()));
            Iterator it = iterDoc.iterator();
            if(it.hasNext()) { 
                Document temp = (Document) it.next();
                Date date = null;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                    dateFormat.setLenient(false);
                    date = dateFormat.parse((String) temp.get("timestamp"));
                    ret = "{\"success\": true, \"number\": 1,\"maxTimestamp\": " + date.getTime() + ", \"lastCommittedDocument\": " + temp.toJson() + "}";
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    ret = SUCCESS_FALSE;
                }
            } else
                ret = SUCCESS_FALSE;
        } else
            ret = SUCCESS_FALSE;
        
        return ret;
    }
    
    /**
     * POST method for adding or creating an element in the log file (for first phase of 2PC)
     * @param sequenceNumber sequence number of the db write operation
     * @param collectionName name of the db collection
     * @param directory directory of the test result
     * @param cycle cycle of the test result
     * @param meanAdd Mean Execution time for the add operations in the submitted test
     * @param meanDownload Mean Execution time for the downloadoperations in the submitted test
     * @param stdDevAdd Standard Deviation of Execution time for the add operations in the submitted test
     * @param stdDevDownload Standard Deviation of Execution time for the download operations in the submitted test
     * @param state state of the test result
     * @param timestamp timestamp of the test result
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections")
    @Produces(MediaType.TEXT_PLAIN)
    public String addEntry(@FormParam("sequence_number") int sequenceNumber, @FormParam("collection_name") String collectionName, @FormParam("directory") String directory, @FormParam("cycle") int cycle, @FormParam("mean_add") double meanAdd, @FormParam("mean_download") double meanDownload, @FormParam("stddev_add") double stdDevAdd, @FormParam("stddev_download") double stdDevDownload, @FormParam("state") int state, @FormParam("timestamp") String timestamp) {
        String ret = "";
        
        if(!LogManager.checkLogFile()) ret = SUCCESS_FALSE;     
        else {
            if(LogManager.addLogEntry(sequenceNumber, collectionName, directory, cycle, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state, timestamp))
                ret = "{\"success\": true, \"sequence_number\": " + sequenceNumber + "}";
            else
                ret = "{\"success\": true, \"sequence_number\": " + sequenceNumber + "}";
        }
        
        return ret;
    }
        
    /**
     * POST method for commit an element in the database (for second phase of 2PC)
     * @param sequenceNumber sequence number of the write db operation
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/commit")
    @Produces(MediaType.TEXT_PLAIN)
    public String commitEntry(@FormParam("sequence_number") int sequenceNumber) {
        String ret = "";
        LogEntry entry = null;
        LogEntry entryToCommit = null;
        
        LogManager.checkLogFile();     
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
            
            // saving into the db
            if(entryToCommit != null && writeDbEntry(entryToCommit)) {
                
                // updating log if write succeds
                if(LogManager.writeEntries(entries))
                    ret = "{\"success\": true, \"sequence_number\": " + sequenceNumber + "}";
                else
                    ret = SUCCESS_FALSE;
            } else 
                ret = SUCCESS_FALSE;
        }
        
        return ret;
    }
    
    /**
     * POST method for abort a database operation (for second phase of 2PC)
     * @param sequenceNumber sequence number of the write db operation
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/abort")
    @Produces(MediaType.TEXT_PLAIN)
    public String abortEntry(@FormParam("sequence_number") int sequenceNumber) {
        String ret = "";
        LogEntry entry = null;
        boolean bFound = false;
        
        LogManager.checkLogFile();     
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
                ret = "{\"success\": true, \"sequence_number\": " + sequenceNumber + "}";
            else
                ret = SUCCESS_FALSE;
            
        }
        
        return ret;
    }
    
    /**
     * Writes a document in the specified database collection
     * @param entry Log Entry to be moved in the db for the second phase of 2PC
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
