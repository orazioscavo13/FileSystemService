/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 *
 * @author orazio
 */
public class Directory {

    public ArrayList<MyFile> files;
    public ArrayList<Directory> directories;
    public String name;
    
    //TODO capire come funzionano i jsonproperty
    @JsonCreator
    public Directory(@JsonProperty("name") String name,@JsonProperty("directories") ArrayList<Directory> directories, @JsonProperty("files") ArrayList<Directory> files){
        this.name = name;
        this.files = new ArrayList<MyFile>();
        this.directories = new ArrayList<Directory>();
    }
    
    @JsonCreator
    public Directory(@JsonProperty("name") String name){
        this.name = name;
        this.files = new ArrayList<MyFile>();
        this.directories = new ArrayList<Directory>();
    }
    
}
