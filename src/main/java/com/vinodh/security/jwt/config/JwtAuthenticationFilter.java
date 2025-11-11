package com.vinodh.security.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinodh.security.jwt.exceptions.ErrorResponse;
import com.vinodh.security.jwt.exceptions.InvalidTokenException;
import com.vinodh.security.jwt.exceptions.JwtResponseUtil;
import com.vinodh.security.jwt.service.IJwtService;
import com.vinodh.security.jwt.service.IUserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private  IJwtService jwtService;
    @Autowired
    private  IUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader,"Bearer")) {
            // no auth header - continue filter chain
            filterChain.doFilter(request,response);
            return;
        }

        jwt = authHeader.substring(7);
        logger.debug("JWT extracted (masked) for request {}: {}", request.getRequestURI(), (jwt.length() > 10 ? jwt.substring(0,6)+"...": "masked"));

        try {
            userEmail = jwtService.extractUserName(jwt);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            logger.warn("JWT parsing failed for request {}: {}", request.getRequestURI(), e.getMessage());
            JwtResponseUtil.sendError(request, response, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
            return;
        } catch (Exception e) {
            logger.error("JWT processing error for request {}: {}", request.getRequestURI(), e.getMessage(), e);
            JwtResponseUtil.sendError(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "JWT token processing failed");
            return;
        }

        if (StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

            boolean isValid;
            try {
                isValid = jwtService.validateToken(jwt, userDetails);
            } catch (Exception ex) {
                logger.warn("JWT validation exception for user {}: {}", userEmail, ex.getMessage());
                throw new InvalidTokenException("JWT token validation failed");
            }

            if (!isValid) {
                logger.warn("Invalid/expired JWT for user {}", userEmail);
                throw new InvalidTokenException("JWT token is invalid or expired");
            }
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            securityContext.setAuthentication(token);
            SecurityContextHolder.setContext(securityContext);
            logger.debug("SecurityContext set for user {}", userEmail);
        }

        filterChain.doFilter(request,response);
    }
}
