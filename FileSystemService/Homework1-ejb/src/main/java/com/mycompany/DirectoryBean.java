/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;


/**
 * 
 * @author Orazio
 * @author Alessandro
 */
@Stateless
public class DirectoryBean implements DirectoryBeanLocal {
    
    private final String basePath = "../../../../../../../FileSystemService/";

    
    /* === DIRECTORIES METHODS === */
    
    /**
     * Create a new directory in the filesystem
     * @param path the path for the new directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @Override
    public String createDirectory(String path) {
        String newPath = UrlToPath(path);
        try {
            Files.createDirectory(Paths.get(newPath));
            return "{\"success\": true}";
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{\"success\": false, \"message\": \"Invalid Path\"}";
    }
    
    /**
     * Delete specified directory from Filesystem
     * @param path the path of the directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @Override
    public String deleteDirectory(String path) { 
       String newPath = UrlToPath(path);
       Path pathObj = Paths.get(newPath);
       if(!Files.isDirectory(pathObj)){
           return "{\"success\": false, \"message\": \"Secified Path is not a directory\"}";
       }
        try {
            Files.walkFileTree(pathObj, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return "{\"success\": true}";
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{\"success\": false, \"message\": \"Invalid Path\"}";
    }
    
    /**
     * Retrieves a list of directories in the specified path
     * @param path path of the directory with '*' instead of '/' 
     * @return List of all the directories in the Specified Directory
     */
    @Override
    public LinkedList<Directory> getDirectories(String path){

        LinkedList<Directory> directories = new LinkedList<Directory>();
        String newPath = UrlToPath(path);
        try {
            BasicFileAttributes attr = Files.readAttributes(Paths.get(newPath), BasicFileAttributes.class);
            Iterable<Path> entries = Files.newDirectoryStream(Paths.get(newPath));
            for (Path name: entries) {
                if(Files.isDirectory(name)){
                    
                    directories.add(new Directory(name.getFileName().toString(), attr.creationTime().toString(), attr.lastAccessTime().toString(), attr.lastModifiedTime().toString()));
                    System.out.println(name);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return directories;
    }
    
    
    /* === FILES METHODS === */
    
    /**
     * Delete a file specified in the path from the filesystem
     * @param path the path of the file with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @Override
    public String deleteFile(String path) {
       String file = UrlToPath(path);
       Path pathObj = Paths.get(file);
       if(Files.isDirectory(pathObj) || !Files.exists(pathObj)){
           return "{\"success\": false, \"message\": \"Secified Path is not a file\"}";
       }
        try {
            Files.delete(pathObj);
            return "{\"success\": true}";

        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
       return "{\"success\": false, \"message\": \"Invalid Path\"}";
    }
    
    /**
     * Retrieves a list of files in the specified path
     * @param path path of the directory with '*' instead of '/' 
     * @return List of all the files in the Specified Directory
     */
    @Override
    public LinkedList<MyFile> getFiles(String path) {
        LinkedList<MyFile> files = new LinkedList<MyFile>();
        String newPath = UrlToPath(path);
        try {
            BasicFileAttributes attr = Files.readAttributes(Paths.get(newPath), BasicFileAttributes.class);
            Iterable<Path> entries = Files.newDirectoryStream(Paths.get(newPath));
            for (Path name: entries) {
                if(!Files.isDirectory(name)){
                    files.add(new MyFile(name.getFileName().toString(), attr.creationTime().toString(), attr.lastAccessTime().toString(), attr.lastModifiedTime().toString()));
                    System.out.println(name);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return files;
    }
    
    /**
     * Get specific file from the filesystem
     * @param path the path of the requested file
     * @return the requested file
     */
    @Override
    public Response getFile(String path) {
        StreamingOutput fileStream;
        if(!Files.exists(Paths.get(UrlToPath(path)))){
            return Response.ok("{\"success\": false, \"message\": \"Il file richiesto non esiste!\"").build();
        }else{
            fileStream = new StreamingOutput(){
            String tmpPath = UrlToPath(path);
            @Override
            public void write(java.io.OutputStream output) throws IOException, WebApplicationException
            {
                try
                {
                    java.nio.file.Path path = Paths.get(tmpPath);
                    byte[] data = Files.readAllBytes(path);
                    output.write(data);
                    output.flush();
                }
                catch (Exception e)
                {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition","attachment; filename = " + Paths.get(UrlToPath(path)).getFileName().toString())
                .build();
        }
    }
    
    /**
     * Replace specific file in the filesystem (A file with the same name of the one uploaded must exixst in the specified destination)
     * @param fileInputStream inputstream from the file to be updated
     * @param filename the name of the file to be updated
     * @param destination path of the file to be updated
     * @return string containing the outcome of the operation
     */
    @Override
    public String updateFile(InputStream fileInputStream, String filename, String destination) {
        String path = UrlToPath(destination) + "/" + filename;
        if(Files.exists(Paths.get(path)))
            return simpleFileUpload(fileInputStream, path);
        else 
            return "{\"success\": false, \"message\": \"Non esiste un file con questo nome!\"";
    }
    
    /**
     * Upload a new file in the filesystem
     * @param fileInputStream inputstream from the file to be uploaded
     * @param filename the name of the file
     * @param destination destination for the file in the remote filesystem (path with '*' instead of '/')
     * @return string containing the outcome of the operation
     */
    @Override
    public String uploadFile(InputStream fileInputStream, String filename, String destination) {
       String path = UrlToPath(destination) + "/" + filename;
        if(!Files.exists(Paths.get(path)))
            return simpleFileUpload(fileInputStream, path);
        else 
            return "{\"success\": false, \"message\": \"Esiste già un file con questo nome!\"";
    }    
    
    
    /* === OTHERS === */

    /**
     * Write uploaded file into his new destination in the filesystem
     * @param fileInputStream
     * @param path destination path for the uploaded file
     * @return string containing the outcome of operation
     */
    private String simpleFileUpload(InputStream fileInputStream, String path){
        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            
            OutputStream out = null;
            out = new FileOutputStream(new File(path));
            while ((read = fileInputStream.read(bytes)) != -1){
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
            return "{\"success\": false, \"message\": \"Errore nel caricamento del file!\"";
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
            return "{\"success\": false, \"message\": \"Errore nella lettura del file!\"";
        }
        return "{\"success\": true}";
    }
    
    /**
     * Replace all the '*' in the given string with '/' to get a valid path
     * @param url the url to be converted
     * @return the converted url (with '/' instead of '*')
     */
    String UrlToPath(String url){
        String[] parts = url.split("\\*");
        String newPath = "";
        for(int i=0; i<parts.length;i++){
            newPath = newPath + "/" + parts[i];
        }
        return (this.basePath + newPath);
    }

}
