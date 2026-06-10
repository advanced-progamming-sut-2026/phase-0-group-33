package controllers.managers;

import models.Result;
import models.user.User;

public class UserManagerTest {
    public static void main(String[] args) {
        UserManager manager = UserManager.getInstance();
        Result result = manager.login("hrdsrt", "Hirad@1010");
        if (result.isSuccessfull()) {
            User user = manager.getCurrentUser();
            System.out.println(user.getUsername());
            System.out.println(user.getNickname());
        }
    }
}
