#!/bin/sh


/usr/local/glassfish4/bin/asadmin start-domain
/usr/local/glassfish4/bin/asadmin deploy /Homework1-ear-1.0-SNAPSHOT.ear  
/usr/local/glassfish4/bin/asadmin stop-domain
/usr/local/glassfish4/bin/asadmin start-domain --verbose