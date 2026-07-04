package models;

import java.util.ArrayList;

public class Result {
    private boolean success;
    private ArrayList<String> messages = new ArrayList<>();
    private Object data;

    public Result() {
    }

    /** Convenience factory for a successful result with optional messages. */
    public static Result ok(String... messages) { // Can have multiple messages
        Result result = new Result();
        result.setSuccess(true);
        for (String message : messages) {
            result.addMessage(message);
        }
        return result;
    }

    /** Convenience factory for a failed result with optional messages. */
    public static Result fail(String... messages) { // Can have multiple messages
        Result result = new Result();
        result.setSuccess(false);
        for (String message : messages) {
            result.addMessage(message);
        }
        return result;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ArrayList<String> getMessages() {
        return this.messages;
    }

    public Object getData() {
        return this.data;
    }

    public void setSuccess(boolean status) {
        this.success = status;
    }

    public boolean isSuccessfull() {
        return this.success;
    }
}