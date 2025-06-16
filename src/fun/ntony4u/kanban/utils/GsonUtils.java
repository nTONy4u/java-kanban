package fun.ntony4u.kanban.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fun.ntony4u.kanban.adapters.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public static Gson getGson() {
        return gson;
    }
}