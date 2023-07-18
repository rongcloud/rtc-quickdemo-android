#include "custom_audio_frame_decryptor.h"
#include "frame_crypto_jni.h"

CustomAudioFrameDecryptor::CustomAudioFrameDecryptor(std::string mediastreamId)
        : CustomFrameDecryptorInterface(mediastreamId) {
}

int CustomAudioFrameDecryptor::Decrypt(const uint8_t *encrypted_frame, size_t encrypted_frame_size,
                                       uint8_t *frame, size_t *bytes_written,
                                       const char* mediastream_id, int mediatype) {
    //在此处实现自己的解密算法，示例为按位取反
    uint8_t fake_key_ = 0xff;
    for (size_t i = 0; i < encrypted_frame_size; i++) {
        frame[i] = encrypted_frame[i] ^ fake_key_;
    }
    *bytes_written = encrypted_frame_size;
    return 0;
}

size_t CustomAudioFrameDecryptor::GetMaxPlaintextByteSize(size_t frame_size, const char* mediastream_id,
                                                   int mediatype) {
    LOGI("custom_crypto. %s, %d mediastream_id:%s, mediatype:%d", __func__, __LINE__, mediastream_id, mediatype);
    // 解密之后帧大小
    return frame_size;
}
