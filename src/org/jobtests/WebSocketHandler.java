package org.jobtests;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {
    static private final ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public WebSocketHandler() {
        System.out.println("WebSocket frame u!!!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("TalkerWS get " + msg.toString());
        if (msg instanceof WebSocketFrame) {
            System.out.println("This is a WebSocket frame");
            System.out.println("Client Channel : " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
                System.out.println("BinaryWebSocketFrame Received : ");
                System.out.println(((BinaryWebSocketFrame) msg).content());
            } else if (msg instanceof TextWebSocketFrame) {
                System.out.println("TextWebSocketFrame Received : ");
                String command = ((TextWebSocketFrame) msg).text();
                ctx.channel().writeAndFlush(
                        new TextWebSocketFrame("Message received : " + command));
                JSONParser parser = new JSONParser();
                String name;
                String text;
                try {
                    JSONObject jo = (JSONObject) parser.parse(command);
                    name = jo.get("name").toString();
                    text = jo.get("message").toString();
                    if (text.startsWith("auth ")) {
                        Key key = KeyKeeper.getKey();
                        text = text.substring(5);
                        try {
                            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(text).getBody();
                            for (String k : claims.keySet()) {
                                System.out.println(k + " : " + claims.get(k));
                            }
                            //and here we add handler for listen and speaking
                            recipients.add(ctx.channel());
                            System.out.println("Recipients Count: " + recipients.size());
                            ctx.pipeline().addLast("DBHandler", new DBHandler());
                            //ctx.fireChannelRead(new Message(name, "login"));
                            ctx.channel().writeAndFlush(new TextWebSocketFrame("Authorised as " + name));
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        Message message = new Message(name, text);
                        //ctx.fire - send msg to another handlers in this channel
                        ctx.fireChannelRead(message);
                        //recpts.write to future and await future - send msg to another channels
                        ChannelGroupFuture future = recipients.writeAndFlush(new TextWebSocketFrame(/*"to " + recipients.size() + " ch, " + */message.getName() + " : " + message.getText()));
                        future.awaitUninterruptibly();
                        System.out.println("message sent to upstreams: " + message);
                    }

                } catch (Exception e) {
                    String error = "wrong message '" + command + "' from channel " + ctx.channel().id();
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(error));
                    System.out.println(error);
                }

                ctx.channel().flush();
            } else if (msg instanceof PingWebSocketFrame) {
                System.out.println("PingWebSocketFrame Received : ");
                System.out.println(((PingWebSocketFrame) msg).content());
            } else if (msg instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame Received : ");
                System.out.println(((PongWebSocketFrame) msg).content());
            } else if (msg instanceof CloseWebSocketFrame) {
                System.out.println("CloseWebSocketFrame Received : ");
                System.out.println("ReasonText :" + ((CloseWebSocketFrame) msg).reasonText());
                System.out.println("StatusCode : " + ((CloseWebSocketFrame) msg).statusCode());
            } else {
                System.out.println("Unsupported WebSocketFrame");
            }
        } else if (msg instanceof Message) {
            String name = ((Message) msg).getName();
            String text = ((Message) msg).getText();
            Long ts = ((Message) msg).getTs();
            ctx.channel().writeAndFlush(
                    new TextWebSocketFrame("('ts': " + ts + ", 'name': '" + name + "', text: '" + text + "')"));
            System.out.println(ts + "/" + name + "/" + text);
        }
    }
}