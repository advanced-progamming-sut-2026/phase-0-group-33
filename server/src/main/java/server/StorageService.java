package server;

import net.Packet;
import net.Protocol;
import utils.FileStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public final class StorageService {

    private static final Pattern OWNED = Pattern.compile(
            "(users/|user_|news_|zen_|match_)[A-Za-z0-9-]{1,64}\\.(properties|txt)");
    private static final Pattern SHARED = Pattern.compile("[A-Za-z0-9-]{1,64}\\.(properties|txt)");
    private static final int MAX_LINES = 20000;

    public void register(Map<String, BiConsumer<ClientSession, Packet>> routes) {
        routes.put(Protocol.FILE_READ, this::read);
        routes.put(Protocol.FILE_WRITE, this::write);
        routes.put(Protocol.FILE_LIST, this::list);
        routes.put(Protocol.FILE_RENAME, this::rename);
        routes.put(Protocol.FILE_DELETE, this::delete);
        routes.put(Protocol.FILE_EXISTS, this::exists);
    }

    static boolean isAllowed(String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.contains("\\")) {
            return false;
        }
        return OWNED.matcher(fileName).matches() || SHARED.matcher(fileName).matches();
    }

    private void read(ClientSession session, Packet request) {
        String file = request.str("file");
        if (!isAllowed(file)) {
            session.deny(request, "That file name is not allowed.");
            return;
        }
        List<Object> lines = new ArrayList<>(FileStore.readLines(file));
        session.ok(request, Packet.of(request.type()).put("lines", lines));
    }

    private void write(ClientSession session, Packet request) {
        String file = request.str("file");
        if (!isAllowed(file)) {
            session.deny(request, "That file name is not allowed.");
            return;
        }
        List<String> lines = request.list("lines");
        if (lines.size() > MAX_LINES) {
            session.deny(request, "That file is too large.");
            return;
        }
        boolean written = FileStore.writeLines(file, lines);
        session.reply(request, Packet.of(request.type()).put(Protocol.OK, written));
    }

    private void list(ClientSession session, Packet request) {
        String dir = request.str("dir");
        if (!"users".equals(dir)) {
            session.deny(request, "That folder cannot be listed.");
            return;
        }
        List<Object> names = new ArrayList<>(FileStore.listFiles(dir));
        session.ok(request, Packet.of(request.type()).put("names", names));
    }

    private void rename(ClientSession session, Packet request) {
        String from = request.str("from");
        String to = request.str("to");
        if (!isAllowed(from) || !isAllowed(to)) {
            session.deny(request, "That file name is not allowed.");
            return;
        }
        FileStore.rename(from, to);
        session.ok(request);
    }

    private void delete(ClientSession session, Packet request) {
        String file = request.str("file");
        if (!isAllowed(file)) {
            session.deny(request, "That file name is not allowed.");
            return;
        }
        FileStore.delete(file);
        session.ok(request);
    }

    private void exists(ClientSession session, Packet request) {
        String file = request.str("file");
        if (!isAllowed(file)) {
            session.deny(request, "That file name is not allowed.");
            return;
        }
        session.ok(request, Packet.of(request.type()).put("exists", FileStore.exists(file)));
    }
}
