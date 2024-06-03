package com.example.bluetooth.comms
//                                    [     0    header    Length[2]       X     X      ]
const val  RESET_ASCII: Byte = 0x4F //O 79d
const val  ALARM_ASCII: Byte = 0x41 //A 65d
const val  BUZZ_ASCII: Byte =0x42  //B 66d
const val  PUSH_ASCII: Byte =0x50  //P 80d


enum class PACKET(val header: Byte, val command: ByteArray) {

    RESET(RESET_ASCII, byteArrayOf(0x5B, 0x30, RESET_ASCII, 0x30, 0x32, 0x31, 0x31, 0x5D)),
    ALARM(ALARM_ASCII, byteArrayOf(0x5B, 0x30, ALARM_ASCII, 0x30, 0x32, 0x31, 0x31, 0x5D)),
    BUZZ(BUZZ_ASCII, byteArrayOf(0x5B, 0x30, BUZZ_ASCII, 0x30, 0x32, 0x31, 0x31, 0x5D)),
    PUSH(PUSH_ASCII,byteArrayOf(0x5B, 0x30, PUSH_ASCII, 0x30, 0x32, 0x31, 0x31, 0x5D));
}

sealed class RequestType {
    object AlarmType : RequestType()
    object BuzzType: RequestType()
    object PushType:RequestType()
    object Undetermined : RequestType()
}
