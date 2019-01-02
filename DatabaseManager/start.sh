#!/bin/sh


/usr/local/glassfish4/bin/asadmin start-domain
/usr/local/glassfish4/bin/asadmin deploy /DatabaseManager-1.0-SNAPSHOT.war
/usr/local/glassfish4/bin/asadmin stop-domain
/usr/local/glassfish4/bin/asadmin start-domain --verbose