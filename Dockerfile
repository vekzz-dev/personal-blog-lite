# =============================================================
# Stage 1: Build — compila en modo producción (Vaadin optimizado)
# =============================================================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copiar primero los archivos de build para cache de dependencias
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/
COPY gradlew ./
RUN chmod +x gradlew

# Descargar dependencias (cache layer — solo cambia si cambia build.gradle)
RUN ./gradlew dependencies --no-daemon || true

# Copiar el resto del código fuente
COPY src/ src/

# Build en modo producción (Vaadin compila el frontend bundle)
RUN ./gradlew installDist -Pvaadin.productionMode --no-daemon

# =============================================================
# Stage 2: Runtime — solo JRE + la distribución compilada
# =============================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Crear usuario no-root para seguridad
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin appuser

# Copiar la distribución generada por installDist
COPY --from=build /app/build/install/personal-blog-lite/ ./

# El directorio de la app pertenece al usuario no-root
RUN chown -R appuser:appgroup /app

USER appuser

# Puerto por defecto de vaadin-boot (Jetty)
EXPOSE 8080

# Variables de entorno con defaults (se sobreescriben desde compose)
ENV DB_URL=jdbc:mariadb://db:3306/blog_lite \
    DB_USER=blog_user \
    DB_PASSWORD=blog_pass

# Health check básico — el app responde en /
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# installDist genera bin/personal-blog-lite (start script)
ENTRYPOINT ["bin/personal-blog-lite"]
