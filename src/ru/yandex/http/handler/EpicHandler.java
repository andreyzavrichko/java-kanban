package ru.yandex.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.http.BaseHttpHandler;
import ru.yandex.http.HttpTaskServer;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;

import java.io.IOException;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private static final Gson gson = HttpTaskServer.GSON;

    public EpicHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendNotFound(exchange, "Метод не поддерживается");
            }

        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        Integer id = parsePathId(path);
        if (id != null) {
            Optional<Epic> epic = manager.getEpicById(id);
            if (epic.isPresent()) {
                sendText(exchange, gson.toJson(epic.get()));
            } else {
                sendNotFound(exchange, "Эпик с id=" + id + " не найден");
            }
        } else {
            sendText(exchange, gson.toJson(manager.getAllEpics()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getId() == null || manager.getEpicById(epic.getId()).isEmpty()) {
            manager.addEpic(epic);
        } else {
            manager.updateEpic(epic);
        }

        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Integer id = parsePathId(path);
        if (id != null) {
            Optional<Epic> epic = manager.getEpicById(id);
            if (epic.isPresent()) {
                manager.deleteEpicById(id);
                sendText(exchange, "Удалён эпик с id=" + id);
            } else {
                sendNotFound(exchange, "Эпик не найден");
            }
        } else {
            sendNotFound(exchange, "Некорректный путь удаления");
        }
    }
}
