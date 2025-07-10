package ru.yandex.manager;

import ru.yandex.tasks.Epic;
import ru.yandex.tasks.Status;
import ru.yandex.tasks.Subtask;
import ru.yandex.tasks.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected Integer idCounter = 1;

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
        prioritizedTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
        prioritizedTasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            updateEpicFields(epic);
        });
        subtasks.clear();
        prioritizedTasks.clear();
    }


    @Override
    public Optional<Task> getTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Epic> getEpicById(Integer id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public Optional<Subtask> getSubtaskById(Integer id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public void addTask(Task task) {
        if (hasIntersections(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }


    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpic(epic);
    }


    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task not found");
        }

        Task oldTask = tasks.get(task.getId());
        prioritizedTasks.remove(oldTask);

        if (hasIntersections(task)) {
            prioritizedTasks.add(oldTask);
            throw new IllegalArgumentException("Обновлённая задача пересекается по времени с другой задачей");
        }

        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }


    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Epic not found");
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic.getId());
        epic.calculateTimeAndDuration(subtasksOfEpic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Subtask not found");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());
        prioritizedTasks.remove(oldSubtask);

        if (hasIntersections(subtask)) {
            prioritizedTasks.add(oldSubtask);
            throw new IllegalArgumentException("Обновлённая подзадача пересекается по времени с другой задачей");
        }

        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        updateEpic(epic);
    }


    @Override
    public List<Subtask> getSubtasksOfEpic(Integer epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void deleteTaskById(Integer id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            epic.getSubtaskIds().remove(id);
            updateEpic(epic);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private Integer generateId() {
        return idCounter++;
    }

    private void updateEpicStatus(Epic epic) {
        List<Status> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStatus)
                .toList();

        if (statuses.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else if (statuses.stream().allMatch(status -> status == Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else if (statuses.stream().allMatch(status -> status == Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }


    private boolean isTimeIntersecting(Task a, Task b) {
        if (a.getStartTime() == null || b.getStartTime() == null) return false;

        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();

        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        return !aEnd.isBefore(bStart) && !aStart.isAfter(bEnd);
    }

    private boolean hasIntersections(Task newTask) {
        if (newTask.getStartTime() == null) return false;

        return prioritizedTasks.stream()
                .filter(task -> !Objects.equals(task.getId(), newTask.getId()))
                .anyMatch(existing -> isTimeIntersecting(newTask, existing));
    }

    private void updateEpicFields(Epic epic) {
        updateEpicStatus(epic);
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic.getId());
        epic.calculateTimeAndDuration(subtasksOfEpic);
    }

}
