package ru.post.automation.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class AppLogger {
    private static boolean configured = false;

    private AppLogger() {
    }

    public static synchronized Logger get(Class<?> type) {
        if (!configured) {
            configure();
        }
        return Logger.getLogger(type.getSimpleName());
    }

    private static void configure() {
        configured = true;
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) {
            h.setFormatter(new LineFormatter());
        }
        try {
            new java.io.File("logs").mkdirs();
            FileHandler file = new FileHandler("logs/app.log", true);
            file.setFormatter(new LineFormatter());
            root.addHandler(file);
        } catch (IOException e) {
            root.warning("Файл журнала недоступен: " + e.getMessage());
        }
    }

    private static class LineFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(record.getLevel()).append(" [").append(record.getLoggerName()).append("] ")
                    .append(formatMessage(record)).append(System.lineSeparator());
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(sw));
                sb.append(sw);
            }
            return sb.toString();
        }
    }
}
