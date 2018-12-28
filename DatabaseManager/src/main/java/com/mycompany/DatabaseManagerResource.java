/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.util.LinkedList;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author Orazio
 */
@Path("mongodb")
public class DatabaseManagerResource {
    private TransactionManager transactionManager;

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of DatabaseManagerResource
     */
    public DatabaseManagerResource(){
        transactionManager = TransactionManager.getInstance();
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
        return transactionManager.quorumRead(collectionName);
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
        return transactionManager.quorumRead(collectionName + "/lastCommittedDocument");
    }
    
    /**
     * 
     * @param collectionName
     * @param directory
     * @param cycle
     * @param meanAdd
     * @param meanDownload
     * @param stdDevAdd
     * @param stdDevDownload
     * @param state
     * @param timestamp
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/commit")
    @Produces(MediaType.TEXT_PLAIN)
    public String commitEntry(@FormParam("collectionName") String collectionName, @FormParam("directory") String directory, @FormParam("cycle") int cycle, @FormParam("mean_add") double meanAdd, @FormParam("mean_download") double meanDownload, @FormParam("stddev_add") double stdDevAdd, @FormParam("stddev_download") double stdDevDownload, @FormParam("state") int state, @FormParam("timestamp") String timestamp) {
        return transactionManager.twoPhaseCommitWrite(new TestResult(cycle, directory, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state, timestamp), collectionName);
    }
}
