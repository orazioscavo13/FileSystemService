/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Iterator;
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
    MongoClient mongo;
    MongoCredential credential; 
    MongoDatabase database;
    

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ReplicaResource
     */
    public ReplicaResource() {
        // Creating a Mongo client 
        mongo = new MongoClient( "localhost" , 27017 ); 

        // Creating Credentials 
        credential = MongoCredential.createCredential("FSDatabaseManagerAdmin", "FileSyistemDB", "password".toCharArray()); 
        System.out.println("Connected to the database successfully");  
        
        // Accessing the database 
        database = mongo.getDatabase("FileSyistemDB"); 
    }

      
    /**
     * DELETE method for deleting a collection
     * @param path the path of the directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @DELETE
    @Path("collections/{collectionName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteCollection(@PathParam("collectionName") String collectionName) {
      MongoCollection<Document> collection = database.getCollection(collectionName);

      collection.drop(); 
      return "Collection dropped successfully";
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
        BasicDBObject searchQuery = new BasicDBObject();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> iterDoc = collection.find();
        Iterator it = iterDoc.iterator(); 
    
        String out = "";
        while (it.hasNext()) { out = out + it.next() + (it.hasNext() ? ", " : ""); }
        
        return out;
    }
    
    /**
     * POST method for adding or creating an element in the DB
     * @param directory
     * @param cycle
     * @param meanAdd
     * @param meanDownload
     * @param stdDevAdd
     * @param stdDevDownload
     * @param state
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections")
    @Produces(MediaType.TEXT_PLAIN)
    public String AddEntry(@FormParam("collectionName") String collectionName, @FormParam("directory") String directory, @FormParam("cycle") int cycle, @FormParam("mean_add") double meanAdd, @FormParam("mean_download") double meanDownload, @FormParam("stddev_add") double stdDevAdd, @FormParam("stddev_download") double stdDevDownload, @FormParam("state") boolean state, @FormParam("timestamp") String timestamp) {
        
        // Create collection (if it does not exist) and insert a document
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document document = new Document()
            .append("directory", directory)
            .append("ciclo", cycle)
            .append("meanAdd", meanAdd)
            .append("meanDownload", meanDownload)
            .append("stdDevAdd", stdDevAdd)
            .append("stdDevDowdnload", stdDevDownload)
            .append("state", state)
            .append("timestamp", timestamp);
        collection.insertOne(document);
        
        return "Document inserted successfully";
    }
}
