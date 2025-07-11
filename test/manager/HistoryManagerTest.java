package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.manager.HistoryManager;
import ru.yandex.tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class HistoryManagerTest<T extends HistoryManager> {

    protected T historyManager;
    protected Task task;

    protected abstract T createHistoryManager();

    @BeforeEach
    void setUp() {
        historyManager = createHistoryManager();
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
        Task task1 = new Task("Task1", "Desc1");
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2");
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
        assertDoesNotThrow(() -> historyManager.remove(999));
    }

    @Test
    void addingSameTaskShouldMoveItToEnd() {
        Task task = new Task("Task1", "Desc1");
        task.setId(1);
        Task task2 = new Task("Task2", "Desc2");
        task2.setId(2);

        historyManager.add(task);
        historyManager.add(task2);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task, history.getLast());
    }


}
