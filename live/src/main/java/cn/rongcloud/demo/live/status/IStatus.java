package cn.rongcloud.demo.live.status;

public interface IStatus {
    void attach();

    void detach();

    void config();

    void joinRoom(String roomId);

    void leaveRoom();

    void publishDefaultAVStream();

    void subscribeAVStream();

    void requestSpeak();

    void downSpeak();

    void changeUi();

    void mixLayout(String str);

    void publishCustomStream(String filePath);

    void unpublishCustomStream();

    void publishUsbCameraStream();

    void unpublishUsbCameraStream();
}
