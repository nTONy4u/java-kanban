package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void inMemoryTaskManagerShouldNotPersistData() {
        Task task = new Task("Task", "Description");
        taskManager.addTask(task);

        InMemoryTaskManager newManager = createTaskManager();
        assertTrue(newManager.getTasks().isEmpty(),
                "InMemoryTaskManager не должен сохранять данные между экземплярами");
    }

    @Test
    void shouldNotPersistHistoryBetweenInstances() {
        InMemoryTaskManager manager1 = createTaskManager();

        Task task1 = manager1.addTask(new Task("Task 1", "Description"));
        Epic epic1 = manager1.addEpic(new Epic("Epic 1", "Description"));
        Subtask subtask1 = manager1.addSubtask(new Subtask("Subtask 1", "Description", epic1.getId()));

        manager1.getTaskById(task1.getId());
        manager1.getEpicById(epic1.getId());
        manager1.getSubtaskById(subtask1.getId());

        assertEquals(3, manager1.getHistory().size(),
                "Первый менеджер должен содержать 3 элемента в истории");

        InMemoryTaskManager manager2 = createTaskManager();

        assertTrue(manager2.getHistory().isEmpty(),
                "Новый менеджер должен иметь пустую историю");

        Task task2 = manager2.addTask(new Task("Task 2", "Description"));
        manager2.getTaskById(task2.getId());

        assertEquals(1, manager2.getHistory().size(),
                "Второй менеджер должен иметь свою собственную историю");
        assertEquals(3, manager1.getHistory().size(),
                "История первого менеджера не должна измениться");
    }
}