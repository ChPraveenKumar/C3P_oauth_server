FROM jboss/wildfly:8.2.1.Final
EXPOSE 8083
ADD target/C3PCamunda.war /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0", "-bmanagement" ,"0.0.0.0"]