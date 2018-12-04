/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 * 
 * @author Orazio & Alessandro
 */
@Path("filesystem")
@RequestScoped
public class FileSystemResource {

    com.mycompany.DirectoryBeanLocal directoryBean = lookupDirectoryBeanLocal();

    
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FileSystemResource
     */
    public FileSystemResource() {
        try {
            Files.createDirectories(Paths.get("../FileSystemService"));
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private DirectoryBeanLocal lookupDirectoryBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (com.mycompany.DirectoryBeanLocal) c.lookup("java:global/Homework1-ear/Homework1-ejb-1.0-SNAPSHOT/DirectoryBean!com.mycompany.DirectoryBeanLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
    
    /**
     * POST method for creating a new directory in the specified path
     * @param path the path of the new directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @POST
    @Path("directories")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String createDirectory(@FormParam("path") String path) {
        return directoryBean.createDirectory(path);
    }


    /**
     * Retrieves the list of the directories in the specified directory
     * @param path path of the directory with '*' instead of '/' (just '*' for the root directory)
     * @return Json Object containing the list of directories in the specified directory
     */
    @GET
    @Path("directories/{path}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDirectories(@PathParam("path") String path) {
        
        LinkedList<Directory> directories = directoryBean.getDirectories(path);
        //TODO return proper representation object
        //throw new UnsupportedOperationException();
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            String stringifiedDirectories = mapper.writeValueAsString(directories);
            //TODO: utilizzare un metodo di serializzazione/deserializzazione migliore
            return "{\"success\": true, \"directories\":" + stringifiedDirectories + "}"; 
        } catch (JsonProcessingException ex) {
            Logger.getLogger(FileSystemResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{\"success\": false}";
    }
    
    /**
     * Retrieves the list of files in the specified directory
     * 
     * @param path path of the directory with '*' instead of '/' (just '*' for the root directory)
     * @return Json Object containing the list of files in the specified directory
     */
    @GET
    @Path("files/{path}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFiles(@PathParam("path") String path) {
        
        LinkedList<MyFile> files = directoryBean.getFiles(path);
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            String stringifiedFiles = mapper.writeValueAsString(files);
            //TODO: utilizzare un metodo di serializzazione/deserializzazione migliore
            return "{\"success\": true, \"files\":" + stringifiedFiles + "}"; 
        } catch (JsonProcessingException ex) {
            Logger.getLogger(FileSystemResource.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return "{\"success\": false}";
    }
    
    /**
     * DELETE method for deleting a directory
     * @param path the path of the directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("directories/{path}")
    public String deleteDirectory(@PathParam("path") String path) {
        return directoryBean.deleteDirectory(path);
    }
    
    /**
     * DELETE method for deleting a file
     * @param path the path of the file with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("files/{path}")
    public String deleteFile(@PathParam("path") String path) {
        return directoryBean.deleteFile(path);
    }
    
}
