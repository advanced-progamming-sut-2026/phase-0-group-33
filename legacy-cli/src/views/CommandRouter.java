package views;

import models.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRouter {

    public interface Handler {
        Result handle(Matcher matcher);
    }

    private static class Route {
        private final Pattern pattern;
        private final Handler handler;

        Route(Pattern pattern, Handler handler) {
            this.pattern = pattern;
            this.handler = handler;
        }
    }

    private final List<Route> routes = new ArrayList<>();

    public CommandRouter add(Pattern pattern, Handler handler) {
        routes.add(new Route(pattern, handler));
        return this;
    }

    public boolean dispatch(String input) {
        for (Route route : routes) {
            Matcher matcher = route.pattern.matcher(input);
            if (matcher.matches()) {
                Result result;
                try {
                    result = route.handler.handle(matcher);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number in command!");
                    return true;
                }
                if (result != null) {
                    for (String message : result.getMessages()) {
                        System.out.println(message);
                    }
                }
                return true;
            }
        }
        return false;
    }
}
