#include "custom_video_frame_decryptor.h"
#include "frame_crypto_jni.h"

CustomVideoFrameDecryptor::CustomVideoFrameDecryptor(std::string mediastream_id)
        : CustomFrameDecryptorInterface(mediastream_id) {
}

size_t CustomVideoFrameDecryptor::GetMaxPlaintextByteSize(size_t frame_size, const char* mediastream_id,
                                                   int mediatype) {
    LOGI("custom_crypto. %s, %d mediastream_id:%s, mediatype:%d", __func__, __LINE__, mediastream_id, mediatype);
    return frame_size;
}

int CustomVideoFrameDecryptor::Decrypt(const uint8_t *encrypted_frame, size_t encrypted_frame_size,
            uint8_t *frame, size_t *bytes_written, const char* mediastream_id, int mediatype) {
    uint8_t fake_key_ = 0xff;
    for (size_t i = 0; i < encrypted_frame_size; i++) {
        frame[i] = encrypted_frame[i] ^ fake_key_;
    }
    *bytes_written = encrypted_frame_size;
    return 0;
}
