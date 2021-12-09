package com.srijan.demo.facerecognition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Configuration
public class CompreFaceApiKeyInterceptor implements HandlerInterceptor, ClientHttpRequestInterceptor {

    private static String xApiKey = null;
    private final List<String> allowedMethods = List.of("GET", "PUT", "POST", "PATCH", "DELETE");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow only a specific set of methods
        if (!allowedMethods.contains(request.getMethod().toUpperCase(Locale.ROOT))) {
            return true;
        }

        xApiKey = request.getHeader("x-api-key");
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set("x-api-key", xApiKey);
        return execution.execute(request, body);
    }
}
