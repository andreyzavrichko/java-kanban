package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Status;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        Optional<Task> found = manager.getTaskById(task.getId());
        assertTrue(found.isPresent(), "Задача должна быть найдена по ID");
        assertEquals(task, found.get(), "Найденная задача должна совпадать с добавленной");
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

        Optional<Epic> maybeEpic = manager.getEpicById(epic.getId());
        assertTrue(maybeEpic.isPresent(), "Эпик должен существовать");
        assertTrue(maybeEpic.get().getSubtaskIds().isEmpty(), "Список подзадач должен быть пуст");
    }

    @Test
    void shouldNotAffectEpicStatusWhenChangingSubtaskWithoutUpdate() {
        Epic epic = new Epic("Epic", "E");
        manager.addEpic(epic);
        Subtask sub = new Subtask("Sub", "S", epic.getId());
        manager.addSubtask(sub);
        sub.setStatus(Status.DONE);

        Optional<Epic> maybeEpic = manager.getEpicById(epic.getId());
        assertTrue(maybeEpic.isPresent(), "Эпик должен быть найден");
        assertEquals(Status.NEW, maybeEpic.get().getStatus(), "Статус эпика должен остаться NEW");
    }

    @Test
    void epicStatusShouldBeNewIfAllSubtasksNew() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void epicStatusShouldBeDoneIfAllSubtasksDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        sub1.setStatus(Status.DONE);
        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());
        sub2.setStatus(Status.DONE);

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressIfMixedNewAndDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        sub1.setStatus(Status.NEW);
        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());
        sub2.setStatus(Status.DONE);

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressIfAllInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        sub1.setStatus(Status.IN_PROGRESS);
        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());
        sub2.setStatus(Status.IN_PROGRESS);

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldThrowWhenTasksIntersectByTime() {
        Task task1 = new Task("Task1", "Desc1");
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));

        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc2");
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        task2.setDuration(Duration.ofMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> manager.addTask(task2));
    }

    @Test
    void emptyHistoryShouldReturnEmptyList() {
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void addingTaskToHistoryShouldAppearInList() {
        Task task = new Task("Task", "Desc");
        manager.addTask(task);
        manager.getTaskById(task.getId());
        assertEquals(List.of(task), manager.getHistory());
    }


}