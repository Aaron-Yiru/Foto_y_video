# --- ETAPA 1: Construcción (Build) ---
FROM gradle:jdk21-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
# Al ejecutarse esto, gracias al cambio en build.gradle, se crea "app.jar"
RUN gradle bootJar -x test --no-daemon

# --- ETAPA 2: Ejecución (Run) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# AQUI ESTA LA CLAVE: Copiamos el archivo exacto, sin usar asteriscos (*)
COPY --from=build /app/build/libs/app.jar app.jar

ENV SERVER_PORT=8082
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]