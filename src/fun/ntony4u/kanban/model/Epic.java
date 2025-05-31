package fun.ntony4u.kanban.model;

import fun.ntony4u.kanban.service.*;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtaskOfEpic = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public void addSubtask(Subtask subtask) {
        subtaskOfEpic.add(subtask);
    }

    public void clearSubtasks() {
        subtaskOfEpic.clear();
    }

    public ArrayList<Subtask> getSubtaskOfEpic() {
        return subtaskOfEpic;
    }

    public void setSubtaskOfEpic(ArrayList<Subtask> subtaskOfEpic) {
        this.subtaskOfEpic = subtaskOfEpic;
    }

    public void removeSubtaskOfEpic(Integer id) {
        for (int i = subtaskOfEpic.size() - 1; i >= 0; i--) {
            Subtask subtask = subtaskOfEpic.get(i);
            if (subtask.getId() == id) {
                subtaskOfEpic.remove(i);
            }
        }
    }

    @Override
    public String toString() {
        return "fun.ntony4u.kanban.model.Epic[" +
                "id=" + getId() +
                ", name= " + getName() + '\'' +
                ", description = " + getDescription() + '\'' +
                ", status = " + getStatus() +
                ", subtaskOfEpic.size = " + subtaskOfEpic.size() +
                "]";
    }
}