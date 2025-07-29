package http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.http.HttpTaskServer;
import ru.yandex.manager.InMemoryTaskManager;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerSubtasksTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private HttpTaskServer taskServer;
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        String subtaskJson = HttpTaskServer.GSON.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус ответа");

        var subtasks = manager.getAllSubtasks();
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals("Test Subtask", subtasks.getFirst().getName());
    }


    @Test
    public void testGetNonExistentSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}
