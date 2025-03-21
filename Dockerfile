# build angular
FROM node:23 AS ng-build

WORKDIR /src

RUN npm i -g @angular/cli

# copy required files from client
COPY client/src src
COPY client/*.json .
COPY client/public public

# run npm to install node_modules > package.json
RUN npm ci
RUN npm i -g @angular/cli

# produce dist/client/browser
RUN ng build

# build spring boot
# can also use eclipse-temurin:23-jre
FROM openjdk:23-jdk AS j-build

WORKDIR /src

# copy required files from server
COPY server/.mvn .mvn
COPY server/src src
COPY server/mvnw .
COPY server/pom.xml .

# copy angular files over to static
COPY --from=ng-build /src/dist/client/browser src/main/resources/static

# make mvnw executable
RUN chmod a+x mvnw
RUN ./mvnw package -Dmaven.test.skip=true

# copy jar file over to final container
# can use eclipse-temurin:23-jre
FROM eclipse-temurin:23-jre

WORKDIR /app

# from server
COPY --from=j-build /src/target/server-0.0.1-SNAPSHOT.jar app.jar

# set env variables needed for app to run
ENV PORT=8080

ENV SPRING_DATASOURCE_URL=
ENV SPRING_DATASOURCE_USERNAME=
ENV SPRING_DATASOURCE_PASSWORD=

ENV SPRING_DATA_MONGODB_URI=

EXPOSE ${PORT}

SHELL [ "/bin/sh", "-c" ]

ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar