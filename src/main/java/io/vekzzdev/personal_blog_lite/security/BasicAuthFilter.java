package io.vekzzdev.personal_blog_lite.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

/**
 * HTTP Basic Auth filter que protege las rutas /admin/*.
 * <p>
 * Lee las credenciales desde variables de entorno requeridas:
 * - ADMIN_USERNAME
 * - ADMIN_PASSWORD
 * <p>
 * vaadin-boot descubre automáticamente este filter vía @WebFilter.
 */
@WebFilter(urlPatterns = {"/admin/*", "/VAADIN/*"})
public class BasicAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthFilter.class);
    private static final String REALM = "Personal Blog Admin";

    private String expectedUsername;
    private String expectedPassword;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        expectedUsername = BasicAuthFilter.getRequiredEnv("ADMIN_USERNAME");
        expectedPassword = BasicAuthFilter.getRequiredEnv("ADMIN_PASSWORD");
        log.info("BasicAuthFilter initialized for /admin/*");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Solo proteger rutas /admin/* (no /VAADIN/dynamic/* que es recursos estáticos)
        boolean isAdminRoute = path != null
                && (path.startsWith(httpRequest.getContextPath() + "/admin")
                    || path.equals(httpRequest.getContextPath() + "/admin"));

        if (!isAdminRoute) {
            chain.doFilter(request, response);
            return;
        }

        // Verificar header Authorization
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String[] credentials = decodeCredentials(authHeader.substring(6));

            if (credentials != null
                    && expectedUsername.equals(credentials[0])
                    && expectedPassword.equals(credentials[1])) {
                log.debug("Authenticated admin request: {}", path);
                chain.doFilter(request, response);
                return;
            }

            log.warn("Invalid credentials for admin path: {}", path);
            
            // Siempre enviar 401 para credenciales inválidas - esto permite al navegador
            // limpiar su caché de autenticación y volver a pedir credenciales
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // No hay header Authorization — enviar 401 con challenge para mostrar diálogo de login
        log.debug("Unauthenticated request to admin path: {}", path);
        httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    /**
     * Verifica si la solicitud es una solicitud interna de Vaadin que no debe ser redirigida.
     */
    private boolean isVaadinInternalRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // No redirigir solicitudes a recursos VAADIN
        if (path != null && path.contains("/VAADIN/")) {
            return true;
        }
        
        // Verificar si es una solicitud UIDL (comunicación Vaadin client-server)
        String uidlHeader = request.getHeader("X-Vaadin-UIDL");
        if ("true".equals(uidlHeader)) {
            return true;
        }
        
        // Verificar si es una solicitud de archivo estático
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && (acceptHeader.contains("application/javascript") 
                || acceptHeader.contains("text/css") 
                || acceptHeader.contains("image/") 
                || acceptHeader.contains("font/"))) {
            return true;
        }
        
        return false;
    }
    
    public String[] decodeCredentials(String base64Encoded) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64Encoded));
            String[] parts = decoded.split(":", 2);
            if (parts.length == 2) {
                return parts;
            }
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode Basic Auth header", e);
        }
        return null;
    }

    public static String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable '" + key + "' is not set or is empty");
        }
        return value;
    }

    @Override
    public void destroy() {
        log.info("BasicAuthFilter destroyed");
    }
}
