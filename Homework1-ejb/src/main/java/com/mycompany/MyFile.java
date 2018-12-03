/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * 
 * @author Orazio & Alessandro
 */
class MyFile {
    private String name;
    
    /**
     * 
     * @param name the name for the new file
     */
    public MyFile(@JsonProperty("name") String name){
        this.name = name;
    }
    
    /**
     * 
     * @return the name of the file
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    
    /**
     * 
     * @param name new name for the file
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    
}
