# FileSystemService
A web service for remote filesystem management.

## Table of Contents

- [References](#references)
- [Screenshots](#screenshots)
- [Requirements](#requirements)
- [Technologies used](#technologies-used)
- [Running](#running-for-test)

## References

Javadoc is available at:
Docs/apidoc-ejb (EJB documentation)
Docs/apidoc-web  (REST service and web application documentation)

## Screenshots

![screenshot](screenshots/1.png)

![screenshot](screenshots/2.png)

## Requirements

- Java8EE
- maven-compiler-plugin 3.1

## Technologies used

- `jackson-annotations 2.6.0`
- `jackson-databind 2.6.7.1`
- `javaee-api 7.0`
- `javaee-web-api 7.0`
- `jersey-container-servlet-core 2.12`
- `jersey-media-multipart 2.13`

## Running for Test

#### Download or clone the project
```bash
git clone https://github.com/orazioscavo13/FileSystemService.git
```

Then open the project with Netbeans, select "Build with Dependencies" on main module.
Run Homework1-ear


#### Web Server
When you run the project for the first time you will need to select a web server. The project has been successfully tested on Glassfish Server 4.1, it may not work properly on some other platform.

IMPORTANT!
Depending on the web server u may need to change the port to the REST service in the frontend and in the RequestSender used by LoadGeneratorServlet:

- change the value of the attribute 'port' at RequestSenderService.java (line 28)
- TODO: insert instruction to set port in frontend application