/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * This REST Web Service is used to perform read and write operation on the db based on a quorum protocol 
 *
 * @author Orazio
 * @author Alessandro
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


    /* REST METHODS */    
    
    /**
     * Drops replicas' databases collections. NB: for develop use only.
     * @return string containing the outcome of the operation 
     */
    @DELETE
    @Path("collections/drop")
    @Produces(MediaType.TEXT_PLAIN)
    public String dropCollections() {
        return transactionManager.dropCollections("drop");
    }
    
    /**
     * Retrieves all documents in the specified collection
     * @param collectionName the name of the db collection
     * @return string containing the outcome of the operation and the collection's documents
     */
    @GET
    @Path("collections/{collection_name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCollection(@PathParam("collection_name") String collectionName) {
        return transactionManager.quorumRead(collectionName);
    }
    
    /**
     * Retrieves the last document committed in the specified collection
     * @param collectionName the name of the db collection
     * @return string containing the outcome of the operation and the last committed document
     */
    @GET
    @Path("collections/{collection_name}/lastCommittedDocument")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastCommittedDocument(@PathParam("collection_name") String collectionName) {
        return transactionManager.quorumRead(collectionName + "/lastCommittedDocument");
    }
    
    /**
     * Start a 2PC to insert a new entry in the db
     * @param collectionName the name of the db collection
     * @param directory directory of the submitted test
     * @param cycle cycle of the submitted test
     * @param meanAdd Mean Execution time for the add operations in the submitted test
     * @param meanDownload Standard Deviation of Execution time for the download operations in the submitted test
     * @param stdDevAdd Standard Deviation of Execution time for the add operations in the submitted test
     * @param stdDevDownload Standard Deviation of Execution time for the download operations in the submitted test
     * @param state state of the submitted test
     * @param timestamp timestamp of the submitted test
     * @return string containing the outcome of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("collections/write")
    @Produces(MediaType.TEXT_PLAIN)
    public String twoPhaseCommitWrite(@FormParam("collection_name") String collectionName, @FormParam("directory") String directory, @FormParam("cycle") int cycle, @FormParam("mean_add") double meanAdd, @FormParam("mean_download") double meanDownload, @FormParam("stddev_add") double stdDevAdd, @FormParam("stddev_download") double stdDevDownload, @FormParam("state") int state, @FormParam("timestamp") String timestamp) {
        return transactionManager.twoPhaseCommitWrite(new TestResult(cycle, directory, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state, timestamp), collectionName);
    }
    
    /**
     * Retrieves the number of replicas managed
     * @return the number of replicas managed
     */
    @GET
    @Path("replicas")
    @Produces(MediaType.TEXT_PLAIN)
    public String getReplicasNumber() {
        return transactionManager.getReplicas();
    }
}
