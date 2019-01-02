## -*- docker-image-name: "FileSystemService-java" -*-
# 

FROM glassfish:latest

COPY Homework1-ear/target/Homework1-ear-1.0-SNAPSHOT.ear /
COPY start.sh /

EXPOSE 8080

ENTRYPOINT ["/start.sh"]