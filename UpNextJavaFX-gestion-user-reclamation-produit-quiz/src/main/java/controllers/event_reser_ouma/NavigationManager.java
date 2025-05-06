package controllers.event_reser_ouma;

import javafx.scene.Scene;

import java.util.Stack;

public class NavigationManager {
    private static final Stack<Scene> history = new Stack<>();

    public static void push(Scene scene) {
        history.push(scene);
    }

    public static Scene pop() {
        return history.isEmpty() ? null : history.pop();
    }
}
