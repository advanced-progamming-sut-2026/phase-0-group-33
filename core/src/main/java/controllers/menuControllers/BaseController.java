package controllers.menuControllers;

import models.App;

public abstract class BaseController implements AppController {
    protected final App app;

    public BaseController(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    protected boolean loggedOut() {
        return app.getCurrentUser() == null;
    }

    protected models.Result notLoggedIn() {
        return models.Result.fail("You must be logged in to do that.");
    }
}
