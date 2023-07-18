#ifndef API_CRYPTO_CUSTOM_FRAME_DECRYPTOR_INTERFACE_H_
#define API_CRYPTO_CUSTOM_FRAME_DECRYPTOR_INTERFACE_H_

#include <cstddef>
#include <string>

namespace webrtc {
    class CustomFrameDecryptorInterface {
    public:
        CustomFrameDecryptorInterface(std::string mediastream_id) {
            int len = strlen(mediastream_id.c_str()) + 1;
            mediastream_id_ = new char[len];
            strncpy(mediastream_id_, mediastream_id.c_str(), len);
        }

        virtual ~CustomFrameDecryptorInterface() {
            delete [] mediastream_id_;
        }

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
                            int mediatype) = 0;

        /**
         *计算解密后数据的长度
         @param frame_size　密文大小
         @param  mediastream_id 当前音频或视频流的名称
         @param  mediatype 媒体类型, 0为"audio" 1为"video"
         @return size_t 明文长度
         **/
        virtual size_t GetMaxPlaintextByteSize(size_t frame_size, const char* mediastream_id,
                                               int media_type) = 0;

        /**
         *返回当前音频或视频流的名称。用于内部调用，客户无需修改。
         @return const char* 当前音频或视频流的名称
         **/
        virtual const char* GetMediaStreamId() const {
            return mediastream_id_;
        }

    private:
        char* mediastream_id_;
    };
}

#endif
