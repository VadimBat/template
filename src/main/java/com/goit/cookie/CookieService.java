package com.goit.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;

public class CookieService {
    private static final String COOKIE_NAME = "lastTimezone";
    private static final Logger log = LogManager.getLogger(CookieService.class);

    private CookieService() {
    }

    public static Optional<String> getLastTimezone(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies)
                    .filter(c -> c.getName().equals(COOKIE_NAME))
                    .findFirst();
            if (cookie.isPresent()) {
                String timezone = cookie.get().getValue();
                log.info("Received timezone from cookie: {}", timezone);
                return Optional.of(timezone);
            }
        }
        log.info("No timezone found in cookies");
        return Optional.empty();
    }
    public static void setLastTimezone(HttpServletResponse resp, String timezone) {
        Cookie cookie = new Cookie(COOKIE_NAME, timezone);
        int oneDayInSeconds = 24 * 60 * 60;
        cookie.setMaxAge(oneDayInSeconds);
        resp.addCookie(cookie);
        log.info("Set timezone in cookie: {}", timezone);
    }
}
