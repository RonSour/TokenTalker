package org.jobtests;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpWebSockHandler", new TalkerServerWSHandler());
        //pipeline.addLast("http-decoder", new HttpRequestDecoder());
        //pipeline.addLast("http-aggregator", new HttpObjectAggregator(1048576));
        //pipeline.addLast("http-encoder", new HttpResponseEncoder());

    }
}