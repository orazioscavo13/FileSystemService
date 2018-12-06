/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import javax.ejb.Local;
import javax.servlet.http.Part;

/**
 * 
 * @author Orazio & Alessandro
 */
@Local
public interface DirectoryBeanLocal {
    
    public String createDirectory(String path);
    
    public LinkedList<Directory> getDirectories(String path);
    
    public LinkedList<MyFile> getFiles(String path);
    
    public String deleteDirectory(String path);
    
    public String uploadFile(String path, String fileName, InputStream uploadedInputStream);
    
    public File getFile(String path);

    public String updateFile(String path, String name, InputStream uploadedInputStream);
    
    public String deleteFile(String path);    
    
}
