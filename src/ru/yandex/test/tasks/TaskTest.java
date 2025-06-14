package ru.yandex.test.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.tasks.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    void tasksAreEqualIfIdsAreEqual() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Tasks with same ID should be equal");
    }
}
