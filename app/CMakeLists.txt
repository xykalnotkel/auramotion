cmake_minimum_required(VERSION 3.22.1)

project("auramotion" CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Optimization flags
add_compile_options(-O3 -ffast-math -Wall -Wextra -Wno-unused-parameter)

include_directories(
    src/main/cpp
    src/main/cpp/math
    src/main/cpp/animation
    src/main/cpp/scene
    src/main/cpp/effects
    src/main/cpp/renderer
    src/main/cpp/export
    src/main/cpp/jni
)

file(GLOB_RECURSE ENGINE_SOURCES
    "src/main/cpp/*.cpp"
    "src/main/cpp/*.hpp"
)

add_library(auramotion SHARED
    ${ENGINE_SOURCES}
)

# Android specific libraries
if (ANDROID)
    find_library(log-lib log)
    find_library(android-lib android)
    find_library(gles3-lib GLESv3)
    find_library(egl-lib EGL)
    find_library(jnigraphics-lib jnigraphics)

    target_link_libraries(auramotion
        ${log-lib}
        ${android-lib}
        ${gles3-lib}
        ${egl-lib}
        ${jnigraphics-lib}
    )
endif()
