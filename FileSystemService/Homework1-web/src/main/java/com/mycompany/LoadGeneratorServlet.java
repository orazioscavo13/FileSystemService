/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * This class executes an automatic test, it launch a number of thread to execute the operation in a parallel way
 * @author Orazio
 * @author Alessandro
 */
@WebServlet(name = "LoadGeneratorServlet", urlPatterns = {"/LoadGeneratorServlet"})
public class LoadGeneratorServlet extends HttpServlet {
    private final static String QUEUE_NAME = "testResultQueue";
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods, in this method automatic test is executed
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        
        // Apre la connessione alla coda RabbitMQ
        TestResult result;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        Map<String, String> env = System.getenv();
        factory.setUsername(env.get("RABBITMQ_USERNAME"));
        factory.setPassword(env.get("RABBITMQ_PASSWORD"));
        
        // Manipolazione del file system per il test
        try {
            Files.createDirectories(Paths.get("../TesterFiles"));
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i = 1; i<=15; i++){
            File file = new File("../TesterFiles/file_" + i + ".txt");
            file.createNewFile();     
        }    
        
        RequestSenderService sender = new RequestSenderService();
        ExecutorService uploadThreadPool;
        ExecutorService downloadThreadPool;
        ArrayList<Callable<Long>> threadList;
        String table = 
                    "            <table style=\"width:100%; border: 1px solid rgba(0,0,0,0.5); border-radius:3px; box-shadow:1px 1px 2px rgba(0,0,0,0.5); padding: 1rem;\">\n" +
                    "                <thead>\n" +
                    "                    <th>\n" +
                    "                        Directory\n" +
                    "                    </th>\n" +
                    "                    <th>\n" +
                    "                        Cycle\n" +
                    "                    </th>\n" +
                    "                    <th>\n" +
                    "                        Add Mean\n" +
                    "                    </th>\n" +
                    "                    <th>\n" +
                    "                        Download Mean\n" +
                    "                    </th>\n" +
                    "                    <th>\n" +
                    "                        Add Standard Deviation\n" +
                    "                    </th>\n" +
                    "                    <th>\n" +
                    "                        Download Standard Deviation\n" +
                    "                    </th>\n" +
                    "                    <th>\n" +
                    "                        Cycle state\n" +
                    "                    </th>\n" +
                    "                </thead>\n" +
                    "                <tbody>";
        
        
        // Creazione di tre directory. Per ogni directory, esecuzione di 3 cicli di 5 add e 10 download per ciclo.
        for(int i=0; i<3 ;i++) {

            sender.createDirectory("Directory_" + i);
	
            // 3 cicli di 5 add e 10 download. Cicli sincroni, operazioni che sono analoghe tra loro dentro il singolo ciclo eseguite in modo asincrono
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
                } catch (InterruptedException | ExecutionException ex) {
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
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                } 
                result = new TestResult(n+1, "Directory_" + i, getMean(exeTimeAdd, 5), getMean(exeTimeDownload, 10), getStdDev(exeTimeAdd, 5), getStdDev(exeTimeDownload, 10), operationOutcome(exeTimeAdd, exeTimeDownload), (new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss")).format(new Date()));
                table = table +  
            "               <tr>\n" + 
            "                    <td>\n" +
                                    result.getDirectory() +
            "                    </td>\n" +
                                 "<td>\n" +
                                    (n+1) +
            "                    </td>\n" +
                                 "<td>\n" +
                                    result.getMeanAdd()+
            "                    </td>\n" +
            "                    <td>\n" +
                                    result.getMeanDownload() +
            "                    </td>\n" +
            "                    <td>\n" +
                                    result.getStdAdd()+
            "                    </td>\n" +
            "                    <td>\n" +
                                    result.getStdDownload() +
            "                    </td>" + 
            "                    <td>\n" +
                                    printOperationOutcome(result.getState()) +
            "                    </td>" +
            "               </tr>";
                System.out.println("------------------------------------");
                System.out.println("===== RISULTATI Directory " + i + ", ciclo " + (n+1) + "=====");
                System.out.println("Tempi Add: ");
                printArray(exeTimeAdd,5);
                System.out.println("Tempi Download: ");
                printArray(exeTimeDownload,10);
                System.out.println("Media Add: " + getMean(exeTimeAdd, 5));
                System.out.println("Deviazione standard Add: " + getStdDev(exeTimeAdd, 5));
                System.out.println("Media Download: " + getMean(exeTimeDownload, 10));
                System.out.println("Deviazione standard Download: " + getStdDev(exeTimeDownload, 10));
                System.out.println("------------------------------------");
                
                /*
                Publishing results on RabbitMQ Queue for persistent saving in the DB
                */
                try (Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()){
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    channel.basicPublish("", QUEUE_NAME, null, result.serialize());
                    System.out.println(" [x] Result Sent");
                } catch (TimeoutException ex) {
                    Logger.getLogger(LoadGeneratorServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        table = table + "</tbody>\n" +
        "            </table>\n";
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoadGeneratorServlet</title>");  
            out.println("<link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.6.1/css/all.css\" integrity=\"sha384-gfdkjb5BdAXd+lj+gudLWI+BXq4IuLW5IT+brZEZsLFm++aCMlF1V92rMkPaX4PP\" crossorigin=\"anonymous\">");
            out.println("</head>");
            out.println("<body>");
            out.println("<div>");
            out.println("<div style=\"text-align:center\">");
            out.println("<h1>Test Results</h1>");
            out.println("</div>");
            out.println("<div style=\"text-align:center; padding:0 2rem 0 2rem;\">");
            out.println(table);
            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
        
        //Clean the filesystem since test has finished
        sender.deleteDirectory("Directory_0");
        sender.deleteDirectory("Directory_1");
        sender.deleteDirectory("Directory_2");
        Path pathObj = Paths.get("../TesterFiles");
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
        } catch (IOException ex) {
            Logger.getLogger(DirectoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private double getMean(long[] data, double size) {
        long sum = 0;
        for(long a : data)
            sum += a;
        return sum/size;
    }

    private double getVariance(long[] data, double size) {
        double mean = getMean(data, size);
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    private double getStdDev(long[] data, double size) {
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
    
    private void printArray(long array[], int length){
        for (int i = 0; i<length; i++){
           System.out.print(array[i] + "   ");
        }
        System.out.println(" ");
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

    private int operationOutcome(long[] exeTimeAdd, long[] exeTimeDownload) {
        for (int i = 0; i<5; i++){
           if(exeTimeAdd[i]==-1) return 0;
        }
        for (int i = 0; i<10; i++){
           if(exeTimeDownload[i]==-1) return 0;
        }
        return 1;
    }
    
    private String printOperationOutcome(int state){
        if (state==0) return "<span class=\"fa fa-times-circle\" style=\"color: red;\"></span>";
        else return "<span class=\"fa fa-check-circle\" style=\"color: green;\"></span>";
        
    }

}
