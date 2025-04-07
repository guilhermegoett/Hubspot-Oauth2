package com.hubspot.demo.utils;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class SessionManager {
    private HttpSession session;

    public SessionManager(HttpSession session) {
        this.session = session;
    }

    public void setState(String state) {
        session.setAttribute("oauthState", state);
    }

    public String getState() {
        return (String) session.getAttribute("oauthState");
    }

    public void clearState() {
        session.removeAttribute("oauthState");
    }

    public void setAccessToken(String accessToken) {
        session.setAttribute("accessToken", accessToken);
    }

    public String getAccessToken() {
        return (String) session.getAttribute("accessToken");
    }

    public void clearAccessToken() {
        session.removeAttribute("accessToken");
    }

    public boolean hasAccessToken() {
        Object token = session.getAttribute("accessToken");
        return token != null && !token.toString().isBlank();
    }
}
