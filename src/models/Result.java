package models;

import java.util.ArrayList;

public class Result {
    private boolean success;
    private ArrayList<String> messages = new ArrayList<>();
    private Object data;

    public Result() {
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