/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.util.LinkedList;
import javax.ejb.Stateless;

/**
 * 
 * @author Orazio & Alessandro
 */
@Stateless
public class DirectoryBean implements DirectoryBeanLocal {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    /**
     *
     * @return List of all the directories in the FileSyistem
     */
    @Override
    public LinkedList<Directory> getDirectories(){
        LinkedList<Directory> directories;
        directories = new LinkedList<Directory>();
        directories.add(new Directory("Cartellabella"));
        directories.add(new Directory("Cartellabrutta"));
        return directories;
    }
}
