package manager;

import org.junit.jupiter.api.*;
import ru.yandex.manager.FileBackedTaskManager;
import ru.yandex.manager.InMemoryTaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Status;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    File tempFile;
    FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("task_manager_test", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty(), "Tasks must be empty");
        assertTrue(loaded.getAllEpics().isEmpty(), "Epics must be empty");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Subtasks must be empty");
    }

    @Test
    void shouldSaveAndLoadSingleTask() {
        Task task = new Task("Task1", "Desc");
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = loaded.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals(task, tasks.getFirst());
    }

    @Test
    void shouldSaveAndLoadEpicAndSubtasks() {
        Epic epic = new Epic("Epic1", "Epic desc");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "S1", epic.getId());
        sub1.setStatus(Status.IN_PROGRESS);
        manager.addSubtask(sub1);

        Subtask sub2 = new Subtask("Sub2", "S2", epic.getId());
        sub2.setStatus(Status.DONE);
        manager.addSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Epic loadedEpic = loaded.getEpicById(epic.getId());
        List<Subtask> loadedSubs = loaded.getSubtasksOfEpic(epic.getId());

        assertNotNull(loadedEpic, "Epic must be restored");
        assertEquals(2, loadedSubs.size(), "Should restore both subtasks");
        assertEquals(sub1.getStatus(), loadedSubs.get(0).getStatus(), "Status must match");
    }

    @Test
    void shouldPersistAfterEachModification() throws IOException {
        Task task = new Task("T1", "Desc");
        manager.addTask(task);

        String content = java.nio.file.Files.readString(tempFile.toPath());
        assertTrue(content.contains("T1"), "File must contain task name after save");
    }

    @Test
    void fileBackedManagerShouldBehaveLikeInMemoryManager() throws IOException {
        InMemoryTaskManager memManager = new InMemoryTaskManager();
        File tempFile = File.createTempFile("test", ".csv");
        FileBackedTaskManager fileManager = new FileBackedTaskManager(tempFile);

        Task t1 = new Task("Задача", "Описание");
        t1.setId(1);

        memManager.addTask(t1);
        fileManager.addTask(new Task("Задача", "Описание")); // будет ID = 1 тоже

        assertEquals(memManager.getAllTasks(), fileManager.getAllTasks(), "Обе реализации должны вести себя одинаково");
    }


}
