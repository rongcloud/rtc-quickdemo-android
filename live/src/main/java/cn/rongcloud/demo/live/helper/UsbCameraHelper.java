package cn.rongcloud.demo.live.helper;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import cn.rongcloud.rtc.api.stream.RCRTCSurfaceTextureHelper;

public class UsbCameraHelper implements RCRTCSurfaceTextureHelper.Sink {

    protected Handler mWorkerHandler;
    String tag = "UsbCameraHelper";
    Context context;
    CameraHelperCallBack cameraHelperCallBack;
    RCRTCSurfaceTextureHelper surfaceTextureHelper;
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Log.d("usb", "usb onAttach");
            if (mUSBMonitor != null) {
                mUSBMonitor.requestPermission(device);
            }

            if (null != cameraHelperCallBack) {
                cameraHelperCallBack.onAttach(device);
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.d("usb", "usb onConnect");
            mUVCCamera = new UVCCamera();
            mUVCCamera.open(ctrlBlock);
            try {
                mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
            } catch (final IllegalArgumentException e) {
                try {
                    // fallback to YUV mode
                    mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                } catch (final IllegalArgumentException e1) {
                    mUVCCamera.destroy();
                    return;
                }
            }

            surfaceTextureHelper.startListening(UsbCameraHelper.this);
            mUVCCamera.setPreviewTexture(surfaceTextureHelper.getSurfaceTexture());
            mUVCCamera.startPreview();
            if (null != cameraHelperCallBack) {
                cameraHelperCallBack.onConnect(device, ctrlBlock, createNew);
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Log.d("usb", "usb onDisconnect");
            mUVCCamera.close();
            mUVCCamera.destroy();
            if (null != cameraHelperCallBack) {
                cameraHelperCallBack.onDisconnect(device, ctrlBlock);
            }

        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.d("usb", "usb onDettach");
            if (null != cameraHelperCallBack) {
                cameraHelperCallBack.onDettach(device);
            }

        }

        @Override
        public void onCancel(final UsbDevice device) {
            Log.d("usb", "usb onCancel");
        }
    };
    private int mWidth = 480;
    private int mHeight = 640;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UsbCameraHelper(Context context, RCRTCSurfaceTextureHelper helper, CameraHelperCallBack callBack) {
        this.context = context;
        cameraHelperCallBack = callBack;
        surfaceTextureHelper = helper;

        mWorkerHandler = surfaceTextureHelper.getHandler();
        surfaceTextureHelper.setTextureSize(mWidth, mHeight);
        mUSBMonitor = new USBMonitor(this.context.getApplicationContext(), mOnDeviceConnectListener);
        mUSBMonitor.register();
        Log.d("usb", "usb onregister  tid " + Thread.currentThread().getId());

    }

    @Override
    public void onTexture(int width, int height, int oesTextureId, float[] transformMatrix,
                          int rotation, long timestamp) {
        if (null != cameraHelperCallBack) {
            cameraHelperCallBack.onTexture(width, height, oesTextureId, transformMatrix, rotation, timestamp, mWorkerHandler);
        }
    }

    public void release() {
        mUSBMonitor.destroy();
    }

    public interface CameraHelperCallBack {
        public void onAttach(final UsbDevice device);

        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew);

        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock);

        public void onDettach(final UsbDevice device);

        public void onTexture(final int textureWidth, final int textureHeight, int oexTextureId, float[] transformMatrix, int rotation, long timestampNs, Handler handler);
    }
}
