package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Status;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldAddAndFindTasksById() {
        Task task = new Task("Задача 1", "Описание");
        manager.addTask(task);
        Task found = manager.getTaskById(task.getId());
        assertEquals(task, found);
    }

    @Test
    void subtaskCannotBeOwnEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);
        Subtask sub = new Subtask("Подзадача", "Описание", epic.getId());
        manager.addSubtask(sub);
        assertNotEquals(sub.getId(), sub.getEpicId());
    }

    @Test
    void deletingSubtaskShouldRemoveIdFromEpic() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.addSubtask(subtask);
        manager.deleteSubtaskById(subtask.getId());
        assertTrue(manager.getEpicById(epic.getId()).getSubtaskIds().isEmpty());
    }

    @Test
    void shouldNotAffectEpicStatusWhenChangingSubtaskWithoutUpdate() {
        Epic epic = new Epic("Epic", "E");
        manager.addEpic(epic);
        Subtask sub = new Subtask("Sub", "S", epic.getId());
        manager.addSubtask(sub);
        sub.setStatus(Status.DONE);
        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }
}