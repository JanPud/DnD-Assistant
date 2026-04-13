package com.dndassistant.utilities

class ByteArrayOperations {
}

fun ByteArray.toUInt32(): UInt{
    require(size == 4) {"Requiered 4 bytes, but was $size"}

    return this[0].toUInt() shl 24 and 0xFF000000U or
            (this[1].toUInt() shl 16 and 0xFF0000U) or
            (this[2].toUInt() shl 8 and 0xFF00U) or
            (this[3].toUInt() and 0xFFU)
}