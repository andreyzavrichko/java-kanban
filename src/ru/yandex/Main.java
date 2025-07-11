package ru.yandex;

import ru.yandex.manager.TaskManager;
import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import static ru.yandex.manager.Managers.getDefault;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = getDefault();
        customUserScenario(manager);
    }

    private static void customUserScenario(TaskManager manager) {

        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Эпик с подзадачами", "Описание эпика 1");
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Подзадача 1", "Описание", epic1.getId());
        Subtask sub2 = new Subtask("Подзадача 2", "Описание", epic1.getId());
        Subtask sub3 = new Subtask("Подзадача 3", "Описание", epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        manager.addSubtask(sub3);

        Epic epic2 = new Epic("Эпик без подзадач", "Описание эпика 2");
        manager.addEpic(epic2);

        manager.getTaskById(task1.getId()).ifPresent(System.out::println);
        manager.getEpicById(epic1.getId()).ifPresent(System.out::println);
        manager.getSubtaskById(sub1.getId()).ifPresent(System.out::println);

        manager.getTaskById(task2.getId()).ifPresent(System.out::println);
        manager.getSubtaskById(sub2.getId()).ifPresent(System.out::println);
        manager.getSubtaskById(sub3.getId()).ifPresent(System.out::println);

        manager.getEpicById(epic2.getId()).ifPresent(System.out::println);
        manager.getTaskById(task1.getId()).ifPresent(System.out::println);

        printHistory(manager, "История после просмотров:");

        manager.deleteTaskById(task1.getId());
        printHistory(manager, "История после удаления task1:");

        manager.deleteEpicById(epic1.getId());
        printHistory(manager, "История после удаления epic1 и его подзадач:");
    }

    private static void printHistory(TaskManager manager, String title) {
        System.out.println("\n" + title);
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
