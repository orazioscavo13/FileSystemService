/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author orazio
 */
class MyFile {
    public String name;

    public MyFile(@JsonProperty("name") String name){
        this.name = name;
    }
}
