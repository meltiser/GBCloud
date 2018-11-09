package ru.grigorev.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
            Message message = (Message) msg;
            if (message.getType().equals(MessageType.FILE_REQUEST)) {
                if (Files.exists(Paths.get("server/server_storage/" + message.getFileName()))) {
                    Message fileMessage = new Message(MessageType.FILE, Paths.get("server/server_storage/" + message.getFileName()));
                    ctx.writeAndFlush(fileMessage);
                }
            }
            if (message.getType().equals(MessageType.FILE)) {
                Files.write(Paths.get("server/server_storage/" + message.getFileName()), message.getByteArr(), StandardOpenOption.CREATE);
            }
            if (message.getType().equals(MessageType.REFRESH_REQUEST)) {
                ctx.writeAndFlush(getRefreshResponseMessage());
            }
            if (message.getType().equals(MessageType.DELETE_FILE)) {
                try {
                    Files.delete(Paths.get("server/server_storage/" + message.getFileName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private Message getRefreshResponseMessage() throws IOException {
        Message refreshMessage = new Message(MessageType.REFRESH_RESPONSE);
        refreshMessage.setListFileNames(Files.list(Paths.get("server/server_storage"))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList()));
        return refreshMessage;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
