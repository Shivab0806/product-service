package com.example.productservice.JWTconfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            if (jwtUtil.validateToken(token)) {

                String username = jwtUtil.extractUsername(token);
                Set<String> roles = jwtUtil.extractRoles(token);
                java.util.Set<String> perms = jwtUtil.extractAuthorities(token);
                java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
                roles.stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).forEach(authorities::add);
                perms.stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).forEach(authorities::add);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println(" getAuthentication:{}"+  SecurityContextHolder.getContext().getAuthentication());

            }

        } catch (Exception ex) {
            // Invalid token -> do not authenticate the request.
            // Spring Security will return 401 for protected endpoints.
        }

        filterChain.doFilter(request, response);
    }
}

