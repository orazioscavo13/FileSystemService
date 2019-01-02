## -*- docker-image-name: "databasemanager-java" -*-
# 

FROM glassfish:latest

COPY target/DatabaseManager-1.0-SNAPSHOT.war /
COPY start.sh /

EXPOSE 8080

ENTRYPOINT ["/start.sh"]