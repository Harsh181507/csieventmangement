package com.harsh.csieventmangement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Handles unauthenticated requests to protected endpoints.
 *
 * <p>Without this, Spring Security returns an HTML error page for 401s,
 * which the Android app (expecting JSON) cannot parse — resulting in a
 * generic Retrofit exception with no useful error message.
 *
 * <p>With this, every 401 returns a consistent JSON body:
 * <pre>
 * {
 *   "success": false,
 *   "message": "Authentication required. Please log in.",
 *   "status":  401
 * }
 * </pre>
 *
 * <p><strong>Why the compile error happened:</strong>
 * The previous version of this file was an empty class with no
 * {@code implements AuthenticationEntryPoint}. SecurityConfig tried to
 * pass it as an {@link AuthenticationEntryPoint} — Java rejected it
 * because the class didn't implement the interface.
 *
 * <p><strong>Reference:</strong>
 * Spring Security — AuthenticationEntryPoint
 * https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-authenticationentrypoint
 *
 * <p><strong>File:</strong>
 * {@code src/main/java/com/harsh/csieventmangement/security/JwtAuthenticationEntryPoint.java}
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** Jackson mapper — reused across calls (thread-safe after configuration). */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Called whenever an unauthenticated user attempts to access a secured resource.
     *
     * @param request       the incoming HTTP request
     * @param response      the HTTP response that will be sent back
     * @param authException the exception that triggered this entry point
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // Set HTTP 401 status and JSON content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build response body consistent with the project's ApiResponse format
        Map<String, Object> body = Map.of(
                "success", false,
                "message", "Authentication required. Please log in.",
                "status",  HttpStatus.UNAUTHORIZED.value()
        );

        // Write JSON directly to the response output stream
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}