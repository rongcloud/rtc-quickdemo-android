package cn.rongcloud.demo.callplus;

import cn.rongcloud.callplus.api.RCCallPlusCode;
import cn.rongcloud.callplus.api.RCCallPlusSession;
import cn.rongcloud.callplus.api.RCCallPlusUser;
import cn.rongcloud.callplus.api.callback.IRCCallPlusResultListener;

import java.util.List;

/**
 * Created by RongCloud on 2023/6/14.
 */
public class CallPlusResultListenerImpl implements IRCCallPlusResultListener {

    /**
     * 发起通话方法结果回调<br>
     *
     * @param code     方法请求结果<br>
     * @param callId   通话Id<br>
     * @param userList 呼叫成功后，返回被邀请人列表中的忙线用户列表<br>
     */
    @Override
    public void onStartCall(RCCallPlusCode code, String callId, List<RCCallPlusUser> userList) {
        IRCCallPlusResultListener.super.onStartCall(code, callId, userList);
    }

    /**
     * 邀请用户加入通话结果回调<br>
     *
     * @param code     方法请求结果<br>
     * @param userIds  被邀请的用户列表<br>
     * @param userList 被邀请列表中忙线的用户列表<br>
     */
    @Override
    public void onInvite(RCCallPlusCode code, String callId, List<String> userIds, List<RCCallPlusUser> userList) {
        IRCCallPlusResultListener.super.onInvite(code, callId, userIds, userList);
    }

    /**
     * 接听通话结果回调<br>
     *
     * @param code   方法请求结果<br>
     * @param callId 通话Id<br>
     */
    @Override
    public void onAccept(RCCallPlusCode code, String callId) {
        IRCCallPlusResultListener.super.onAccept(code, callId);
    }

    /**
     * 挂断指定通话结果回调<br>
     *
     * @param code   方法请求结果<br>
     * @param callId 通话Id<br>
     */
    @Override
    public void onHangup(RCCallPlusCode code, String callId) {
        IRCCallPlusResultListener.super.onHangup(code, callId);
    }

    /**
     * 开启摄像头数据采集方法结果回调<br>
     *
     * @param code   方法请求结果<br>
     * @param mirror 当前摄像头采集是否镜像<br>
     */
    @Override
    public void onStartCamera(RCCallPlusCode code, boolean mirror) {
        IRCCallPlusResultListener.super.onStartCamera(code, mirror);
    }

    /**
     * 开启摄像头数据采集方法结果回调<br>
     *
     * @param code     方法请求结果<br>
     * @param cameraId 摄像头Id<br>
     * @param mirror   当前摄像头采集是否镜像<br>
     */
    @Override
    public void onStartCamera(RCCallPlusCode code, int cameraId, boolean mirror) {
        IRCCallPlusResultListener.super.onStartCamera(code, cameraId, mirror);
    }

    /**
     * 关闭摄像头方法结果回调<br>
     *
     * @param code 方法请求结果<br>
     */
    @Override
    public void onStopCamera(RCCallPlusCode code) {
        IRCCallPlusResultListener.super.onStopCamera(code);
    }

    /**
     * 切换前后摄像头方法结果回调<br>
     *
     * @param code          方法请求结果<br>
     * @param isFrontCamera 当前开启的摄像头是否是前置摄像头<br>
     */
    @Override
    public void onSwitchCamera(RCCallPlusCode code, boolean isFrontCamera) {
        IRCCallPlusResultListener.super.onSwitchCamera(code, isFrontCamera);
    }

    /**
     * 切换前后摄像头方法结果回调<br>
     *
     * @param code          方法请求结果<br>
     * @param cameraId      摄像头Id<br>
     * @param mirror        当前摄像头采集是否镜像<br>
     * @param isFrontCamera 当前开启的摄像头是否是前置摄像头<br>
     */
    @Override
    public void onSwitchCamera(RCCallPlusCode code, int cameraId, boolean mirror, boolean isFrontCamera) {
        IRCCallPlusResultListener.super.onSwitchCamera(code, cameraId, mirror, isFrontCamera);
    }

    /**
     * 批量删除通话记录方法结果回调<br>
     *
     * @param code    方法请求结果<br>
     * @param callIds 需要删除通话记录的通话Id列表<br>
     */
    @Override
    public void onDeleteCallRecordsFromServer(RCCallPlusCode code, List<String> callIds) {
        IRCCallPlusResultListener.super.onDeleteCallRecordsFromServer(code, callIds);
    }

    /**
     * 清除当前用户通话记录方法结果回调<br>
     *
     * @param code 方法请求结果<br>
     */
    @Override
    public void onDeleteAllCallRecordsFromServer(RCCallPlusCode code) {
        IRCCallPlusResultListener.super.onDeleteAllCallRecordsFromServer(code);
    }

    /**
     * 响应通话中的切换媒体类型方法结果回调<br>
     *
     * @param code          方法请求结果<br>
     * @param callId        通话Id<br>
     * @param transactionId 事务Id<br>
     */
    @Override
    public void onReplyChangeMediaType(RCCallPlusCode code, String callId, String transactionId, boolean isAgreed) {
        IRCCallPlusResultListener.super.onReplyChangeMediaType(code, callId, transactionId, isAgreed);
    }

    /**
     * 取消已经发起的切换媒体类型方法结果回调<br>
     *
     * @param code          方法请求结果<br>
     * @param callId        通话Id<br>
     * @param transactionId 事务Id<br>
     */
    @Override
    public void onCancelChangeMediaType(RCCallPlusCode code, String callId, String transactionId) {
        IRCCallPlusResultListener.super.onCancelChangeMediaType(code, callId, transactionId);
    }
}
