cmake_minimum_required(VERSION 3.22.1)

project("canbus_native")

add_library(
    canbus_native
    SHARED
    native-calculator.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    canbus_native
    ${log-lib}
)
