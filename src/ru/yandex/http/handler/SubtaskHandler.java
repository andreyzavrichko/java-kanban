package ru.yandex.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.http.BaseHttpHandler;
import ru.yandex.http.HttpTaskServer;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Subtask;

import java.io.IOException;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private static final Gson gson = HttpTaskServer.GSON;

    public SubtaskHandler(TaskManager manager) {
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
            Optional<Subtask> subtask = manager.getSubtaskById(id);
            if (subtask.isPresent()) {
                sendText(exchange, gson.toJson(subtask.get()));
            } else {
                sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
            }
        } else {
            sendText(exchange, gson.toJson(manager.getAllSubtasks()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        try {
            if (subtask.getId() == null || manager.getSubtaskById(subtask.getId()).isEmpty()) {
                manager.addSubtask(subtask);
            } else {
                manager.updateSubtask(subtask);
            }
            sendCreated(exchange);
        } catch (IllegalArgumentException e) {
            sendHasOverlaps(exchange, "Подзадача пересекается по времени");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Integer id = parsePathId(path);
        if (id != null) {
            Optional<Subtask> subtask = manager.getSubtaskById(id);
            if (subtask.isPresent()) {
                manager.deleteSubtaskById(id);
                sendText(exchange, "Удалена подзадача с id=" + id);
            } else {
                sendNotFound(exchange, "Подзадача не найдена");
            }
        } else {
            sendNotFound(exchange, "Некорректный путь удаления");
        }
    }
}
