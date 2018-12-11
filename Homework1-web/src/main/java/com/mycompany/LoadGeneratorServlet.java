/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.LongStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Orazio & Alessandro
 */
@WebServlet(name = "LoadGeneratorServlet", urlPatterns = {"/LoadGeneratorServlet"})
public class LoadGeneratorServlet extends HttpServlet {
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        RequestSenderService sender = new RequestSenderService();
        ExecutorService uploadThreadPool;
        ExecutorService downloadThreadPool;
        ArrayList<Callable<Long>> threadList;
        
        // Creazione di tre directory. Per ogni directory, esecuzione di 3 cicli di 5 add e 10 download per ciclo.
      
        for(int i=0; i<3 ;i++) {

            sender.createDirectory("Directory_" + i);
	
            // 3 cicli di 5 add e 10 download. Cicli sincroni, op che sono analoghe tra loro dentro il singolo ciclo eseguite in modo asincrono
            for(int n=0;n<3;n++) {
		long exeTimeAdd[] = new long[5];
		long exeTimeDownload[] = new long[10];
                uploadThreadPool = Executors.newFixedThreadPool(5);
                downloadThreadPool = Executors.newFixedThreadPool(10);
		
                // Esegue 5 add in parallelo
                threadList = new ArrayList<Callable<Long>>();
		for(int j=0; j<5; j++) {
                    try {
                        threadList.add(new TestThread("../TesterFiles/file_" + (j+1)*(n+1) + ".txt", "*Directory_" + i));
                    } catch (Exception ex) {
                        Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
		}
                try {
                    ArrayList<Future<Long>> futures = (ArrayList<Future<Long>>) uploadThreadPool.invokeAll(threadList);
                    awaitTerminationAfterShutDown(uploadThreadPool);
                    for(int j=0; j<5; j++) {
                        exeTimeAdd[j] = futures.get(j).get();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                } 

                // 10 download
                threadList = new ArrayList<Callable<Long>>();
		for(int j=0; j<10; j++) {
                    try {
                        threadList.add(new TestThread("Directory_" + i + "*file_" + ((int)(Math.random() * 4) + 1)*(n+1) + ".txt"));
                    } catch (Exception ex) {
                        Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
		}
                try {
                    ArrayList<Future<Long>> futures = (ArrayList<Future<Long>>) downloadThreadPool.invokeAll(threadList);
                    awaitTerminationAfterShutDown(downloadThreadPool);
                    for(int j=0; j<10; j++) {
                        exeTimeDownload[j] = futures.get(j).get();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                } 
                
                System.out.println("------------------------------------");
                System.out.println("===== RISULTATI Directory " + i + ", ciclo " + n+1 + "=====");
                System.out.println("Media Add: " + getMean(exeTimeAdd, 5));
                System.out.println("Deviazione standard Add: " + getStdDev(exeTimeAdd, 5));
                System.out.println("Media Download: " + getMean(exeTimeDownload, 10));
                System.out.println("Deviazione standard Download: " + getStdDev(exeTimeDownload, 10));
                System.out.println("------------------------------------");
            }
        }
        
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoadGeneratorServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LoadGeneratorServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    private double getMean(long[] data, int size) {
        long sum = 0;
        for(long a : data)
            sum += a;
        return sum/size;
    }

    private double getVariance(long[] data, int size) {
        double mean = getMean(data, size);
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    private double getStdDev(long[] data, int size) {
        return Math.sqrt(getVariance(data,size));
    }
    
    public void awaitTerminationAfterShutDown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
