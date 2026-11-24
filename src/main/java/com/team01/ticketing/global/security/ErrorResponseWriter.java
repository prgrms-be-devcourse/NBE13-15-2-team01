package com.team01.ticketing.global.security;

import com.team01.ticketing.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class ErrorResponseWriter {

    private ErrorResponseWriter() {
    }

    static void write(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"code\":\"" + code.name() + "\",\"message\":\"" + code.getMessage()
                + "\",\"fieldErrors\":{}}";
        response.getWriter().write(body);
    }
}