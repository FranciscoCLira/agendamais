package com.agendademais.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionDebugInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);

        System.out.println("*** SESSION DEBUG ***");
        System.out.println("URL: " + request.getRequestURL());
        System.out.println("Session existente: " + (session != null));

        if (session != null) {
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session criada em: " + new java.util.Date(session.getCreationTime()));
            System.out.println("Ultimo acesso: " + new java.util.Date(session.getLastAccessedTime()));
            System.out.println("Max inactive: " + session.getMaxInactiveInterval());
        }

        // Debugar cookies
        Cookie[] cookies = request.getCookies();
        System.out.println("Cookies recebidos:");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("  " + cookie.getName() + " = " + cookie.getValue());
            }
        } else {
            System.out.println("  Nenhum cookie recebido");
        }

        System.out.println("*******************");

        return true;
    }
}
