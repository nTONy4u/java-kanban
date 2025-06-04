package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, CustomLinkedList.Node<Task>> historyGetCalls = new HashMap<>();
    private final CustomLinkedList<Task> historyList = new CustomLinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        int id = task.getId();
        remove(id);
        historyList.linkLast(task);
        historyGetCalls.put(id, historyList.getTail());
    }

    @Override
    public List<Task> getHistory() {
        return historyList.getTasks();
    }

    @Override
    public void remove(int id) {
        CustomLinkedList.Node<Task> node = historyGetCalls.remove(id);
        historyList.removeNode(node);
    }
}