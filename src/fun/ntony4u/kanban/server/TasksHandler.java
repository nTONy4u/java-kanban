package fun.ntony4u.kanban.server;

import com.sun.net.httpserver.HttpExchange;
import fun.ntony4u.kanban.model.Task;
import fun.ntony4u.kanban.service.ManagerSaveException;
import fun.ntony4u.kanban.service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            switch (method) {
                case "GET":
                    if (pathParts.length == 2) {
                        handleGetAllTasks(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetTaskById(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateTask(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        handleDeleteTask(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        sendSuccess(exchange, taskManager.getTasks());
    }

    private void handleGetTaskById(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            Optional<Task> task = taskManager.getTaskById(id);
            if (task.isPresent()) {
                sendSuccess(exchange, task.get());
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            if (task == null) {
                sendNotFound(exchange);
                return;
            }

            try {
                Task resultTask;
                if (task.getId() == 0) {
                    resultTask = taskManager.addTask(task);
                    sendCreated(exchange, resultTask);
                } else {
                    resultTask = taskManager.updateTask(task);
                    if (resultTask != null) {
                        sendSuccess(exchange, resultTask);
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } catch (ManagerSaveException e) {
                sendHasInteractions(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalError(exchange, e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange, e);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            taskManager.deleteTaskById(id);
            sendSuccess(exchange, "Задача удалена");
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }
}