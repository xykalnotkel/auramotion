cmake_minimum_required(VERSION 3.22.1)

project("AuraMotionRoot" CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

option(BUILD_TESTS "Build native test suite" ON)

include_directories(
    app/src/main/cpp
    app/src/main/cpp/math
    app/src/main/cpp/animation
    app/src/main/cpp/scene
    app/src/main/cpp/effects
    app/src/main/cpp/renderer
    app/src/main/cpp/export
)

if (BUILD_TESTS)
    enable_testing()
    add_executable(test_engine
        tests/cpp/test_engine.cpp
        app/src/main/cpp/export/VideoExporter.cpp
    )
    add_test(NAME EngineUnitTests COMMAND test_engine)
endif()
