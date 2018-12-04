/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
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

/**
 * 
 * @author Orazio & Alessandro
 */
@Stateless
public class DirectoryBean implements DirectoryBeanLocal {
    
    String UrlToPath(String url){
        String[] parts = url.split("\\*");
        String newpath = "";
        for(int i=0; i<parts.length;i++){
            newpath = newpath + "/" + parts[i];
        }
        return newpath;
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    /**
     *
     * @param path path of the directory with '*' instead of '/' 
     * @return List of all the directories in the Specified Directory
     */
    @Override
    public LinkedList<Directory> getDirectories(String path){
        LinkedList<Directory> directories = new LinkedList<Directory>();
        String newpath = UrlToPath(path);
        try {
            Iterable<Path> entries = Files.newDirectoryStream(Paths.get("../FileSystemService" + newpath));
            for (Path name: entries) {
                if(Files.isDirectory(name)){
                    directories.add(new Directory(name.getFileName().toString()));
                    System.out.println(name);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return directories;
        /*
        Iterable<Path> dirs = FileSystems.getDefault().getRootDirectories();
        for (Path name: dirs) {
            System.out.println(name);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                System.out.println(file.getFileName());
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
        LinkedList<Directory> directories;
        directories = new LinkedList<Directory>();
        Directory cartella = new Directory("Cartellabella");
        cartella.addFile("Nuovo_Documento");
        directories.add(cartella);
        directories.add(new Directory("Cartellabrutta"));
*/
    }
    
    /**
     * 
     * @param path the path for the new directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @Override
    public String createDirectory(String path) {
        //TODO: aggiungere informazioni su creazione e ultima modifica agli oggetti File e Directory
        String newpath = UrlToPath(path);
        try {
            Files.createDirectory(Paths.get("../FileSystemService" + "/" + newpath));
            return "{\"success\": true}";
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{\"success\": false, \"message\": \"Invalid Path\"}";
    }
    
    /**
     * 
     * @param path path of the directory with '*' instead of '/' 
     * @return List of all the files in the Specified Directory
     */
    @Override
    public LinkedList<MyFile> getFiles(String path) {
        LinkedList<MyFile> files = new LinkedList<MyFile>();
        String newpath = UrlToPath(path);
        try {
            Iterable<Path> entries = Files.newDirectoryStream(Paths.get("../FileSystemService" + newpath));
            for (Path name: entries) {
                if(!Files.isDirectory(name)){
                    files.add(new MyFile(name.getFileName().toString()));
                    System.out.println(name);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return files;
    }
    
    /**
     * 
     * @param path the path of the directory with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @Override
    public String deleteDirectory(String path) { //TODO: eliminare anche cartelle non vuote
       String directory = UrlToPath(path);
       Path pathObj = Paths.get("../FileSystemService" + "/" + path);
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

    @Override
    public String createFile(String path, String name, File newFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getFile(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String updateFile(String path, String name, File newFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * 
     * @param path the path of the file with '*' instead of '/'
     * @return string containing the outcome of the operation
     */
    @Override
    public String deleteFile(String path) {
       String file = UrlToPath(path);
       Path pathObj = Paths.get("../FileSystemService" + "/" + file);
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
}
