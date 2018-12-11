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
import java.io.PrintWriter;
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
import javax.servlet.http.Part;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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
    
    /**
     * 
     * @param path the destination for the new file
     * @param fileName the name for the new file
     * @param uploadedInputStream
     * @param filePart 
     * @return string containing the outcome of the operation
     */

    @Override
    public String uploadFile(InputStream fileInputStream, String filename, String destination) {
       String path = "../FileSystemService/" + UrlToPath(destination) + "/" + filename;
        if(!Files.exists(Paths.get(path)))
        return simpleFileUpload(fileInputStream, path);
        else return "{\"success\": false, \"message\": \"Esiste già un file con questo nome!\"";
    }    
    
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

    @Override
    public Response getFile(String path) {
        StreamingOutput fileStream;
        fileStream = new StreamingOutput(){
            String tmpPath = "../FileSystemService/" + UrlToPath(path);
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
                .header("content-disposition","attachment; filename = myfile.pdf")
                .build();
    }

    @Override
    public String updateFile(InputStream fileInputStream, String filename, String destination) {
        String path = "../FileSystemService/" + UrlToPath(destination) + "/" + filename;
        if(Files.exists(Paths.get(path)))
        return simpleFileUpload(fileInputStream, path);
        else return "{\"success\": false, \"message\": \"Non esiste un file con questo nome!\"";
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
