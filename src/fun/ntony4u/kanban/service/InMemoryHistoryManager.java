package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_LIMIT = 10;
    private final List<Task> historyGetCalls = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        if (historyGetCalls.size() == HISTORY_LIMIT) {
            historyGetCalls.removeFirst();
        }
        historyGetCalls.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(historyGetCalls);
    }
}
