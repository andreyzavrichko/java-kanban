package ru.yandex.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.http.BaseHttpHandler;
import ru.yandex.http.HttpTaskServer;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Task;

import java.io.IOException;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = HttpTaskServer.getGson();
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
            e.printStackTrace();
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
        try {
            Task task = gson.fromJson(body, Task.class);
            System.out.println("Deserialized task: " + task);
            if (task == null) {
                sendNotFound(exchange, "Задача не может быть null");
                return;
            }
            if (task.getId() == null || manager.getTaskById(task.getId()).isEmpty()) {
                manager.addTask(task); // Добавляем задачу
                System.out.println("Task added with id: " + task.getId()); // Лог успешного добавления
            } else {
                manager.updateTask(task);
            }
            sendCreated(exchange);
        } catch (JsonParseException e) {
            e.printStackTrace();
            sendServerError(exchange, "Ошибка парсинга JSON: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            sendHasOverlaps(exchange, "Задача пересекается по времени: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
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