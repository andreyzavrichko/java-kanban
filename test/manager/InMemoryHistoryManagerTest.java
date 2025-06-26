package manager;

import ru.yandex.manager.InMemoryHistoryManager;

class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager> {
    @Override
    protected InMemoryHistoryManager createHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
