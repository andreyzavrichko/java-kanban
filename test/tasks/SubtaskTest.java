package tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.tasks.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {
    @Test
    void subtasksAreEqualIfIdsAreEqual() {
        Subtask sub1 = new Subtask("Подзадача 1", "Описание", 1);
        Subtask sub2 = new Subtask("Подзадача 2", "Описание", 1);
        sub1.setId(2);
        sub2.setId(2);

        assertEquals(sub1, sub2, "Subtasks with same ID should be equal");
    }
}
