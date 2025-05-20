package ru.yandex.tests.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.manager.HistoryManager;
import ru.yandex.manager.InMemoryHistoryManager;
import ru.yandex.manager.InMemoryTaskManager;
import ru.yandex.manager.TaskManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.manager.Managers.getDefault;
import static ru.yandex.manager.Managers.getDefaultHistory;

class ManagersTest {

    @Test
    void getDefaultShouldReturnTaskManagerInstance() {
        TaskManager manager = getDefault();
        assertNotNull(manager, "getDefault() вернул null");
        assertTrue(manager instanceof InMemoryTaskManager, "Менеджер должен быть экземпляром InMemoryTaskManager");
    }

    @Test
    void getDefaultHistoryShouldReturnHistoryManagerInstance() {
        HistoryManager historyManager = getDefaultHistory();
        assertNotNull(historyManager, "getDefaultHistory() вернул null");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "Менеджер истории должен быть экземпляром InMemoryHistoryManager");
    }
}
