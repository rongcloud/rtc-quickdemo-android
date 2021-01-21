package cn.rongcloud.common.tools;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public class ChatRoomKit {

    public static void setReceiveMessageListener(RongIMClient.OnReceiveMessageWrapperListener listener){
        RongIMClient.getInstance().setOnReceiveMessageListener(listener);
    }

    /**
     * 加入聊天室。如果聊天室不存在，sdk 会创建聊天室并加入，如果已存在，则直接加入。加入聊天室时，可以选择拉取聊天室消息数目。
     *
     * @param roomId          聊天室 Id
     * @param defMessageCount 默认开始时拉取的历史记录条数
     * @param callback        状态回调
     */
    public static void joinChatRoom(String roomId, int defMessageCount, final RongIMClient.OperationCallback callback) {
        RongIMClient.getInstance().joinChatRoom(roomId, defMessageCount, callback);
    }

    /**
     * 退出聊天室，不在接收其消息。
     */
    public static void quitChatRoom(String roomId,final RongIMClient.OperationCallback callback) {
        RongIMClient.getInstance().quitChatRoom(roomId, callback);
    }

    /**
     * 向当前聊天室发送消息。
     * </p>
     * <strong>注意：</strong>此函数为异步函数，发送结果将通过handler事件返回。
     *
     * @param msgContent 消息对象
     */
    public static void sendMessage(String roomId,final MessageContent msgContent,IRongCallback.ISendMessageCallback callback) {
        sendMessage(Message.obtain(roomId, Conversation.ConversationType.CHATROOM, msgContent),callback);
    }

    public static void sendMessage(Message msg,IRongCallback.ISendMessageCallback callback) {
        RongIMClient.getInstance().sendMessage(msg, null, null, callback);
    }
}
