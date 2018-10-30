FROM java:8
VOLUME /tmp
ARG JAR_FILE
ADD /api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
