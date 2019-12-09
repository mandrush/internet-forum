FROM openjdk:8-jre-alpine

COPY run/app/yourconfig.conf application.conf
ADD target/scala-2.13/internet-forum-assembly-0.1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dconfig.file=/application.conf", "-jar","/app.jar"]