package fun.ntony4u.kanban.server;

import com.sun.net.httpserver.HttpExchange;
import fun.ntony4u.kanban.model.Subtask;
import fun.ntony4u.kanban.service.ManagerSaveException;
import fun.ntony4u.kanban.service.NotFoundException;
import fun.ntony4u.kanban.service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
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
                        handleGetAllSubtasks(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetSubtaskById(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateSubtask(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        handleDeleteSubtask(exchange, pathParts[2]);
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

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        sendSuccess(exchange, taskManager.getSubtasks());
    }

    private void handleGetSubtaskById(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            Optional<Subtask> subtask = taskManager.getSubtaskById(id);
            if (subtask.isPresent()) {
                sendSuccess(exchange, subtask.get());
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask.getId() == 0) {
                Subtask createdSubtask = taskManager.addSubtask(subtask);
                sendCreated(exchange, createdSubtask);
            } else {
                Subtask updatedSubtask = taskManager.updateSubtask(subtask);
                if (updatedSubtask != null) {
                    sendSuccess(exchange, updatedSubtask);
                } else {
                    sendNotFound(exchange);
                }
            }
        } catch (ManagerSaveException e) {
            sendHasInteractions(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            taskManager.deleteSubtaskById(id);
            sendSuccess(exchange, "Подзадача удалена");
        } catch (NumberFormatException | NotFoundException e) {
            sendNotFound(exchange);
        }
    }
}