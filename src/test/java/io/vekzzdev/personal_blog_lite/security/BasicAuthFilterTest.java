package io.vekzzdev.personal_blog_lite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("BasicAuthFilter")
@ExtendWith(MockitoExtension.class)
class BasicAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private BasicAuthFilter filter;

    @BeforeEach
    void setUp() throws ServletException {
        filter = new BasicAuthFilter();
        // For testing purposes, we'll initialize the filter with test values
        // This bypasses the environment variable requirement that's needed in production
        try {
            filter.init(null);
        } catch (IllegalStateException e) {
            // If environment variables are not set, we'll use reflection to set the fields
            // This approach allows us to test the filter logic without needing to set
            // real environment variables in the test environment
            try {
                var usernameField = BasicAuthFilter.class.getDeclaredField("expectedUsername");
                var passwordField = BasicAuthFilter.class.getDeclaredField("expectedPassword");
                
                usernameField.setAccessible(true);
                passwordField.setAccessible(true);
                
                usernameField.set(filter, "admin");
                passwordField.set(filter, "admin123_2024");
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set test credentials via reflection", ex);
            }
        }
    }

    // --- doFilter ---

    @Nested
    @DisplayName("doFilter")
    class DoFilterTests {

        @Test
        @DisplayName("should allow access to non-admin routes")
        void shouldAllowAccess_whenNotAdminRoute() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/public/posts");
            when(request.getContextPath()).thenReturn("");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        @DisplayName("should allow access to VAADIN routes")
        void shouldAllowAccess_whenVaadinRoute() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/VAADIN/static/something");
            when(request.getContextPath()).thenReturn("");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        @DisplayName("should send 401 when no Authorization header")
        void shouldSend401_whenNoAuthorizationHeader() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        @Test
        @DisplayName("should send 401 when Authorization header is not Basic")
        void shouldSend401_whenNotBasicAuth() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn("Bearer token");

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        @Test
        @DisplayName("should send 401 when credentials are invalid")
        void shouldSend401_whenInvalidCredentials() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn("Basic " + encodeBase64("wrong:wrong"));

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        @Test
        @DisplayName("should allow access with valid credentials from environment")
        void shouldAllowAccess_whenValidCredentialsFromEnv() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn("Basic " + encodeBase64("admin:admin123_2024"));

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        @DisplayName("should allow access with valid admin credentials")
        void shouldAllowAccess_whenValidAdminCredentials() throws ServletException, IOException {
            // Note: Since we can't actually modify environment variables in Java tests,
            // this test demonstrates that the filter would work with admin credentials
            // In a real environment, these would be set externally
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn("Basic " + encodeBase64("admin:admin123_2024"));

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        @DisplayName("should send 401 when malformed Base64 credentials")
        void shouldSend401_whenMalformedBase64() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn("Basic invalid_base64!!");

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        @Test
        @DisplayName("should send 401 when credentials don't contain colon")
        void shouldSend401_whenCredentialsWithoutColon() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin/dashboard");
            when(request.getContextPath()).thenReturn("");
            when(request.getHeader("Authorization")).thenReturn("Basic " + encodeBase64("invalidcredentials"));

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        @Test
        @DisplayName("should handle admin route with context path")
        void shouldHandleAdminRouteWithContextPath() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/context/admin/dashboard");
            when(request.getContextPath()).thenReturn("/context");

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        @Test
        @DisplayName("should handle exact admin route match")
        void shouldHandleExactAdminRouteMatch() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/admin");
            when(request.getContextPath()).thenReturn("");

            filter.doFilter(request, response, chain);

            verify(chain, never()).doFilter(request, response);
            verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Personal Blog Admin\"");
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    // --- decodeCredentials ---

    @Nested
    @DisplayName("decodeCredentials")
    class DecodeCredentialsTests {

        @Test
        @DisplayName("should decode valid Base64 credentials")
        void shouldDecode_whenValidBase64() {
            String encoded = encodeBase64("user:pass");
            String[] result = filter.decodeCredentials(encoded);

            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualTo("user");
            assertThat(result[1]).isEqualTo("pass");
        }

        @Test
        @DisplayName("should return null when Base64 is invalid")
        void shouldReturnNull_whenInvalidBase64() {
            String[] result = filter.decodeCredentials("invalid_base64!!");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return null when decoded string has no colon")
        void shouldReturnNull_whenNoColon() {
            String encoded = encodeBase64("invalidcredentials");
            String[] result = filter.decodeCredentials(encoded);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return first two parts when decoded string has more than one colon")
        void shouldReturnFirstTwoParts_whenMultipleColons() {
            String encoded = encodeBase64("user:pass:extra");
            String[] result = filter.decodeCredentials(encoded);

            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualTo("user");
            assertThat(result[1]).isEqualTo("pass:extra");
        }
    }

    

    // --- Helper method ---

    private String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }
}