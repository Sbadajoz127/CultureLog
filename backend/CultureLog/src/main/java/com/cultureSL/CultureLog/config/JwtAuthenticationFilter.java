package com.cultureSL.CultureLog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT que intercepta cada petición HTTP.
 * <p>
 * Extiende {@link OncePerRequestFilter} para garantizar una sola ejecución por petición.
 * Extrae el token JWT de la cabecera {@code Authorization} (formato Bearer), lo valida
 * y, si es correcto, establece la autenticación en el {@link SecurityContextHolder}
 * con el ID del usuario como principal.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Procesa cada petición HTTP para verificar la presencia y validez del token JWT.
     * <p>
     * Si el token es válido, crea un {@link UsernamePasswordAuthenticationToken} con el
     * ID del usuario como principal y lo establece en el contexto de seguridad.
     * Si no hay token o es inválido, la petición continúa sin autenticación.
     * </p>
     *
     * @param request     petición HTTP entrante
     * @param response    respuesta HTTP
     * @param filterChain cadena de filtros para continuar el procesamiento
     * @throws ServletException si ocurre un error en el filtro
     * @throws IOException      si ocurre un error de E/S
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                Long userId = jwtService.extractUserId(token);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
