package ru.grigorev.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.grigorev.common.Info;
import ru.grigorev.common.message.Message;
import ru.grigorev.common.message.MessageType;
import ru.grigorev.common.utils.BigFileHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

/**
 * @author Dmitriy Grigorev
 */
public class MainInHandler extends ChannelInboundHandlerAdapter {
    private String login;
    private BigFileHandler bigFileHandler;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;
            if (bigFileHandler == null) bigFileHandler = new BigFileHandler();
            Message message = (Message) msg;
            login = message.getLogin() + "/";
            Path file = Paths.get(Info.SERVER_FOLDER_NAME + login + message.getFileName());
            if (!bigFileHandler.checkFolderExisting(Info.SERVER_FOLDER_NAME + login))
                ctx.writeAndFlush(getRefreshResponseMessage());

            if (message.getType().equals(MessageType.FILE_REQUEST)) {
                System.out.println(MessageType.FILE_REQUEST);

                if (Files.exists(file)) {
                    if (Files.size(file) <= Info.MAX_FILE_SIZE) sendSmallFile(ctx, file);
                    else bigFileHandler.sendBigFile(ctx, file);
                }
            }
            if (message.getType().equals(MessageType.FILE)) {
                System.out.println(MessageType.FILE);

                Files.write(file, message.getByteArr(), StandardOpenOption.CREATE);
            }
            if (message.getType().equals(MessageType.FILE_PART)) {
                System.out.println(MessageType.FILE_PART + " : " + message.getCurrentPart() + "/" + message.getPartsCount());

                if (!bigFileHandler.isWriting()) bigFileHandler.startWriting(file);
                bigFileHandler.continueWriting(message);
            }
            if (message.getType().equals(MessageType.REFRESH_REQUEST)) {
                System.out.println(MessageType.REFRESH_RESPONSE);

                ctx.writeAndFlush(getRefreshResponseMessage());
            }
            if (message.getType().equals(MessageType.DELETE_FILE)) {
                System.out.println(MessageType.DELETE_FILE);

                Files.delete(file);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private Message getRefreshResponseMessage() throws IOException {
        Message refreshMessage = new Message(MessageType.REFRESH_RESPONSE);
        refreshMessage.setListFileNames(Files.list(Paths.get(Info.SERVER_FOLDER_NAME + login))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList()));
        return refreshMessage;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendSmallFile(ChannelHandlerContext ctx, Path path) throws IOException {
        Message fileMessage = new Message(MessageType.FILE, path);
        ctx.writeAndFlush(fileMessage);
    }
}
