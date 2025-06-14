package fun.ntony4u.kanban.server;

import fun.ntony4u.kanban.service.InMemoryTaskManager;
import fun.ntony4u.kanban.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class HttpTaskServerTestBase {
    protected TaskManager taskManager;
    protected HttpTaskServer taskServer;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }
}