# FileSystemService
A web service for remote filesystem management.

## Table of Contents

- [References](#references)
- [Screenshots](#screenshots)
- [Requirements](#requirements)
- [Dependencies](#dependencies)
- [Running](#running-for-test)



## References

Javadoc is available at:

Docs/apidoc-ejb (EJB documentation)

Docs/apidoc-web  (REST service and web application documentation)

Docs/apidoc-DBManager  (DB Writer/Reader documentation)

Docs/apidoc-ReplicaManager  (Replica Manager documentation)



## Screenshots
![screenshot](screenshots/1.png)


![screenshot](screenshots/2.png)



## Requirements

- Java8EE
- maven-compiler-plugin 3.1
- MongoDB
- rabbitMQ-server


## Dependencies

- `jackson-annotations 2.6.0`
- `jackson-databind 2.6.7.1`
- `javaee-api 7.0`
- `javaee-web-api 7.0`
- `jersey-container-servlet-core 2.12`
- `jersey-media-multipart 2.13`
- `rabbitMQ amqp-client 5.5.1`
- `mongo-java-driver 3.4.1`



## Running for Test

#### 1 - Download or clone the project
```bash
git clone https://github.com/orazioscavo13/FileSystemService.git
```

#### 2 - Run the RabbitMQ Server
on windows:
```bash
rabbitmq-server start
```
on Linux:
```bash
sudo rabbitmq-server start
```
NB: you will need to enter your password to run the server



#### 3 - Open the project with Netbeans
#### 4 - Select "Build with Dependencies" on main module (Homework-1), on DatabaseManager module and in ReplicaManager module.
#### 5 - Run Homework1-ear, ReplicaManager, Database Manager (Follow this order to avoid runtime errors!)


#### 6 - Select and configure Web Server and environment
When you run the project for the first time you will need to select a web server. 

The project has been successfully tested on Glassfish Server 4.1 on ArchLinux, it may not work properly on some other platform.


NB: Depending on the web server you may need to change the port to the REST service in the frontend and in the RequestSender used by LoadGeneratorServlet (default is 43636):

- change the value of the attribute 'port' at RequestSenderService.java (line 28)
- change the value of the port in the baseUrl string in the js controller for the frontend page, mainController.js (line 10)
