FROM filesystemservice/glassfish_mongo

COPY target/ReplicaManager-1.0-SNAPSHOT.war /
COPY start.sh /

EXPOSE 8080


ENTRYPOINT ["/start.sh"]