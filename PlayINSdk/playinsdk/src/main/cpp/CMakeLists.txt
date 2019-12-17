
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_BUILD_TYPE "Release")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

add_library(
        playin
        SHARED
        Playin.cpp FFmpeg.cpp)

find_library(
        log-lib
        log)


include_directories(${CMAKE_SOURCE_DIR}/ffmpeg/include)
include_directories(${CMAKE_SOURCE_DIR}/yuv/include)


add_library(
        libyuv
        SHARED
        IMPORTED
)
set_target_properties(
        libyuv
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/yuv/libs/${ANDROID_ABI}/libyuv.so
)

target_link_libraries(
        playin
        -lm -lz -landroid
        libyuv
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavformat.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavcodec.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavutil.a
        ${log-lib}
)
