package ru.grigorev.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.grigorev.common.AuthMessage;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;
import ru.grigorev.server.db.dao.DAO;
import ru.grigorev.server.db.model.User;

import java.util.List;

/**
 * @author Dmitriy Grigorev
 */
public class AuthInHandler extends ChannelInboundHandlerAdapter {
    private boolean isAuthorized;
    private DAO dao;
    private List<User> users;


    public AuthInHandler(DAO dao) {
        this.dao = dao;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client has connected (Auth Handler)");
        users = dao.getAllUsers();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //TODO
            if (msg == null) {
                return;
            }
            if (msg instanceof AuthMessage) {
                AuthMessage message = (AuthMessage) msg;
                if (message.getType().equals(MessageType.SIGN_IN_REQUEST)) {
                    String login = message.getLogin();
                    String password = message.getPassword();
                    User foundUser = null;
                    for (User user : users) {
                        if (user.getLogin().equals(login)) {
                            foundUser = user;
                            break;
                        }
                    }
                    if (foundUser == null)
                        ctx.writeAndFlush(new AuthMessage(MessageType.AUTH_FAIL, "No such user or incorrect login!"));
                    else {
                        if (password.equals(foundUser.getPassword())) {
                            ctx.writeAndFlush(new AuthMessage(MessageType.AUTH_OK, "You have successfully authorized!"));
                            isAuthorized = true;
                        } else ctx.writeAndFlush(new AuthMessage(MessageType.AUTH_FAIL, "Wrong password!"));
                    }
                }
                if (message.getType().equals(MessageType.SIGN_UP_REQUEST)) {
                    // TODO
                }
            }
            if (msg instanceof Message) {
                /*if (isAuthorized) ctx.fireChannelRead(msg);
                else ctx.writeAndFlush(new AuthMessage(MessageType.AUTH_FAIL, "Please sign in or sign up"));*/
                ctx.fireChannelRead(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
