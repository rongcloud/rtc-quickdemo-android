cmake_minimum_required(VERSION 3.4.1)

add_library(custom_frame_crypto
        SHARED
        custom_video_frame_decryptor.cpp
        custom_audio_frame_decryptor.cpp
        custom_video_frame_encryptor.cpp
        custom_audio_frame_encryptor.cpp
        frame_crypto_jni.cpp)

include_directories(include)

find_library(log-lib log)

add_definitions("-Wall -g")

add_definitions(-std=c++11)

target_link_libraries(custom_frame_crypto ${log-lib})