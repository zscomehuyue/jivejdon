
FROM centos/s2i-core-centos7
#FROM tomcat:8.5-jre8-alpine

RUN yum install -y yum-utils && \
    yum -y clean all --enablerepo='*'


# add JNDI config 
#ADD tomcat/conf/context.xml /usr/local/tomcat/conf/
#
##add jass lib and config
#ADD tomcat/conf/server.xml /usr/local/tomcat/conf/
#ADD tomcat/conf/jaas.config /usr/local/tomcat/conf/
#ADD tomcat/lib/jaas.jar /usr/local/tomcat/lib/
#
##add javamail and activation.jar
#ADD tomcat/lib/email.jar      /usr/local/tomcat/lib/
#
## # add MySQL JDBC driver jar
#ADD tomcat/lib/mysql-connector-java-5.0.8-bin.jar /usr/local/tomcat/lib/
#
#RUN rm -rf /usr/local/tomcat/webapps/
#
#ADD target/ROOT.war /usr/local/tomcat/webapps/
#
## # create mount point for volume with application
#WORKDIR /usr/local/tomcat/webapps/
#
#
## # add tomcat jpda debugging environmental variables
#ENV JPDA_OPTS="-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n"
#ENV JPDA_ADDRESS="8000"
#ENV JPDA_TRANSPORT="dt_socket"
#
## # start tomcat with remote debugging
# EXPOSE 8080 8000
#
# WORKDIR /usr/local/tomcat/bin
# RUN ls -l
# RUN JAVA_OPTS="-XX:+UseStringCache   -Djava.security.auth.login.config=../conf/jaas.config"
# CMD ["catalina.sh", "jpda", "run"]

