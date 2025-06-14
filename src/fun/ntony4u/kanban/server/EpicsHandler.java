package fun.ntony4u.kanban.server;

import com.sun.net.httpserver.HttpExchange;
import fun.ntony4u.kanban.model.Epic;
import fun.ntony4u.kanban.service.NotFoundException;
import fun.ntony4u.kanban.service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
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
                        handleGetAllEpics(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetEpicById(exchange, pathParts[2]);
                    } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
                        handleGetEpicSubtasks(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    if (pathParts.length == 2) {
                        handleCreateEpic(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        handleDeleteEpic(exchange, pathParts[2]);
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

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        sendSuccess(exchange, taskManager.getEpics());
    }

    private void handleGetEpicById(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            Optional<Epic> epic = taskManager.getEpicById(id);
            if (epic.isPresent()) {
                sendSuccess(exchange, epic.get());
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            Optional<Epic> epic = taskManager.getEpicById(id);
            if (epic.isPresent()) {
                sendSuccess(exchange, taskManager.getEpicSubtasks(epic.get()));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }

    private void handleCreateEpic(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);
            Epic createdEpic = taskManager.addEpic(epic);
            sendCreated(exchange, createdEpic);
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            taskManager.deleteEpicById(id);
            sendSuccess(exchange, "Эпик удален");
        } catch (NumberFormatException | NotFoundException e) {
            sendNotFound(exchange);
        }
    }
}