/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 *
 * @author orazio
 * @author alessandro
 * 
 */
public class Directory {

    private LinkedList<MyFile> files;
    private LinkedList<Directory> directories;
    private String name;
    
    /**
     * 
     * @param name the name of the directory
     * @param directories list of innder directories
     * @param files list of files in the directory
     */
    public Directory(String name, LinkedList<Directory> directories, LinkedList<Directory> files){
        this.name = name;
        this.files = new LinkedList<MyFile>();
        this.directories = new LinkedList<Directory>();
    }
    
    /**
     * 
     * @param name the name of the directory
     */
    public Directory(String name){
        this.name = name;
        this.files = new LinkedList<MyFile>();
        this.directories = new LinkedList<Directory>();
    }
    
    /**
     *
     * @return the list of files in the directory
     */
    @JsonProperty("files")
    public LinkedList<MyFile> getFiles() {
        return files;
    }
    
    /**
     *
     * @return the list of directories in the directory
     */
    @JsonProperty("directories")
    public LinkedList<Directory> getDirectories() {
        return directories;
    }
    
    /**
     *
     * @return the name of current directory
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    
    /**
     *
     * @param name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * creates a new directory in the current directory
     * @param name
     */
    public void addDirectory(String name) {
        this.directories.add(new Directory(name));
    }
    
    /**
     * creates a new file in the current directory
     * @param name
     */
    public void addFile(String name) {
        this.files.add(new MyFile(name));
    }
    
    /**
     * removes the directory with the specified name from current directory
     * @param name the name of the directory to be deleted
     */
    public void removeDirectory(String name) {
        this.directories.removeIf(p->p.getName().equals(name));
    }
    
    /**
     * removes the file with the specified name from current directory
     * @param name the name of the file to be deleted
     */
    public void removeFile(String name) {
        this.files.removeIf(p->p.getName().equals(name));
    }
    
}
