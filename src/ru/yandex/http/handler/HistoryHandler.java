package ru.yandex.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.http.BaseHttpHandler;
import ru.yandex.http.HttpTaskServer;
import ru.yandex.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private static final Gson gson = HttpTaskServer.GSON;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendNotFound(exchange, "Метод не поддерживается");
                return;
            }

            sendText(exchange, gson.toJson(manager.getHistory()));
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}
