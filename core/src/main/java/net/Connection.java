package net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class Connection implements Closeable {

    private final Socket socket;
    private final BufferedReader reader;
    private final Writer writer;
    private final Object sendLock = new Object();

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.socket.setTcpNoDelay(true);
        this.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
    }

    public void send(Packet packet) throws IOException {
        synchronized (sendLock) {
            writer.write(packet.encode());
            writer.write('\n');
            writer.flush();
        }
    }

    public Packet receive() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        if (line.isEmpty()) {
            return Packet.of("");
        }
        return Packet.decode(line);
    }

    public String remoteName() {
        return socket.getInetAddress() == null ? "?" : socket.getInetAddress().getHostAddress();
    }

    public boolean isOpen() {
        return !socket.isClosed() && socket.isConnected();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            return;
        }
    }
}
