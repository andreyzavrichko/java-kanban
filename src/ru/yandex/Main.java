package ru.yandex;

import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import static ru.yandex.manager.Managers.getDefault;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = getDefault();
        demoScenario(manager);
    }

    private static void demoScenario(TaskManager manager) {
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        manager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        manager.addTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic1.getId());
        manager.addSubtask(subtask2);

        manager.getTaskById(task1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getEpicById(epic1.getId());
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getEpicById(epic1.getId());

        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);
            for (Task subtask : manager.getAllEpics()) {
                System.out.println("--> " + subtask);
            }
        }

        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
