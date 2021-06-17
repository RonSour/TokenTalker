package org.jobtests;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class DBHandler extends ChannelInboundHandlerAdapter {
    static private DatabaseUtility dbu = DatabaseUtility.getInstance();

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            System.out.println("db2");
            dbu.addMessage((Message)msg);
        }
    }
}
