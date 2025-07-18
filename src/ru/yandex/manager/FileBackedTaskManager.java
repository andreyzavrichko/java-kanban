package ru.yandex.manager;

import ru.yandex.tasks.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic,duration,startTime\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) continue;

                Task task = fromString(lines[i]);

                if (task instanceof Epic epic) {
                    manager.epics.put(epic.getId(), epic);
                } else if (task instanceof Subtask subtask) {
                    manager.subtasks.put(subtask.getId(), subtask);
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtask(subtask.getId());
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }

                if (task.getStartTime() != null) {
                    manager.getPrioritizedTasks().add(task);
                }

                manager.idCounter = Math.max(manager.idCounter, task.getId() + 1);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке файла", e);
        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка при разборе строки из файла", e);
        }

        return manager;
    }


    private static Task fromString(String value) {
        try {
            String[] parts = value.split(",");
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4];
            Duration duration = Duration.ofMinutes(Long.parseLong(parts[6]));
            LocalDateTime startTime = "null".equals(parts[7]) ? null : LocalDateTime.parse(parts[7]);

            return switch (type) {
                case TASK -> {
                    Task task = new Task(name, description);
                    task.setId(id);
                    task.setStatus(status);
                    task.setDuration(duration);
                    task.setStartTime(startTime);
                    yield task;
                }
                case EPIC -> {
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setStatus(status);
                    yield epic;
                }
                case SUBTASK -> {
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description, epicId);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    subtask.setDuration(duration);
                    subtask.setStartTime(startTime);
                    yield subtask;
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении строки задачи: " + value, e);
        }
    }


    private String toString(Task task) {
        String type = task instanceof Epic ? "EPIC"
                : task instanceof Subtask ? "SUBTASK"
                : "TASK";
        String epicId = task instanceof Subtask s ? String.valueOf(s.getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                task.getId(),
                type,
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                task.getDuration() != null ? task.getDuration().toMinutes() : 0,
                task.getStartTime() != null ? task.getStartTime() : "null"
        );
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
