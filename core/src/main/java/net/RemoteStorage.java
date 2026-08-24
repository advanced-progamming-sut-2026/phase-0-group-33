package net;

import utils.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RemoteStorage implements Storage {

    private static final int FLUSH_SECONDS = 4;

    private final NetClient client;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
    private final ExecutorService writer =
            Executors.newSingleThreadExecutor(RemoteStorage::thread);

    public RemoteStorage(NetClient client) {
        this.client = client;
    }

    private static Thread thread(Runnable task) {
        Thread worker = new Thread(task, "net-writes");
        worker.setDaemon(true);
        return worker;
    }

    public void forget() {
        flush();
        cache.clear();
    }

    public void flush() {
        try {
            writer.submit(() -> {
            }).get(FLUSH_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public List<String> readLines(String fileName) {
        List<String> cached = cache.get(fileName);
        if (cached != null) {
            return new ArrayList<>(cached);
        }
        Packet reply = client.request(Packet.of(Protocol.FILE_READ).put("file", fileName));
        List<String> lines = reply.flag(Protocol.OK, false) ? reply.list("lines") : new ArrayList<>();
        cache.put(fileName, new ArrayList<>(lines));
        return lines;
    }

    @Override
    public boolean writeLines(String fileName, List<String> lines) {
        cache.put(fileName, new ArrayList<>(lines));
        final List<Object> payload = new ArrayList<>(lines);
        writer.submit(() -> client.request(Packet.of(Protocol.FILE_WRITE)
                .put("file", fileName).put("lines", payload)));
        return true;
    }

    @Override
    public List<String> listFiles(String directory) {
        flush();
        Packet reply = client.request(Packet.of(Protocol.FILE_LIST).put("dir", directory));
        return reply.flag(Protocol.OK, false) ? reply.list("names") : new ArrayList<>();
    }

    @Override
    public void rename(String fromFile, String toFile) {
        List<String> moved = cache.remove(fromFile);
        if (moved != null) {
            cache.put(toFile, moved);
        }
        writer.submit(() -> client.request(Packet.of(Protocol.FILE_RENAME)
                .put("from", fromFile).put("to", toFile)));
    }

    @Override
    public void delete(String fileName) {
        cache.put(fileName, new ArrayList<>());
        writer.submit(() -> client.request(Packet.of(Protocol.FILE_DELETE).put("file", fileName)));
    }

    @Override
    public boolean exists(String fileName) {
        List<String> cached = cache.get(fileName);
        if (cached != null) {
            return !cached.isEmpty();
        }
        Packet reply = client.request(Packet.of(Protocol.FILE_EXISTS).put("file", fileName));
        return reply.flag(Protocol.OK, false) && reply.flag("exists", false);
    }
}
