package ru.yandex.tests.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.manager.Managers;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

}
