package com.goit.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZoneOffset;

@WebFilter("/time")
public class TimezoneValidateFilter extends HttpFilter {
    private static final Logger log = LogManager.getLogger(TimezoneValidateFilter.class);
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        String parameter = req.getParameter("timezone");
        log.info("Received timezone parameter: {}", parameter);

        if (parameter != null && !parameter.trim().isEmpty()) {
            log.error("Invalid timezone offset: {}", parameter);
            String[] parts = parameter.split("\\s+");
            String timezoneId = parts[0];
            int offset = 0;

            if (parts.length > 1) {
                offset = Integer.parseInt(parts[1]);
            }

            if (offset < -18 || offset > 18) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.setContentType("text/html");
                try (PrintWriter out = res.getWriter()) {
                    out.println("<html><head><title>Error</title></head><body>");
                    out.println("<h1>Invalid timezone offset</h1>");
                    out.println("</body></html>");
                }
                return;
            }

            ZoneId zoneId;
            if (timezoneId.startsWith("UTC") || timezoneId.startsWith("GMT")) {
                ZoneOffset zoneOffset = ZoneOffset.ofHours(offset);
                zoneId = ZoneId.ofOffset(timezoneId, zoneOffset);
            } else {
                zoneId = ZoneId.of(timezoneId);
            }

            req.setAttribute("zoneId", zoneId);
            log.info("Timezone successfully validated and set: {}", zoneId);
        }
        chain.doFilter(req, res);
    }
}
