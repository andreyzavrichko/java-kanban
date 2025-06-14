package ru.yandex.test.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.manager.HistoryManager;
import ru.yandex.manager.Managers;
import ru.yandex.tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    Task task;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task = new Task("Test", "Test desc");
        task.setId(1);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test
    void historyShouldNotExceedLimit() {
        for (int i = 0; i < 15; i++) {
            Task t = new Task("Task" + i, "Desc");
            t.setId(i);
            historyManager.add(t);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "History should contain only 10 most recent tasks");
    }

    @Test
    void addTaskShouldAddToEndOfHistory() {
        Task first = new Task("First", "Desc1");
        first.setId(1);
        Task second = new Task("Second", "Desc2");
        second.setId(2);

        historyManager.add(first);
        historyManager.add(second);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(first, second), history);
    }

    @Test
    void removeShouldDeleteTaskFromHistory() {
        Task task1 = new Task("T1", "D");
        task1.setId(1);
        Task task2 = new Task("T2", "D");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());
    }

    @Test
    void removeNonexistentIdShouldNotThrow() {
        historyManager.remove(999);
    }

    @Test
    void historyShouldNotExceedTenTasks() {
        for (int i = 1; i <= 15; i++) {
            Task t = new Task("T" + i, "D");
            t.setId(i);
            historyManager.add(t);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(15, history.size());
        assertEquals(1, history.getFirst().getId());
    }

    @Test
    void addingSameTaskShouldMoveItToEnd() {
        Task task = new Task("Task", "Desc");
        task.setId(1);

        Task dummy = new Task("Dummy", "D");
        dummy.setId(2);

        historyManager.add(task);
        historyManager.add(dummy);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task, history.getLast());
    }


}
