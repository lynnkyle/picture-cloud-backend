package org.example.picturecloudbackend.test;

import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/ws")
public class WebSocketTest {

    private static final CopyOnWriteArraySet<WebSocketTest> clients = new CopyOnWriteArraySet<>();
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        clients.add(this);
        System.out.println("新连接接入，当前在线：" + clients.size());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("收到消息：" + message);
        // 群发
        for (WebSocketTest client : clients) {
            try {
                client.session.getBasicRemote().sendText("服务端回复：" + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose() {
        clients.remove(this);
        System.out.println("连接关闭，当前在线：" + clients.size());
    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }
}