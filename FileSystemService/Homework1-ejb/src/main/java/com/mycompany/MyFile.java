/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Orazio
 * @author Alessandro
 */
class MyFile {
    private String name;
    private String lastModified;
    private String lastAccessTime;
    private String creationTime;
    
    /**
     * 
     * @param name 
     */
    public MyFile(@JsonProperty("name") String name){
        this.name = name;
    }

    /**
     * 
     * @param name
     * @param lastModified
     * @param lastAccessTime
     * @param creationTime 
     */
    public MyFile(String name, String lastModified, String lastAccessTime, String creationTime) {
        this.name = name;
        this.lastModified = lastModified;
        this.lastAccessTime = lastAccessTime;
        this.creationTime = creationTime;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }
    
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(String lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
    
    
}
