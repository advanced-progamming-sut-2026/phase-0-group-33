package net;

import utils.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RemoteStorage implements Storage {

    private final NetClient client;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public RemoteStorage(NetClient client) {
        this.client = client;
    }

    public void forget() {
        cache.clear();
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
        Packet reply = client.request(Packet.of(Protocol.FILE_WRITE)
                .put("file", fileName).put("lines", new ArrayList<Object>(lines)));
        return reply.flag(Protocol.OK, false);
    }

    @Override
    public List<String> listFiles(String directory) {
        Packet reply = client.request(Packet.of(Protocol.FILE_LIST).put("dir", directory));
        return reply.flag(Protocol.OK, false) ? reply.list("names") : new ArrayList<>();
    }

    @Override
    public void rename(String fromFile, String toFile) {
        cache.remove(fromFile);
        cache.remove(toFile);
        client.request(Packet.of(Protocol.FILE_RENAME).put("from", fromFile).put("to", toFile));
    }

    @Override
    public void delete(String fileName) {
        cache.remove(fileName);
        client.request(Packet.of(Protocol.FILE_DELETE).put("file", fileName));
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
