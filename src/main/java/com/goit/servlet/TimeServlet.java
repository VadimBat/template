package com.goit.servlet;

import com.goit.cookie.CookieService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private static final Logger log = LogManager.getLogger(TimeServlet.class);
    private final TemplateEngine templateEngine;

    public TimeServlet(){
        this.templateEngine = new TemplateEngine();
    }

    @Override
    public void init() throws ServletException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/webapp/template/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        res.setContentType("text/html; charset=utf-8");
        String timezoneParam = req.getParameter("timezone");
        Optional<String> lastTimezone = CookieService.getLastTimezone(req);

        Context ctx = new Context();
        ctx.setVariable("formattedTime", parseTimezone(res, timezoneParam, lastTimezone));

        templateEngine.process("current_time", ctx, res.getWriter());
        res.getWriter().close();

    }

    private String parseTimezone(HttpServletResponse res, String timezoneParam, Optional<String> lastTimezone) {
        ZoneId zoneId;
        if (timezoneParam != null && !timezoneParam.trim().isEmpty()) {

            String[] parts = timezoneParam.split("\\s+");
            String numericPart = parts[1];

            int offsetHours = Integer.parseInt(numericPart);
            ZoneOffset offset = ZoneOffset.ofHours(offsetHours);

            zoneId = ZoneId.ofOffset("UTC", offset);
            CookieService.setLastTimezone(res, numericPart);

            log.info("Set timezone {}", timezoneParam);
        } else if (lastTimezone.isPresent()) {

            ZoneOffset offset = ZoneOffset.ofHours(Integer.parseInt(lastTimezone.get()));
            zoneId = ZoneId.ofOffset("UTC", offset);

            log.info("Using last timezone from cookie: {}", lastTimezone.get());
        } else {
            zoneId = ZoneId.of("UTC");

            log.info("Using default timezone UTC");
        }
        LocalDateTime currentTime = LocalDateTime.now(zoneId);
        return currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + zoneId;
    }
}
