FROM containers.cisco.com/aws_managed/tomcat-7-jws3.1_custom
## Maintainer ##
MAINTAINER Lae2Cae-Operations
USER root
EXPOSE 8080
COPY package/repo  ${HOME}/lae-home/app-root/runtime/repo
RUN chmod -R 777  ${HOME}/lae-home/app-root/runtime/repo
# Add Deployment WAR
RUN echo "please copy your war file path as"
ADD package/dependencies/jbossews/webapps/*.war ${JWS_HOME}/webapps/
#ADD caeconfig.zip  ${JWS_HOME}/webapps/logcso/WEB-INF/classes
#WORKDIR ${JWS_HOME}/webapps/logcso/WEB-INF/classes
#RUN chmod -R 777 caeconfig.zip
#RUN unzip -q caeconfig.zip 
#For ojdbc6.jar keep the default value and for classes12.jar which is part of the code, change the value to '' (empty)
ENV CUSTOM_CLASSPATH='/usr/lib/oracle/11.2/client64/lib/ojdbc6.jar'
RUN echo "Building Application Image!"
ADD http://64.102.209.160/files/AppServerAgent-1.8-21.11.4.33358.zip /tmp
RUN rm -fR /opt/AppDServerAgent && mkdir -p /opt/AppDServerAgent && unzip -oq /tmp/AppServerAgent-1.8-21.11.4.33358.zip -d /opt/AppDServerAgent && chmod -R 777 /opt/AppDServerAgent
USER default
# Main Command
CMD ${JWS_HOME}/bin/serverStart.sh
