package org.jobtests;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class DBHandler extends ChannelInboundHandlerAdapter {
    static private final DatabaseUtility dbu = DatabaseUtility.getInstance();

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            String text = ((Message) msg).getText();
            System.out.println("db handler get: " + text);
            if (((Message) msg).getText().startsWith("history ")) {
                Integer count = Integer.valueOf(text.substring(8));
                List<Message> messages = dbu.getLastMessages(count);
                System.out.println("start sending messages");
                for (Message message : messages) {
                    ctx.write(new TextWebSocketFrame(message.getName() + " : " + message.getText()));
                }
                System.out.println("end sending messages");
                ctx.flush();
            }
            dbu.addMessage((Message) msg);
        }
    }
}
