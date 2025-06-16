package fun.ntony4u.kanban.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fun.ntony4u.kanban.service.TaskManager;
import fun.ntony4u.kanban.utils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson = GsonUtils.getGson();

    protected BaseHttpHandler(TaskManager taskManager) {
        if (taskManager == null) {
            throw new IllegalArgumentException("TaskManager cannot be null");
        }
        this.taskManager = taskManager;
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendSuccess(HttpExchange exchange, Object responseObject) throws IOException {
        String response = gson.toJson(responseObject);
        sendText(exchange, response, HttpURLConnection.HTTP_OK);
    }

    protected void sendCreated(HttpExchange exchange, Object responseObject) throws IOException {
        String response = gson.toJson(responseObject);
        sendText(exchange, response, HttpURLConnection.HTTP_CREATED);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        String errorJson = gson.toJson(Map.of("error", "Object not found"));
        sendText(exchange, errorJson, HttpURLConnection.HTTP_NOT_FOUND);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        String errorJson = gson.toJson(Map.of(
                "error", "Task time overlaps with existing tasks",
                "code", "time_overlap"
        ));
        sendText(exchange, errorJson, HttpURLConnection.HTTP_NOT_ACCEPTABLE);
    }

    protected void sendInternalError(HttpExchange exchange, Exception e) throws IOException {
        String errorJson = gson.toJson(Map.of(
                "error", "Internal server error",
                "details", e.getMessage()
        ));
        sendText(exchange, errorJson, HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}