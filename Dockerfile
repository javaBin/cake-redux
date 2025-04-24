# Fetch the application jar from the target directory and rename it to application.jar
FROM amazoncorretto:21-al2023-headless AS fetch

COPY target /target
RUN mv /target/*-with-dependencies.jar /target/application.jar


# Actual image to run the application
FROM amazoncorretto:21-al2023-headless

EXPOSE 8082

COPY --from=fetch /target/application.jar /work/application.jar

ENTRYPOINT ["java", "-jar", "/work/application.jar"]