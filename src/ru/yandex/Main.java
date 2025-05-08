package ru.yandex;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Купить ноутбук", "Выбрать и заказать в интернете");
        Task task2 = new Task("Убраться дома", "Пропылесосить и помыть полы");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Организовать переезд", "Переезд на новую квартиру");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Собрать коробки", "Упаковать вещи", epic1.getId());
        Subtask subtask2 = new Subtask("Заказать грузчиков", "Найти компанию", epic1.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        Epic epic2 = new Epic("Купить машину", "Найти подходящую машину и купить");
        manager.addEpic(epic2);
        Subtask subtask3 = new Subtask("Посмотреть объявления", "Найти варианты", epic2.getId());
        manager.addSubtask(subtask3);

        System.out.println("###### Задачи ######");
        manager.getAllTasks().forEach(System.out::println);
        System.out.println("###### Эпики ######");
        manager.getAllEpics().forEach(System.out::println);
        System.out.println("###### Подзадачи ######");
        manager.getAllSubtasks().forEach(System.out::println);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);
        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        System.out.println("###### После изменения статусов ######");
        System.out.println("Эпик 1 статус: " + manager.getEpicById(epic1.getId()).getStatus());
        System.out.println("Эпик 2 статус: " + manager.getEpicById(epic2.getId()).getStatus());

        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("###### После удаления ######");
        System.out.println("Оставшиеся задачи: " + manager.getAllTasks());
        System.out.println("Оставшиеся эпики: " + manager.getAllEpics());
        System.out.println("Оставшиеся подзадачи: " + manager.getAllSubtasks());
    }
}
