package at.irian.ankorman.sample2.domain.task;

import java.util.UUID;

// TODO: Resolve ambivalent naming: Task <-> Todo
public class Task {
    private String id;
    private String title = "";
    private boolean completed = false;

    public Task(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
    }

    public Task(Task t) {
        this.id = t.id;
        this.title = t.title;
        this.completed = t.completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (!id.equals(task.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}