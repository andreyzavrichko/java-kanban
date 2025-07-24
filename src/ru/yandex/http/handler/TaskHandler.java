package ru.yandex.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.http.BaseHttpHandler;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Task;

import java.io.IOException;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET")) {
                handleGet(exchange, path);
            } else if (method.equals("POST")) {
                handlePost(exchange);
            } else if (method.equals("DELETE")) {
                handleDelete(exchange, path);
            } else {
                sendNotFound(exchange, "Метод не поддерживается");
            }

        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        Integer id = parsePathId(path);
        if (id != null) {
            Optional<Task> task = manager.getTaskById(id);
            if (task.isPresent()) {
                sendText(exchange, gson.toJson(task.get()));
            } else {
                sendNotFound(exchange, "Задача с id=" + id + " не найдена");
            }
        } else {
            sendText(exchange, gson.toJson(manager.getAllTasks()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Task task = gson.fromJson(body, Task.class);

        try {
            if (task.getId() == null || manager.getTaskById(task.getId()).isEmpty()) {
                manager.addTask(task);
            } else {
                manager.updateTask(task);
            }
            sendCreated(exchange);
        } catch (IllegalArgumentException e) {
            sendHasOverlaps(exchange, "Задача пересекается по времени");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Integer id = parsePathId(path);
        if (id != null) {
            Optional<Task> task = manager.getTaskById(id);
            if (task.isPresent()) {
                manager.deleteTaskById(id);
                sendText(exchange, "Удалена задача с id=" + id);
            } else {
                sendNotFound(exchange, "Задача не найдена");
            }
        } else {
            sendNotFound(exchange, "Некорректный путь удаления");
        }
    }
}
