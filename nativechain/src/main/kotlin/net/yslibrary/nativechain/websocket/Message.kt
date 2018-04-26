package net.yslibrary.nativechain.websocket

import net.yslibrary.nativechain.Block
import net.yslibrary.nativechain.Blockchain
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Message(
    val type: Int,
    val block: Block?,
    val blockchain: Blockchain?
) {
    fun messageType() = MessageType.values()[type]
}