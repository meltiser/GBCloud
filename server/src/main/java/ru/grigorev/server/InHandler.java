package ru.grigorev.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author Dmitriy Grigorev
 */
public class InHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client has connected!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            Message myMessage = (Message) msg;
            if (myMessage.getType().equals(MessageType.FILE_REQUEST)) {
                if (Files.exists(Paths.get("server/server_storage/" + myMessage.getFileName()))) {
                    Message fileMessage = new Message(MessageType.FILE, Paths.get("server/server_storage/" + myMessage.getFileName()));
                    ctx.writeAndFlush(fileMessage);
                }
            }
            if (myMessage.getType().equals(MessageType.REFRESH_REQUEST)) {
                Message refreshMessage = new Message(MessageType.REFRESH_RESPONSE);
                refreshMessage.setListFileNames(Files.list(Paths.get("server/server_storage"))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList()));
                ctx.writeAndFlush(refreshMessage);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
