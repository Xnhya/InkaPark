package com.example.Inkapark.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("ADMIN_ID") != null) {
            return true;
        }
        resp.sendRedirect(req.getContextPath() + "/admin/login");
        return false;
    }
}
