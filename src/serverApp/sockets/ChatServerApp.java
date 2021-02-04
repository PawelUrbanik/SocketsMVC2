package serverApp.sockets;

import java.io.IOException;

public class ChatServerApp {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            return;
        }
        int port = Integer.parseInt(args[0]);
        ChatServer chat = new ChatServer(port);
        chat.start();
    }

}
