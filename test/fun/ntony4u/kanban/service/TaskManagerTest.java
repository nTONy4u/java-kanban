package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static fun.ntony4u.kanban.service.Status.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() throws Exception {
        taskManager = createTaskManager();
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        final Task addedTask = taskManager.addTask(task);
        final Optional<Task> savedTask = taskManager.getTaskById(addedTask.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task.getName(), savedTask.get().getName(), "Имена задач не совпадают.");
        assertEquals(task.getDescription(), savedTask.get().getDescription(), "Описания задач не совпадают.");
        assertEquals(task.getStatus(), savedTask.get().getStatus(), "Статусы задач не совпадают.");

        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Список задач не должен быть null.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void tasksWithSameIdAreEqual() {
        Task task1 = new Task(1, "task1", "Description", NEW);
        Task task2 = new Task(1, "task1 updated", "New Description", DONE);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды должны совпадать.");
    }

    @Test
    void subtasksWithSameIdAreEqual() {
        Epic epic = new Epic("epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask(1, "subtask1", "Description", NEW, epic.getId());
        Subtask subtask2 = new Subtask(1, "subtask1 updated", "New Description", DONE, epic.getId());

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны.");
    }

    @Test
    void epicsWithSameIdAreEqual() {
        Epic epic1 = new Epic(1, "epic1", "Description", NEW);
        Epic epic2 = new Epic(1, "epic1 Updated", "New Description", DONE);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны.");
    }

    @Test
    void shouldNotAddEpicAsSubtaskToItself() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.addEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("subtask", "Test Description", epicId);
        subtask.setId(epicId);

        Subtask result = taskManager.addSubtask(subtask);

        assertTrue(taskManager.getSubtaskById(epicId).isEmpty(),
                "Подзадача с id эпика не должна быть добавлена");
        assertEquals(subtask, result,
                "Метод должен вернуть ту же подзадачу, которая передавалась");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("subtask", "Description", epic.getId());

        assertDoesNotThrow(() -> taskManager.addSubtask(subtask),
                "Подзадача не может быть своим эпиком в текущей реализации");
    }

    @Test
    void taskManagerAddsAndFindsTasks() {
        Task task = new Task("task", "Description");
        Epic epic = new Epic("epic", "Description");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("subtask", "Description", epic.getId());

        taskManager.addTask(task);
        taskManager.addSubtask(subtask);

        assertNotNull(taskManager.getTaskById(task.getId()), "Задача не найдена.");
        assertNotNull(taskManager.getEpicById(epic.getId()), "Эпик не найден.");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не найдена.");
    }

    @Test
    void generatedAndManualIdsDoNotConflict() {
        Task task1 = new Task("task1", "Description");
        Task task2 = new Task(1, "task2", "Description", NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(2, taskManager.getTasks().size(), "Обе задачи должны быть добавлены.");
        assertNotEquals(task1.getId(), task2.getId(), "Id не должны совпадать.");
    }

    @Test
    void taskRemainsUnchangedAfterAdding() {
        Task originalTask = new Task("originalTask", "Description", NEW);
        Task addedTask = taskManager.addTask(originalTask);

        assertEquals(originalTask.getName(), addedTask.getName(), "Имя изменилось.");
        assertEquals(originalTask.getDescription(), addedTask.getDescription(), "Описание изменилось.");
        assertEquals(originalTask.getStatus(), addedTask.getStatus(), "Статус изменился.");
    }

    @Test
    void testEpicSubtaskIntegration() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", "Description", epic.getId());
        Subtask subtask2 = new Subtask("subtask2", "Description", epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteSubtaskById(subtask1.getId());

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic);
        assertEquals(1, epicSubtasks.size(), "Должна остаться одна подзадача");
        assertFalse(epicSubtasks.contains(subtask1), "Удаленная подзадача не должна оставаться в эпике");
        assertTrue(epicSubtasks.contains(subtask2), "Оставшаяся подзадача должна быть в эпике");
    }

    @Test
    void epicStatusShouldBeNewWhenNoSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        assertEquals(NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(NEW, epic.getStatus(), "Статус эпика со всеми подзадачами NEW должен быть NEW");
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        subtask1.setStatus(DONE);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        subtask2.setStatus(DONE);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(DONE, epic.getStatus(), "Статус эпика со всеми подзадачами DONE должен быть DONE");
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksNewAndDone() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        subtask1.setStatus(NEW);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        subtask2.setStatus(DONE);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(IN_PROGRESS, epic.getStatus(),
                "Статус эпика с подзадачами NEW и DONE должен быть IN_PROGRESS");
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        subtask1.setStatus(IN_PROGRESS);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        subtask2.setStatus(DONE);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(IN_PROGRESS, epic.getStatus(),
                "Статус эпика с хотя бы одной подзадачей IN_PROGRESS должен быть IN_PROGRESS");
    }

    @Test
    void subtaskShouldHaveEpic() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        taskManager.addSubtask(subtask);

        Optional<Subtask> savedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertTrue(savedSubtask.isPresent(), "Подзадача должна быть сохранена");
        assertEquals(epic.getId(), savedSubtask.get().getEpicId(),
                "Подзадача должна ссылаться на правильный эпик");
    }

    @Test
    void epicShouldContainSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic);
        assertEquals(2, epicSubtasks.size(), "Эпик должен содержать 2 подзадачи");
        assertTrue(epicSubtasks.contains(subtask1), "Эпик должен содержать подзадачу 1");
        assertTrue(epicSubtasks.contains(subtask2), "Эпик должен содержать подзадачу 2");
    }

    @Test
    void shouldDetectTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofHours(1));
        taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusMinutes(30)); // пересекается с task1
        task2.setDuration(Duration.ofHours(1));

        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(task2),
                "Должно быть исключение при пересечении задач");
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofHours(1));
        taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusHours(2)); // не пересекается
        task2.setDuration(Duration.ofHours(1));

        assertDoesNotThrow(() -> taskManager.addTask(task2),
                "Не должно быть исключения для непересекающихся задач");
    }

    @Test
    void shouldThrowWhenAddingNullTask() {
        assertThrows(NullPointerException.class, () -> taskManager.addTask(null),
                "Добавление null задачи должно вызывать исключение");
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentTask() {
        assertDoesNotThrow(() -> taskManager.deleteTaskById(999),
                "Удаление несуществующей задачи не должно вызывать исключений");
    }

    @Test
    void shouldHandleEmptyTaskLists() {
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        assertTrue(taskManager.getTaskById(999).isEmpty(),
                "Запрос несуществующей задачи должен возвращать Optional.empty()");
    }

    @Test
    void epicStatusShouldUpdateWhenSubtasksChange() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        taskManager.addSubtask(subtask);

        assertEquals(NEW, epic.getStatus(), "Статус должен быть NEW после добавления подзадачи");

        subtask.setStatus(IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        assertEquals(IN_PROGRESS, epic.getStatus(),
                "Статус должен измениться на IN_PROGRESS при изменении подзадачи");

        subtask.setStatus(DONE);
        taskManager.updateSubtask(subtask);
        assertEquals(DONE, epic.getStatus(),
                "Статус должен измениться на DONE при завершении подзадачи");
    }

    @Test
    void shouldAllowZeroDurationTasks() {
        Task task = new Task("Task", "Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ZERO);

        assertDoesNotThrow(() -> taskManager.addTask(task),
                "Задачи с нулевой длительностью должны быть разрешены");
    }

    @Test
    void shouldAllowTasksWithoutTime() {
        Task task = new Task("Task", "Description");

        assertDoesNotThrow(() -> taskManager.addTask(task),
                "Задачи без указания времени должны быть разрешены");
    }

    @Test
    void shouldDetectVariousOverlapCases() {
        LocalDateTime now = LocalDateTime.now();

        // Задача 1: 10:00 - 11:00
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now.withHour(10).withMinute(0));
        task1.setDuration(Duration.ofHours(1));
        taskManager.addTask(task1);

        // Случай 1: Полное перекрытие (10:30 - 11:30)
        Task overlapping1 = new Task("Overlapping 1", "Desc");
        overlapping1.setStartTime(now.withHour(10).withMinute(30));
        overlapping1.setDuration(Duration.ofHours(1));
        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(overlapping1),
                "Должно быть исключение при полном перекрытии");

        // Случай 2: Начало внутри (10:30 - 10:45)
        Task overlapping2 = new Task("Overlapping 2", "Desc");
        overlapping2.setStartTime(now.withHour(10).withMinute(30));
        overlapping2.setDuration(Duration.ofMinutes(15));
        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(overlapping2),
                "Должно быть исключение при начале внутри");

        // Случай 3: Окончание внутри (9:30 - 10:30)
        Task overlapping3 = new Task("Overlapping 3", "Desc");
        overlapping3.setStartTime(now.withHour(9).withMinute(30));
        overlapping3.setDuration(Duration.ofHours(1));
        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(overlapping3),
                "Должно быть исключение при окончании внутри");

        // Случай 4: Полное включение (9:00 - 12:00)
        Task overlapping4 = new Task("Overlapping 4", "Desc");
        overlapping4.setStartTime(now.withHour(9).withMinute(0));
        overlapping4.setDuration(Duration.ofHours(3));
        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(overlapping4),
                "Должно быть исключение при полном включении");
    }

    @Test
    void shouldDetectEdgeCaseOverlaps() {
        LocalDateTime now = LocalDateTime.now();

        // Задача 1: 10:00 - 10:30
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now.withHour(10).withMinute(0));
        task1.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(task1);

        // Случай 1: Точно в конце (10:30 - 11:00)
        Task edgeCase1 = new Task("Edge 1", "Desc");
        edgeCase1.setStartTime(now.withHour(10).withMinute(30));
        edgeCase1.setDuration(Duration.ofMinutes(30));
        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(edgeCase1),
                "Должно быть исключение при точном совпадении времени окончания");

        // Случай 2: Точно в начале (9:30 - 10:00)
        Task edgeCase2 = new Task("Edge 2", "Desc");
        edgeCase2.setStartTime(now.withHour(9).withMinute(30));
        edgeCase2.setDuration(Duration.ofMinutes(30));
        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(edgeCase2),
                "Должно быть исключение при точном совпадении времени начала");
    }
}