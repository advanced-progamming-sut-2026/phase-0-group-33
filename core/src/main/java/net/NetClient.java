package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class NetClient {

    public interface Listener {
        void onPush(Packet packet);
    }

    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int REPLY_TIMEOUT_MS = 8000;

    private static final NetClient INSTANCE = new NetClient();

    private final Map<Long, BlockingQueue<Packet>> pending = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    private volatile Connection connection;
    private volatile Thread pump;
    private volatile String host = Protocol.DEFAULT_HOST;
    private volatile int port = Protocol.DEFAULT_PORT;
    private volatile String failure = "";

    private NetClient() {
    }

    public static NetClient get() {
        return INSTANCE;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        Connection open = connection;
        return open != null && open.isOpen();
    }

    public String lastFailure() {
        return failure;
    }

    public String address() {
        return host + ":" + port;
    }

    public synchronized boolean connect(String wantedHost, int wantedPort) {
        disconnect();
        this.host = wantedHost == null || wantedHost.isBlank() ? Protocol.DEFAULT_HOST : wantedHost.trim();
        this.port = wantedPort <= 0 ? Protocol.DEFAULT_PORT : wantedPort;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(this.host, this.port), CONNECT_TIMEOUT_MS);
            connection = new Connection(socket);
            failure = "";
        } catch (IOException e) {
            failure = describe(e);
            connection = null;
            return false;
        }
        pump = new Thread(this::pump, "net-client");
        pump.setDaemon(true);
        pump.start();
        return true;
    }

    private String describe(IOException e) {
        String detail = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return "Cannot reach the server at " + host + ":" + port + " (" + detail + ").";
    }

    public synchronized void disconnect() {
        Connection open = connection;
        connection = null;
        if (open != null) {
            open.close();
        }
        Thread thread = pump;
        pump = null;
        if (thread != null) {
            thread.interrupt();
        }
        for (BlockingQueue<Packet> queue : pending.values()) {
            queue.offer(Packet.of(Protocol.MESSAGE).put(Protocol.OK, false)
                    .put(Protocol.MESSAGE, "The connection to the server was lost."));
        }
        pending.clear();
    }

    private void pump() {
        Connection open = connection;
        while (open != null && !Thread.currentThread().isInterrupted()) {
            Packet packet;
            try {
                packet = open.receive();
            } catch (IOException e) {
                break;
            }
            if (packet == null) {
                break;
            }
            deliver(packet);
            open = connection;
        }
        if (connection != null) {
            failure = "The connection to the server was lost.";
            disconnect();
            deliver(Packet.of(Protocol.LOGOUT).put("reason", "disconnected"));
        }
    }

    private void deliver(Packet packet) {
        long id = packet.big(Protocol.REQ, 0);
        if (id > 0) {
            BlockingQueue<Packet> queue = pending.remove(id);
            if (queue != null) {
                queue.offer(packet);
                return;
            }
        }
        for (Listener listener : listeners) {
            listener.onPush(packet);
        }
    }

    public Packet request(Packet packet) {
        Connection open = connection;
        if (open == null) {
            return fail("You are not connected to the server.");
        }
        long id = nextId.getAndIncrement();
        BlockingQueue<Packet> queue = new ArrayBlockingQueue<>(1);
        pending.put(id, queue);
        try {
            open.send(packet.put(Protocol.REQ, id));
            Packet reply = queue.poll(REPLY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return reply == null ? fail("The server did not answer in time.") : reply;
        } catch (IOException e) {
            return fail("The connection to the server was lost.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fail("The request was interrupted.");
        } finally {
            pending.remove(id);
        }
    }

    public void tell(Packet packet) {
        Connection open = connection;
        if (open == null) {
            return;
        }
        try {
            open.send(packet);
        } catch (IOException e) {
            disconnect();
        }
    }

    private Packet fail(String message) {
        return Packet.of(Protocol.MESSAGE).put(Protocol.OK, false).put(Protocol.MESSAGE, message);
    }
}
