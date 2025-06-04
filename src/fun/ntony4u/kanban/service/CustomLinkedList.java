package fun.ntony4u.kanban.service;

import java.util.ArrayList;
import java.util.List;

public class CustomLinkedList<T> {


    public static class Node<T> {
        T data;
        Node<T> next;
        Node<T> prev;

        public Node(Node<T> prev, T data, Node<T> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node<T> head;
    private Node<T> tail;

    public void linkLast(T data) {
        final Node<T> newNode = new Node<>(tail, data, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
    }

    public void removeNode(Node<T> node) {
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    public List<T> getTasks() {
        List<T> tasks = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            tasks.add(current.data);
            current = current.next;
        }
        return tasks;
    }

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }
}