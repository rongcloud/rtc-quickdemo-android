#ifndef ANDROID_WORKSPACE_CRYPTO_CUSTOM_VIDEO_FRAME_DECRYPTOR_H
#define ANDROID_WORKSPACE_CRYPTO_CUSTOM_VIDEO_FRAME_DECRYPTOR_H

#include "custom_frame_decryptor_interface.h"

/**
   自定义视频解密类，CustomVideoFrameDecryptor 类名可以修改，但是必须继承自CustomFrameDecryptorInterface
   若修改类名，需要同时修改 frame_crypto_jni.cpp 和 CMakeLists.txt  文件内对应名字
   **/
class CustomVideoFrameDecryptor : public webrtc::CustomFrameDecryptorInterface {
public:
    CustomVideoFrameDecryptor(std::string mediastream_id);

    virtual ~CustomVideoFrameDecryptor() {}

    /**
      开发者定义解密方法
      @param  encrypted_frame 解密前的数据起始地址
      @param  encrypted_frame_size 解密前的数据大小
      @param  frame 解密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
      @param  bytes_written 解密后数据的大小
      @param  mediastream_id 当前音频或视频流的名称
      @param  mediatype 媒体类型, 0为"audio" 1为"video"
      @return  0: 成功,非0: 失败。
     **/
    virtual int Decrypt(const uint8_t *encrypted_frame, size_t encrypted_frame_size,
                        uint8_t *frame, size_t *bytes_written, const char* mediastream_id,
                        int mediatype);

    /**
     *计算解密后数据的长度
     @param frame_size　密文大小
     @param  mediastream_id 当前音频或视频流的名称
     @param  mediatype 媒体类型, 0为"audio" 1为"video"
     @return size_t 明文长度
     **/
    virtual size_t
    GetMaxPlaintextByteSize(size_t frame_size, const char* mediastream_id, int mediatype);
};

#endif //ANDROID_WORKSPACE_CRYPTO_CUSTOM_VIDEO_FRAME_DECRYPTOR_H
