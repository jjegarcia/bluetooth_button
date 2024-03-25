package com.example.bluetooth.comms
//                                    [     0    header    Length[2]       X     X      ]
enum class PACKET(val header: Byte, val command: ByteArray) {
    RESET(0x4F, byteArrayOf(0x5B, 0x30, 0x4F, 0x30, 0x32, 0x31, 0x31, 0x5D)),
    ALARM(0x41, byteArrayOf(0x5B, 0x30, 0x41, 0x30, 0x32, 0x31, 0x31, 0x5D)),
    BUZZ(0x42, byteArrayOf(0x5B, 0x30, 0x42, 0x30, 0x32, 0x31, 0x31, 0x5D)),
}

sealed class RequestType {
    object AlarmType : RequestType()
    object BuzzType: RequestType()
    object Undetermined : RequestType()
}
