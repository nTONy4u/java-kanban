import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("task1", "nameOf task1");
        Task task2 = new Task("task2", "nameOf task2", Status.NEW);
        taskManager.addTask(task1);
        System.out.println(task1);
        taskManager.addTask(task2);
        System.out.println(task2);
        System.out.println("added tasks - " + taskManager.getTasks().size());

        Epic epic1 = new Epic("epic1", "nameOf epic1");
        taskManager.addEpic(epic1);

        Subtask epic1subtask1 = new Subtask("epic1subtask1", "nameOf epic1subtask1", epic1.getId());
        Subtask epic1subtask2 = new Subtask("epic1subtask2", "", epic1.getId());
        taskManager.addSubtask(epic1subtask1);
        taskManager.addSubtask(epic1subtask2);

        ArrayList<Subtask> epicSubtasks1 = taskManager.getEpicSubtasks(epic1);
        System.out.println("added tasks to epic1 - " + epicSubtasks1.size());
        System.out.println(epic1);

        Epic epic2 = new Epic("epic2", "nameOf epic2");
        taskManager.addEpic(epic2);

        Subtask epic2subtask1 = new Subtask("epic2subtask1", "nameOf epic2subtask1", epic2.getId());
        taskManager.addSubtask(epic2subtask1);

        ArrayList<Subtask> epicSubtasks2 = taskManager.getEpicSubtasks(epic2);
        System.out.println("added tasks to epic2 - " + epicSubtasks2.size());
        System.out.println(epic2);
        System.out.println("AllEpics: " + taskManager.getEpics());

        task1.setStatus(Status.DONE);
        task2.setStatus(Status.IN_PROGRESS);
        epic1subtask1.setStatus(Status.NEW);
        epic1subtask2.setStatus(Status.NEW);
        epic2subtask1.setStatus(Status.DONE);

        taskManager.updateSubtask(epic2subtask1);

        System.out.println("\nupdated status of epic " + taskManager.getEpics());

        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getEpicSubtasks(epic1));
        System.out.println(taskManager.getEpicSubtasks(epic2));

        taskManager.deleteTaskById(task1.getId());
        taskManager.deleteEpicById(epic1.getId());

        System.out.println("\nAfter delete:" + taskManager.getTasks());
        System.out.println(taskManager.getEpics());
    }
}
