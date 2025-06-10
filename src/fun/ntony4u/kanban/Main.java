package fun.ntony4u.kanban;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    private static final TaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) {
        System.out.println("Поехали!");

        operateWithTasks();
        printAllTasks();
        addhistoryGetCalls();
    }

    private static void operateWithTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("task1", "nameOf task1");
        task1.setDuration(Duration.ofMinutes(30));
        task1.setStartTime(now);

        Task task2 = new Task("task2", "nameOf task2", Status.NEW);
        task2.setDuration(Duration.ofHours(1));
        task2.setStartTime(now.plusHours(2));

        inMemoryTaskManager.addTask(task1);
        inMemoryTaskManager.addTask(task2);

        Epic epic1 = new Epic("epic1", "nameOf epic1");
        inMemoryTaskManager.addEpic(epic1);

        Subtask epic1subtask1 = new Subtask("epic1subtask1", "nameOf epic1subtask1", epic1.getId());
        epic1subtask1.setDuration(Duration.ofMinutes(45));
        epic1subtask1.setStartTime(now.plusHours(4)); // меняем на plusHours(2) получаем exception

        Subtask epic1subtask2 = new Subtask("epic1subtask2", "", epic1.getId());
        epic1subtask2.setDuration(Duration.ofMinutes(15));
        epic1subtask2.setStartTime(now.plusHours(5));

        Subtask epic1subtask3 = new Subtask("epic1subtask3", "", epic1.getId());
        inMemoryTaskManager.addSubtask(epic1subtask1);
        inMemoryTaskManager.addSubtask(epic1subtask2);
        inMemoryTaskManager.addSubtask(epic1subtask3);

        Epic epic2 = new Epic("epic2", "nameOf epic2");
        inMemoryTaskManager.addEpic(epic2);

        Subtask epic2subtask1 = new Subtask("epic2subtask1", "nameOf epic2subtask1", epic2.getId());
        inMemoryTaskManager.addSubtask(epic2subtask1);

        task1.setStatus(Status.DONE);
        task2.setStatus(Status.IN_PROGRESS);
        epic1subtask1.setStatus(Status.NEW);
        epic1subtask2.setStatus(Status.NEW);
        epic2subtask1.setStatus(Status.DONE);

        inMemoryTaskManager.updateSubtask(epic2subtask1);

        inMemoryTaskManager.deleteTaskById(task1.getId());//для проверки истории просмотров комментируем строку
        inMemoryTaskManager.deleteEpicById(epic1.getId()); //для проверки истории просмотров комментируем строку

        Epic epic3 = new Epic("epic3", "Description3");
        Epic epic9 = new Epic("epic9", "Description9");
        inMemoryTaskManager.addEpic(epic3);
        inMemoryTaskManager.addEpic(epic9);

        Subtask epic3subtask1 = new Subtask("epic3subtask1", "Description epicId3", epic3.getId());
        inMemoryTaskManager.addSubtask(epic3subtask1);

        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getSubtaskById(8);
    }

    private static void printAllTasks() {
        System.out.println("Задачи:");
        inMemoryTaskManager.getTasks().forEach(System.out::println);

        System.out.println("Эпики:");
        inMemoryTaskManager.getEpics().forEach(epic -> {
            System.out.println(epic);
            inMemoryTaskManager.getEpicSubtasks(epic).forEach(subtask ->
                    System.out.println("--> " + subtask));
        });

        System.out.println("Подзадачи:");
        inMemoryTaskManager.getSubtasks().forEach(System.out::println);

        System.out.println("История:");
        inMemoryTaskManager.getHistory().forEach(System.out::println);

        System.out.println("Приоритетные задачи:");
        inMemoryTaskManager.getPrioritizedTasks().forEach(System.out::println);
    }

    private static void addhistoryGetCalls() {
        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getEpicById(9);
        inMemoryTaskManager.getSubtaskById(4);
        inMemoryTaskManager.getSubtaskById(5);
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getSubtaskById(8);
        inMemoryTaskManager.getEpicById(7);
        inMemoryTaskManager.getEpicById(9);
        inMemoryTaskManager.getSubtaskById(4);
        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getSubtaskById(11);
        inMemoryTaskManager.getEpicById(9);
        inMemoryTaskManager.getSubtaskById(5);

        System.out.println();
        System.out.println("История просмотров:");
        inMemoryTaskManager.getHistory().forEach(System.out::println);
    }
}
