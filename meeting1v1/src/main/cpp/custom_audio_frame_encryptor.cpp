#include <jni.h>

#include "custom_audio_frame_encryptor.h"
#include "frame_crypto_jni.h"

CustomAudioFrameEncryptor::CustomAudioFrameEncryptor(std::string mediastreamId)
        : CustomFrameEncryptorInterface(mediastreamId) {
}

int CustomAudioFrameEncryptor::Encrypt(const uint8_t *payload_data, size_t payload_size,
                                       uint8_t *encrypted_frame, size_t *bytes_written,
                                       std::string mediastream_id, int mediatype) {
    //在此处实现自己的音频加密算法，示例为按位取反
    uint8_t fake_key_ = 0xff;
    for (size_t i = 0; i < payload_size; i++) {
        encrypted_frame[i] = payload_data[i] ^ fake_key_;
    }
    *bytes_written = payload_size;
    return 0;
}

size_t
CustomAudioFrameEncryptor::GetMaxCiphertextByteSize(size_t frame_size, std::string mediastream_id,
                                                    int mediatype) {
    //加密之后数据帧大小
    return frame_size;
}
