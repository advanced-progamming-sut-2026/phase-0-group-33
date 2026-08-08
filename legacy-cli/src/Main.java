import models.App;
import views.MenuHub;

public class Main {
    public static void main(String[] args) {
        App app = App.getInstance();
        app.run();
        MenuHub.getInstance(app).run();
    }
}
