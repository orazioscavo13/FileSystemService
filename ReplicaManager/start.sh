#!/bin/sh

mongod --dbpath /dbData &
/usr/local/glassfish4/bin/asadmin start-domain
/usr/local/glassfish4/bin/asadmin deploy /ReplicaManager-1.0-SNAPSHOT.war
/usr/local/glassfish4/bin/asadmin stop-domain
/usr/local/glassfish4/bin/asadmin start-domain --verbose
