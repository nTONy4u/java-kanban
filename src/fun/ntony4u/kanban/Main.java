package fun.ntony4u.kanban;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.service.*;

import java.util.List;

public class Main {

    private static final TaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager manager = new InMemoryTaskManager();

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
        Subtask epic1subtask3 = new Subtask("epic1subtask3", "", epic1.getId());
        inMemoryTaskManager.addSubtask(epic1subtask1);
        inMemoryTaskManager.addSubtask(epic1subtask2);
        inMemoryTaskManager.addSubtask(epic1subtask3);


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
        for (Task task : inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
