FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --uid 10001 --gid app --create-home appuser

COPY --chown=appuser:app target/bankingApplication-0.0.1-SNAPSHOT.jar /app/app.jar

USER appuser

EXPOSE 8282

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
