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
}