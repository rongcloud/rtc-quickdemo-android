package cn.rongcloud.demo.callplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.callplus.api.RCCallPlusCallRecord;
import cn.rongcloud.callplus.api.RCCallPlusClient;
import cn.rongcloud.callplus.api.RCCallPlusMediaType;
import cn.rongcloud.callplus.api.RCCallPlusMediaTypeChangeResult;
import cn.rongcloud.callplus.api.RCCallPlusReason;
import cn.rongcloud.callplus.api.RCCallPlusSession;
import cn.rongcloud.callplus.api.RCCallPlusType;
import cn.rongcloud.callplus.api.RCCallPlusUserSessionStatus;
import cn.rongcloud.callplus.api.callback.IRCCallPlusEventListener;

public class CallPlusEventListenerImpl implements IRCCallPlusEventListener {

    /**
     * 本端用户通过该回调接收到通话呼叫<br>
     * 该方法回调场景： 1. 本端用户没有正在进行中的音视频通话({@link
     * RCCallPlusClient#getCurrentCallSession()}获取的对象为空)时，远端用户通过{@link
     * RCCallPlusClient#startCall(List, RCCallPlusType, RCCallPlusMediaType)} 、{@link
     * RCCallPlusClient#invite(List)} 函数邀请本端用户进行通话<br>
     * 2.本端用户正在进行音视频通话({@link RCCallPlusClient#getCurrentCallSession()}获取的对象不为空)时，远端用户通过{@link
     * RCCallPlusClient#startCall(List, RCCallPlusType, RCCallPlusMediaType)}、{@link
     * RCCallPlusClient#invite(List)} 函数邀请本端用户进行通话<br>
     * 如果本端用户需要接听该通话，需要先调用{@link RCCallPlusClient#hangup(String)}方法挂断当前正在进行中的通话。再调用{@link
     * RCCallPlusClient#accept(String)}方法接听<br>
     * 判断本端是否正在进行通话，又收到第二通呼叫方法：
     *
     * <pre class="prettyprint">
     *             @Override
     *             public void onReceivedCall(RCCallPlusSession callSession) { <br>
     *                 RCCallPlusSession currentCallSession = RCCallPlusClient.getInstance().getCurrentCallSession(); <br>
     *                 if (currentCallSession != null && !TextUtils.equals(callSession.getCallId(), currentCallSession.getCallId())) { <br>
     *                     //在此处理有正在进行中的通话，又有第二通通话呼入的情况<br>
     *                     //需要挂断 currentCallSession ，再接听新的通话<br>
     *                 } <br>
     *             } <br>
     *  </pre>
     *
     * @param callSession 通话实体信息<br>
     */
    @Override
    public void onReceivedCall(RCCallPlusSession callSession) {

    }
    /**
     * 通话建立成功<br>
     *
     * @param callSession 通话实体信息<br>
     */
    @Override
    public void onCallConnected(RCCallPlusSession callSession) {
        IRCCallPlusEventListener.super.onCallConnected(callSession);
    }
    /**
     * 远端用户被邀请回调<br>
     *
     * @param callId 通话Id<br>
     * @param inviteeUserList 被邀请用户列表<br>
     * @param inviterUserId 邀请人用户Id<br>
     */
    @Override
    public void onRemoteUserInvited(String callId, ArrayList<String> inviteeUserList, String inviterUserId) {
        IRCCallPlusEventListener.super.onRemoteUserInvited(callId, inviteeUserList, inviterUserId);
    }
    /**
     * 远端用户状态改变监听<br>
     *
     * @param callId 通话Id<br>
     * @param userId 用户Id<br>
     * @param status 该用户当前状态<br>
     * @param reason 该用户当前状态原因<br>
     */
    @Override
    public void onRemoteUserStateChanged(String callId, String userId, RCCallPlusUserSessionStatus status, RCCallPlusReason reason) {
        IRCCallPlusEventListener.super.onRemoteUserStateChanged(callId, userId, status, reason);
    }
    /**
     * 己方参与过的通话结束时，收到该通话结束通知。如果本端正在进行中的通话结束后，{@link RCCallPlusClient#getCurrentCallSession()} 会为空
     * <br>
     *
     * @param session 结束通话的实体对象<br>
     * @param reason 通话结束原因<br>
     */
    @Override
    public void onCallEnded(RCCallPlusSession session, RCCallPlusReason reason) {
        IRCCallPlusEventListener.super.onCallEnded(session, reason);
    }
    /**
     * 己方参与过的通话结束时，收到该通话的通话记录<br>
     *
     * @param record 通话记录对象<br>
     */
    @Override
    public void onReceivedCallRecord(RCCallPlusCallRecord record) {
        IRCCallPlusEventListener.super.onReceivedCallRecord(record);
    }
    /**
     * 远端用户麦克风状态改变监听<br>
     *
     * @param callId 通话Id<br>
     * @param userId 用户Id<br>
     * @param disabled 麦克风是否可用，true:麦克风为关闭状态。false：麦克风为开启状态。<br>
     */
    @Override
    public void onRemoteMicrophoneStateChanged(String callId, String userId, boolean disabled) {
        IRCCallPlusEventListener.super.onRemoteMicrophoneStateChanged(callId, userId, disabled);
    }
    /**
     * 远端用户摄像头状态改变监听<br>
     *
     * @param callId 通话Id<br>
     * @param userId 用户Id<br>
     * @param disabled 摄像头是否可用，true:摄像头为关闭状态。false：摄像头为开启状态。<br>
     */
    @Override
    public void onRemoteCameraStateChanged(String callId, String userId, boolean disabled) {
        IRCCallPlusEventListener.super.onRemoteCameraStateChanged(callId, userId, disabled);
    }
    /**
     * 远端用户调用请求切换媒体 {@link RCCallPlusClient#requestChangeMediaType(RCCallPlusMediaType)} 成功后收到 <br>
     *
     * @param transactionId 事务id<br>
     * @param userId 发起人id<br>
     * @param mediaType 媒体类型<br>
     */
    @Override
    public void onReceivedChangeMediaTypeRequest(String transactionId, String userId, RCCallPlusMediaType mediaType) {
        IRCCallPlusEventListener.super.onReceivedChangeMediaTypeRequest(transactionId, userId, mediaType);
    }
    /**
     * 通话媒体类型应答回调<br>
     *
     * @param transactionId 事务id<br>
     * @param userId 发起人id<br>
     * @param mediaType 媒体类型<br>
     * @param result 媒体类型切换结果<br>
     */
    @Override
    public void onReceivedChangeMediaTypeResult(String transactionId, String userId, RCCallPlusMediaType mediaType, RCCallPlusMediaTypeChangeResult result) {
        IRCCallPlusEventListener.super.onReceivedChangeMediaTypeResult(transactionId, userId, mediaType, result);
    }
    /**
     * 单人通话时({@link RCCallPlusType#PRIVATE})，调用{@link
     * cn.rongcloud.callplus.api.RCCallPlusClient#invite(List)} 邀请其他人员成功，当前通话升级为群通话({@link
     * RCCallPlusType#MULTI})<br>
     */
    @Override
    public void onCallTypeChanged(String callId, RCCallPlusType callType) {
        IRCCallPlusEventListener.super.onCallTypeChanged(callId, callType);
    } /**
     * 接收到本端摄像头流首帧回调<br>
     *
     * @param width 视频宽<br>
     * @param height 视频高<br>
     */

