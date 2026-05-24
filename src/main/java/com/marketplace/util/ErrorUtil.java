package com.marketplace.util;

public class ErrorUtil {

    public static String friendlyMessage(Throwable ex) {
        if (ex == null) return "Неизвестная ошибка.";
        String msg = ex.getMessage();
        if (msg == null) msg = ex.getClass().getSimpleName();

        if (containsAny(msg,
                "UnknownHostException", "Unable to resolve host",
                "SERVFAIL", "NXDOMAIN", "nodename nor servname")) {
            return "Нет подключения к интернету или сервер недоступен.\n" +
                   "Проверьте соединение и попробуйте снова.";
        }
        if (containsAny(msg, "timeout", "SocketTimeoutException", "connect timed out",
                "read timed out", "deadline exceeded")) {
            return "Сервер не отвечает. Возможно, проблемы с соединением.\n" +
                   "Попробуйте через несколько секунд.";
        }
        if (containsAny(msg, "Connection refused", "ConnectException",
                "Failed to connect", "ECONNREFUSED")) {
            return "Не удалось подключиться к серверу.\n" +
                   "Проверьте интернет-соединение.";
        }
        if (containsAny(msg, "SSLException", "SSLHandshakeException",
                "certificate", "CERTIFICATE_VERIFY_FAILED")) {
            return "Ошибка безопасного соединения (SSL).\n" +
                   "Проверьте дату и время на вашем компьютере.";
        }

        if (containsAny(msg, "HTTP 401", "Invalid login credentials",
                "invalid_grant", "Email not confirmed")) {
            return "Неверный email или пароль.\n" +
                   "Проверьте данные и попробуйте снова.";
        }
        if (containsAny(msg, "HTTP 403", "Forbidden", "not authorized",
                "permission denied", "row-level security")) {
            return "Недостаточно прав для выполнения этого действия.";
        }
        if (containsAny(msg, "HTTP 404", "Not Found")) {
            return "Запрашиваемый ресурс не найден.";
        }
        if (containsAny(msg, "HTTP 409", "duplicate", "already exists",
                "unique constraint", "violates unique")) {
            return "Такая запись уже существует.";
        }
        if (containsAny(msg, "HTTP 422", "Unprocessable")) {
            return "Некорректные данные. Проверьте введённую информацию.";
        }
        if (containsAny(msg, "HTTP 429", "Too Many Requests", "rate limit")) {
            return "Слишком много запросов. Подождите немного и попробуйте снова.";
        }
        if (containsAny(msg, "HTTP 5", "Internal Server Error",
                "Bad Gateway", "Service Unavailable")) {
            return "Ошибка на сервере. Попробуйте позже.";
        }

        if (containsAny(msg, "User already registered", "already been registered")) {
            return "Пользователь с таким email уже зарегистрирован.";
        }
        if (containsAny(msg, "Password should be at least")) {
            return "Пароль слишком короткий. Минимум 6 символов.";
        }
        if (containsAny(msg, "Unable to validate email address")) {
            return "Некорректный формат email.";
        }
        if (containsAny(msg, "заблокирован")) {
            return msg;
        }

        if (containsAny(msg, "Ошибка создания заказа", "Заказ не найден",
                "Товары из", "не доступны")) {
            return msg;
        }

        if (msg.matches("HTTP \\d{3}:.*")) {
            int colonIdx = msg.indexOf(':');
            if (colonIdx > 0 && colonIdx < msg.length() - 1) {
                String detail = msg.substring(colonIdx + 1).trim();
                if (detail.startsWith("{") || detail.startsWith("[")) {
                    int code = 0;
                    try { code = Integer.parseInt(msg.substring(5, 8)); } catch (Exception ignored) {}
                    return "Ошибка сервера (код " + code + "). Попробуйте позже.";
                }
            }
        }

        if (msg.matches("[a-zA-Z0-9._-]+\\.[a-zA-Z]{2,}")) {
            return "Нет подключения к интернету или сервер недоступен.\n" +
                   "Проверьте соединение и попробуйте снова.";
        }

        return msg;
    }

    private static boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }
}
