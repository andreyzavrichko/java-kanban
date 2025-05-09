package ru.yandex.tasks;

public class Subtask extends Task {
    private final Integer epicId;

    public Subtask(String name, String description, Integer epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("Subtask{id=%d, name='%s', status=%s, epicId=%d}", id, name, status, epicId);
    }
}
