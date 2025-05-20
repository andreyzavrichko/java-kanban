package ru.yandex.tests.manager;

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
}
