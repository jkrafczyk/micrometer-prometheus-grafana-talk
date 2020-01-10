FROM gradle:6.0.1-jdk11
ADD . /src

WORKDIR /src

RUN gradle -Dorg.gradle.daemon=false build
RUN mkdir /app && mv /src/build/libs/src-0.0.1-SNAPSHOT.jar /app/app.jar && rm -rf /src

ENTRYPOINT [ "java", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar" ]