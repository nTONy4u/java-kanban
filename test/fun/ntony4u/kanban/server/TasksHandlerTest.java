package fun.ntony4u.kanban.server;

import fun.ntony4u.kanban.model.Task;
import fun.ntony4u.kanban.service.Status;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TasksHandlerTest extends HttpTaskServerTestBase {

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = taskServer.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        assertEquals(1, taskManager.getTasks().size(), "Должна быть одна задача в менеджере");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = taskManager.addTask(new Task("Test", "Description"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        Task responseTask = taskServer.getGson().fromJson(response.body(), Task.class);
        assertEquals(task.getId(), responseTask.getId(), "ID задачи должны совпадать");
    }

    @Test
    void testGetNonExistentTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = taskManager.addTask(new Task("Test", "Description"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");
        assertTrue(taskManager.getTasks().isEmpty(), "Задача должна быть удалена из менеджера");
    }
}