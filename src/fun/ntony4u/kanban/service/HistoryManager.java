package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}