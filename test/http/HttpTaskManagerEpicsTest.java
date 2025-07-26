package http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.http.HttpTaskServer;
import ru.yandex.manager.InMemoryTaskManager;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerEpicsTest {

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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        String epicJson = HttpTaskServer.getGson().toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        var epics = manager.getAllEpics();
        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals("Test Epic", epics.getFirst().getName());
    }

    @Test
    public void testGetNonExistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}
