package manager;

import org.junit.jupiter.api.Test;
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

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("test", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(tempFile);
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadSingleTask() {
        Task task = new Task("Task1", "Desc");
        manager.addTask(task);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(List.of(task), loaded.getAllTasks());
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
        assertEquals(2, loaded.getSubtasksOfEpic(epic.getId()).size());
    }

    @Test
    void shouldPersistAfterEachModification() throws IOException {
        Task task = new Task("T1", "Desc");
        manager.addTask(task);
        String content = java.nio.file.Files.readString(tempFile.toPath());
        assertTrue(content.contains("T1"));
    }

    @Test
    void fileBackedManagerShouldBehaveLikeInMemoryManager() throws IOException {
        var inMemory = new InMemoryTaskManager();
        var fileBased = new FileBackedTaskManager(File.createTempFile("copy", ".csv"));
        Task t1 = new Task("Задача", "Описание");
        inMemory.addTask(t1);
        fileBased.addTask(new Task("Задача", "Описание"));
        assertEquals(inMemory.getAllTasks(), fileBased.getAllTasks());
    }

    @Test
    void shouldThrowWhenFileIsCorrupted() throws IOException {
        File brokenFile = File.createTempFile("broken", ".csv");
        java.nio.file.Files.writeString(brokenFile.toPath(), "id,type,name,status,description,epic,duration,startTime\nbroken,data,with,error");

        assertThrows(RuntimeException.class, () -> FileBackedTaskManager.loadFromFile(brokenFile));
    }


    @Test
    void getByIdShouldReturnEmptyIfTaskNotFound() {
        assertTrue(manager.getTaskById(999).isEmpty());
        assertTrue(manager.getEpicById(999).isEmpty());
        assertTrue(manager.getSubtaskById(999).isEmpty());
    }


}
