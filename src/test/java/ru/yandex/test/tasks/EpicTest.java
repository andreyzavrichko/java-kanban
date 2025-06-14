package test.java.ru.yandex.test.tasks;


import org.junit.jupiter.api.Test;
import ru.yandex.tasks.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    @Test
    void epicsAreEqualIfIdsAreEqual() {
        Epic epic1 = new Epic("Эпик 1", "Описание");
        Epic epic2 = new Epic("Эпик 2", "Описание");
        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Epics with same ID should be equal");
    }
}