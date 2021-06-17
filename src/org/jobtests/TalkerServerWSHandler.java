package org.jobtests;

import io.jsonwebtoken.Jwts;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class TalkerServerWSHandler extends ChannelInboundHandlerAdapter {

    WebSocketServerHandshaker handshaker;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {

            HttpRequest httpRequest = (HttpRequest) msg;
            System.out.println("Http Request Received");
            System.out.println("Method : " + httpRequest.method().name());
            HttpHeaders headers = httpRequest.headers();

            System.out.println("Connection : " + headers.get("Connection"));
            System.out.println("Upgrade : " + headers.get("Upgrade"));

            if ("Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                    "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {

                //Adding new handler to the existing pipeline to handle WebSocket Messages
                ctx.pipeline().replace(this, "websocketHandler", new WebSocketHandler());

                System.out.println("WebSocketHandler added to the pipeline");
                System.out.println("Opened Channel : " + ctx.channel());
                System.out.println("Handshaking....");
                //Do the Handshake to upgrade connection from HTTP to WebSocket protocol
                handleHandshake(ctx, httpRequest);
                System.out.println("Handshake is done");
            }
        } else if (msg instanceof ByteBufHolder) {
            // its body of post request
            String command = ((ByteBufHolder) msg).content().toString(CharsetUtil.UTF_8);
            System.out.println(command);
            //!unsecured, remove from prod
            if (command.startsWith("quit") || command.startsWith("exit") || command.equals("\\q;")) {
                ctx.channel().close();
                ctx.channel().parent().close();
            }
            JSONParser parser = new JSONParser();
            String name = "";
            String hash = "";
            try {
                JSONObject jo = (JSONObject) parser.parse(command);
                name = jo.get("name").toString();
                hash = jo.get("hash").toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DatabaseUtility dbu = DatabaseUtility.getInstance();
            if (dbu.checkUser(name, hash)){
                //create token
                Key key = KeyKeeper.getKey();
                //expiration at one hour
                Date exp = new Date(new Date().getTime() + 1000 * 3600);
                String jwt = Jwts.builder().setSubject(name).setExpiration(exp).signWith(key).compact();
                System.out.println("jwt got: " + jwt);
                jwt = "{\"token\" : \"" + jwt + "\"}";
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(jwt.getBytes(StandardCharsets.UTF_8))
                        );
                ctx.writeAndFlush(response);
                ctx.close();
            }
        } else {
            System.out.println("Incoming request is unknown");
            ReferenceCountUtil.release(msg);
        }
    }

    /* Do the handshaking for WebSocket request */
    protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }


    protected String getWebSocketURL(HttpRequest req) {
        System.out.println("Req URI : " + req.uri());
        String url = "ws://" + req.headers().get("Host") + req.uri();
        System.out.println("Constructed URL : " + url);
        return url;
    }


}