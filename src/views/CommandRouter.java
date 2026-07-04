package views;

import models.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small table-driven command dispatcher shared by all menus.
 * Each menu registers its (pattern, handler) pairs once
 * finds the first matching pattern, runs its handler and prints the result.
 */
public class CommandRouter {

    /** A single command handler: receives the successful matcher, returns a printable result. */
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

    /** Registers a command route. Returns this router for chaining. */
    public CommandRouter add(Pattern pattern, Handler handler) {
        routes.add(new Route(pattern, handler));
        return this;
    }

    /**
     * Runs the first route whose pattern matches the input and prints its messages.
     *
     * @return true when some route handled the input, false when nothing matched
     */
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
