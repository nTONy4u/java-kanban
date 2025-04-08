package fun.ntony4u.kanban;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.service.*;

import java.util.List;

public class Main {

    private static final TaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) {
        System.out.println("Поехали!");

        operateWithTasks();
        printAllTasks();
        addhistoryGetCalls();
    }

    private static void operateWithTasks() {
        Task task1 = new Task("task1", "nameOf task1");
        Task task2 = new Task("task2", "nameOf task2", Status.NEW);
        inMemoryTaskManager.addTask(task1);
        inMemoryTaskManager.addTask(task2);

        Epic epic1 = new Epic("epic1", "nameOf epic1");
        inMemoryTaskManager.addEpic(epic1);

        Subtask epic1subtask1 = new Subtask("epic1subtask1", "nameOf epic1subtask1", epic1.getId());
        Subtask epic1subtask2 = new Subtask("epic1subtask2", "", epic1.getId());
        inMemoryTaskManager.addSubtask(epic1subtask1);
        inMemoryTaskManager.addSubtask(epic1subtask2);

        List<Subtask> epicSubtasks1 = inMemoryTaskManager.getEpicSubtasks(epic1);

        Epic epic2 = new Epic("epic2", "nameOf epic2");
        inMemoryTaskManager.addEpic(epic2);

        Subtask epic2subtask1 = new Subtask("epic2subtask1", "nameOf epic2subtask1", epic2.getId());
        inMemoryTaskManager.addSubtask(epic2subtask1);

        List<Subtask> epicSubtasks2 = inMemoryTaskManager.getEpicSubtasks(epic2);

        task1.setStatus(Status.DONE);
        task2.setStatus(Status.IN_PROGRESS);
        epic1subtask1.setStatus(Status.NEW);
        epic1subtask2.setStatus(Status.NEW);
        epic2subtask1.setStatus(Status.DONE);

        inMemoryTaskManager.updateSubtask(epic2subtask1);

        inMemoryTaskManager.deleteTaskById(task1.getId());
        inMemoryTaskManager.deleteEpicById(epic1.getId());

        Epic epic3 = new Epic("epic3", "Description3");
        Epic epic8 = new Epic("epic8", "Description8");
        inMemoryTaskManager.addEpic(epic3);
        inMemoryTaskManager.addEpic(epic8);

        Subtask epic8subtask1 = new Subtask("epic8subtask1", "Description epicId8", 8);
        inMemoryTaskManager.addSubtask(epic8subtask1);

        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getSubtaskById(7);
    }

    private static void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : inMemoryTaskManager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : inMemoryTaskManager.getEpics()) {
            System.out.println(epic);

            for (Task task : inMemoryTaskManager.getEpicSubtasks(epic)) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : inMemoryTaskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }

    private static void addhistoryGetCalls() {
        //смотрели в operateWithTasks() id2, 7, добавили 11 просмотров, в истории последние 10, просмотр 6 исчезнет
        inMemoryTaskManager.getTaskById(6);
        inMemoryTaskManager.getEpicById(9);
        inMemoryTaskManager.getSubtaskById(7);
        inMemoryTaskManager.getSubtaskById(10);
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getEpicById(8);
        inMemoryTaskManager.getSubtaskById(7);
        inMemoryTaskManager.getEpicById(6);
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getSubtaskById(7);
        inMemoryTaskManager.getEpicById(9);

        System.out.println();
        System.out.println("История просмотров:");
        for (Task task : inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
