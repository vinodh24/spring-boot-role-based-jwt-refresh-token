package com.vinodh.security.jwt.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class JwtResponseUtil {

    public static void sendError(HttpServletRequest request,
                                 HttpServletResponse response,
                                 HttpStatus status,
                                 String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                status,
                message,
                request.getRequestURI()
        );

        ObjectMapper objectMapper = new ObjectMapper();

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
