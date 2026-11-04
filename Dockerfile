FROM eclipse-temurin:27-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon > /dev/null

COPY src src
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar --no-daemon \
    && cp "$(ls build/libs/*.jar | grep -v -- '-plain.jar')" app.jar

FROM eclipse-temurin:27-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
