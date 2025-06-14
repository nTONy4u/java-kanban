package fun.ntony4u.kanban.server;

import fun.ntony4u.kanban.model.Epic;
import fun.ntony4u.kanban.model.Subtask;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class SubtasksHandlerTest extends HttpTaskServerTestBase {

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Desc"));

        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        String subtaskJson = taskServer.getGson().toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, taskManager.getSubtasks().size());
    }
}