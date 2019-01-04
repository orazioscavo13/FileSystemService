FROM orazioscavo13/glassfish_mongo:1.0

COPY target/ReplicaManager-1.0-SNAPSHOT.war /
COPY start.sh /

EXPOSE 8080


ENTRYPOINT ["/start.sh"]