package ru.yandex.test.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.manager.Managers;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Status;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldAddAndFindTasksById() {
        Task task = new Task("Задача 1", "Описание");
        manager.addTask(task);

        Task found = manager.getTaskById(task.getId());
        assertEquals(task, found, "Task should be found by ID");
    }

    @Test
    void subtaskCannotBeOwnEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Подзадача", "Описание", epic.getId());
        manager.addSubtask(sub);

        assertNotEquals(sub.getId(), sub.getEpicId(), "Subtask cannot reference itself as epic");
    }

    @Test
    void deletingSubtaskShouldRemoveIdFromEpic() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.addSubtask(subtask);

        manager.deleteSubtaskById(subtask.getId());

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Epic should not reference deleted subtask");
    }

    @Test
    void shouldNotAffectEpicStatusWhenChangingSubtaskWithoutUpdate() {
        Epic epic = new Epic("Epic", "E");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Sub", "S", epic.getId());
        manager.addSubtask(sub);

        sub.setStatus(Status.DONE);

        Epic result = manager.getEpicById(epic.getId());
        assertEquals(Status.NEW, result.getStatus(), "Epic status shouldn't change without proper update");
    }


}
