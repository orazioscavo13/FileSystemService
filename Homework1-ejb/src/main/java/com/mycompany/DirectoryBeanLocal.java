/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.util.LinkedList;
import javax.ejb.Local;

/**
 * 
 * @author Orazio & Alessandro
 */
@Local
public interface DirectoryBeanLocal {
    
    public LinkedList<Directory> getDirectories();
    
    
}
