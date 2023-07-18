#ifndef ANDROID_WORKSPACE_CRYPTO_CUSTOM_AUDIO_FRAME_ENCRYPTOR_H
#define ANDROID_WORKSPACE_CRYPTO_CUSTOM_AUDIO_FRAME_ENCRYPTOR_H

#include "custom_frame_encryptor_interface.h"

/**
   自定义音频加密类，CustomAudioFrameEncryptor 类名可以修改，但必须继承自CustomFrameEncryptorInterface
   若修改类名，需要同时修改 frame_crypto_jni.cpp 和 CMakeLists.txt 文件内对应名字
   **/
class CustomAudioFrameEncryptor : public webrtc::CustomFrameEncryptorInterface {
public:
    CustomAudioFrameEncryptor(std::string mediastream_id);

    virtual ~CustomAudioFrameEncryptor() {}

    /**
      自定义加密方法
      @param  payload_data 加密前的数据起始地址
      @param  payload_size 加密前的数据大小
      @param  encrypted_frame 加密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
      @param  bytes_written 加密后数据的大小
      @param  mediastream_id 当前音频或视频流的名称
      @param  mediatype 媒体类型, 0为"audio" 1为"video"
      @return  0: 成功,非0: 失败。
      **/
    virtual int Encrypt(const uint8_t *payload_data, size_t payload_size,
                        uint8_t *encrypted_frame, size_t *bytes_written, const char* mediastream_id,
                        int mediatype);

    /**
       *计算加密后数据的长度
       @param frame_size　明文大小
       @param  mediastream_id 当前音频或视频流的名称
       @param  mediatype 媒体类型, 0为"audio" 1为"video"
       @return size_t 密文长度
       **/
    virtual size_t
    GetMaxCiphertextByteSize(size_t frame_size, const char* mediastream_id, int mediatype);
};

#endif //ANDROID_WORKSPACE_CRYPTO_CUSTOM_AUDIO_FRAME_ENCRYPTOR_H
