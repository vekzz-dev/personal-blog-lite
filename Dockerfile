# =============================================================
# Stage 1: Build — compila en modo producción (Vaadin optimizado)
# =============================================================
FROM eclipse-temurin:21-jdk-jammy AS build

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar primero los archivos de build para cache de dependencias
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/
COPY gradlew ./
RUN chmod +x gradlew

# Descargar dependencias (cache layer — solo cambia si cambia build.gradle)
RUN ./gradlew dependencies --no-daemon || true

# Copiar el resto del código fuente (incluye jOOQ generado en src/main/java)
COPY src/ src/

# Build en modo producción (Vaadin compila el frontend bundle)
ARG BUILD_VERSION=1.0.0
RUN ./gradlew installDist -Pvaadin.productionMode --no-daemon

# =============================================================
# Stage 2: Runtime — solo JRE + la distribución compilada
# =============================================================
FROM eclipse-temurin:21-jre-jammy

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Crear directorio de logs
RUN mkdir -p /app/logs

# Crear usuario no-root para seguridad
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin appuser

# Copiar la distribución generada por installDist
COPY --from=build /app/build/install/personal-blog-lite/ ./

# El directorio de la app pertenece al usuario no-root
RUN chown -R appuser:appgroup /app

# Crear directorio para logs externo (montado como volumen)
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser

# Puerto por defecto de vaadin-boot (Jetty)
EXPOSE 8080

# Variables de entorno (deben ser proporcionadas via compose o docker run)
ENV DB_URL=jdbc:mariadb://db:3306/blog_lite
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check básico — el app responde en /
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# installDist genera bin/personal-blog-lite (start script)
ENTRYPOINT ["bin/personal-blog-lite"]
