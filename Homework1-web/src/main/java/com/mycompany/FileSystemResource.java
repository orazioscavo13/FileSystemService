/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
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
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author orazio
 */
@Path("directory")
@RequestScoped
public class FileSystemResource {

    com.mycompany.DirectoryBeanLocal directoryBean = lookupDirectoryBeanLocal();

    
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FileSystemResource
     */
    public FileSystemResource() {
    }

    /**
     * Retrieves representation of an instance of com.mycompany.FileSystemResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getJson() {
        ArrayList<Directory> directories = directoryBean.getDirectories();
        //TODO return proper representation object
        //throw new UnsupportedOperationException();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String stringifiedDirectories = mapper.writeValueAsString(directories);
            JsonObject jsonRet = Json.createObjectBuilder()
                .add("directories", stringifiedDirectories)
                .add("success", true)
                .build();
            return jsonRet;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(FileSystemResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Json.createObjectBuilder()
                .add("success", false)
                .build();
        /*
        JsonObject json = Json.createObjectBuilder()
                .add("directories", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add("name", "test1"))
                        .add(Json.createObjectBuilder().add("name", "test2"))
                     )
                .build();
        */
        
    }

    /**
     * PUT method for updating or creating an instance of FileSystemResource
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(String content) {
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

    

    
    
    
}
