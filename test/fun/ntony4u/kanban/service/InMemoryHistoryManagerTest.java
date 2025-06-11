package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static fun.ntony4u.kanban.service.Status.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void historyManagerPreservesTaskState() {
        Task task = new Task(1, "task", "Description", NEW);
        Task taskCopy = new Task(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus()
        );
        historyManager.add(taskCopy);

        task.setStatus(DONE);
        List<Task> history = historyManager.getHistory();

        assertEquals(NEW, history.get(0).getStatus(),
                "История должна хранить исходное состояние.");
    }

    @Test
    void historyManagerHandlesNull() {
        historyManager.add(null);
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    void testHistoryUnlimitedSize() {
        for (int i = 0; i < 100; i++) {
            Task task = new Task(i, "Task " + i, "Description", NEW);
            historyManager.add(task);
        }

        assertEquals(100, historyManager.getHistory().size(), "История должна хранить все задачи");
    }

    @Test
    void testHistoryLinkedListOperations() {
        Task task1 = new Task(1, "Task 1", "Description1", NEW);
        Task task2 = new Task(2, "Task 2", "Description2", NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(task1.getId(), history.get(0).getId(), "Первая задача должна быть первой в истории");
        assertEquals(task2.getId(), history.get(1).getId(), "Вторая задача должна быть второй в истории");

        historyManager.remove(task1.getId());
        history = historyManager.getHistory();
        assertEquals(1, history.size(), "В истории должна остаться одна задача");
        assertEquals(task2.getId(), history.get(0).getId(), "Оставшаяся задача должна быть task2");
    }


    @Test
    void addShouldStoreTaskInHistory() {
        Task task = new Task(1, "Task", "Description", Status.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с добавленной");
    }

    @Test
    void addShouldNotStoreDuplicates() {
        Task task = new Task(1, "Task", "Description", Status.NEW);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
    }

    @Test
    void removeShouldDeleteTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу после удаления");
        assertEquals(task2, history.get(0), "Оставшаяся задача должна быть task2");
    }

    @Test
    void getHistoryShouldReturnEmptyListForEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой для нового менеджера");
    }

    @Test
    void removeFromBeginningOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);
        Task task3 = new Task(3, "Task 3", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void removeFromMiddleOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);
        Task task3 = new Task(3, "Task 3", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void removeFromEndOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);
        Task task3 = new Task(3, "Task 3", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void historyShouldMaintainOrder() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);
        Task task3 = new Task(3, "Task 3", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1); // Повторное добавление должно переместить в конец

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 уникальные задачи");
        assertEquals(task2, history.get(0), "Первый элемент должен быть task2");
        assertEquals(task3, history.get(1), "Второй элемент должен быть task3");
        assertEquals(task1, history.get(2), "Третий элемент должен быть task1");
    }
}