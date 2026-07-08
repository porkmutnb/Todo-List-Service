package com.chermew.todolist.aspect;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogContext {
    private static final ThreadLocal<List<RepositoryActivity>> ACTIVITIES = ThreadLocal.withInitial(ArrayList::new);

    public static void clear() {
        ACTIVITIES.remove();
    }

    public static void addActivity(String tableName, String action, String entityId) {
        ACTIVITIES.get().add(new RepositoryActivity(tableName, action, entityId));
    }

    public static List<RepositoryActivity> getActivities() {
        return ACTIVITIES.get();
    }
}
