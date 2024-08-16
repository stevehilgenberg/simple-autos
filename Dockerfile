# Uses AdoptOpenJDK 11 alpine image.  "Alpine" is a version that is slimed down from the full JDK, but has everything that we need.
FROM eclipse-temurin:17-alpine

# Set the working directory inside the container when run, here it is root
WORKDIR /app

# Take the jar from the build folder and COPY it over as app.jar. This will require there to be a build already in that directory. Please modify the first path to your needs.

# Grade jar path:
COPY build/libs/*SNAPSHOT.jar app.jar

# Maven jar path
# COPY target/*SNAPSHOT.jar app.jar

# Remember how we mapped container ports to our host's port? This is how you expose the port you wish. It exposes PORT 8080.
EXPOSE 8080

# Invoke java executable and run the app.jar file. There is only ONE CMD instruction in a Dockerfile and it is used as default to executing the container. The CMD form can vary, refer to the Docker Docs: Dockerfile Reference on formatting these shell commands. Another, commonly used, command is `ENTRYPOINT ["java", "-jar", "app.jar"]`
CMD java -jar app.jar