    @Override
    public void onFirstLocalVideoFrame(int width, int height) {
        IRCCallPlusEventListener.super.onFirstLocalVideoFrame(width, height);
    }
    /**
     * 接收到远端音频流首帧回调<br>
     *
     * @param userId 用户Id<br>
     */
    @Override
    public void onFirstRemoteAudioFrame(String userId) {
        IRCCallPlusEventListener.super.onFirstRemoteAudioFrame(userId);
    }
    /**
     * 接收到远端视频流首帧回调<br>
     *
     * @param userId 用户Id<br>
     * @param width 视频宽<br>
     * @param height 视频高<br>
     */
    @Override
    public void onFirstRemoteVideoFrame(String userId, int width, int height) {
        IRCCallPlusEventListener.super.onFirstRemoteVideoFrame(userId, width, height);
    }
    /**
     * 用户音量改变回调，每秒回调一次<br>
     *
     * @param hashMap key:用户Id value:音量值大小（取值：0-9）<br>
     */
    @Override
    public void onUserAudioLevelChanged(HashMap<String, Integer> hashMap) {
        IRCCallPlusEventListener.super.onUserAudioLevelChanged(hashMap);
    }
    /**
     * 通话开始时间戳(当前时间到1970-1-1 00:00:00的总毫秒数)<br>
     *
     * @param callStartTime 已经做了时区校准，可以直接使用 (System.currentTimeMillis() - callStartTime) / 1000;
     *     计算出和服务器相同的通话时长(单位秒)<br>
     */
    @Override
    public void onCallStartTimeFromServer(long callStartTime) {
        IRCCallPlusEventListener.super.onCallStartTimeFromServer(callStartTime);
    }
    /**
     * 客户端收到首帧时间戳(当前时间到1970-1-1 00:00:00的总毫秒数)<br>
     *
     * @param callFirstFrameTime 已经做了时区校准，可以直接使用 (System.currentTimeMillis() - callFirstFrameTime) /
     *     1000; 计算出和服务器相同的通话时长(单位秒)<br>
     */
    @Override
    public void onCallFirstFrameTimeFromServer(long callFirstFrameTime) {
        IRCCallPlusEventListener.super.onCallFirstFrameTimeFromServer(callFirstFrameTime);
    }
}
