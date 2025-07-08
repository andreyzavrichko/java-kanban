package ru.yandex.manager;

import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import java.io.File;

public class FileBackedTaskManagerDemo {

    public static void main(String[] args) {
        try {
            File file = new File("tasks.csv");

            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            Task task1 = new Task("Задача 1", "Описание задачи 1");
            Task task2 = new Task("Задача 2", "Описание задачи 2");
            manager.addTask(task1);
            manager.addTask(task2);

            Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
            manager.addEpic(epic1);

            Subtask sub1 = new Subtask("Подзадача 1", "Описание", epic1.getId());
            Subtask sub2 = new Subtask("Подзадача 2", "Описание", epic1.getId());
            manager.addSubtask(sub1);
            manager.addSubtask(sub2);

            Epic epic2 = new Epic("Эпик без подзадач", "Описание эпика 2");
            manager.addEpic(epic2);

            manager.getTaskById(task1.getId()).ifPresent(System.out::println);
            manager.getEpicById(epic1.getId()).ifPresent(System.out::println);
            manager.getSubtaskById(sub2.getId()).ifPresent(System.out::println);

            FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

            System.out.println("\n Задачи:");
            for (Task task : loaded.getAllTasks()) {
                System.out.println(task);
            }

            System.out.println("\n Эпики:");
            for (Epic epic : loaded.getAllEpics()) {
                System.out.println(epic);
            }

            System.out.println("\n Подзадачи:");
            for (Subtask sub : loaded.getAllSubtasks()) {
                System.out.println(sub);
            }

            System.out.println("\n История:");
            for (Task historyTask : loaded.getHistory()) {
                System.out.println(historyTask);
            }

        } catch (Exception e) {
            System.err.println("Ошибка в сценарии: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
