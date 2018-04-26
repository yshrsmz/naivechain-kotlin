package net.yslibrary.nativechain

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Block(
    val index: Long,
    val previousHash: String,
    val timestamp: Long,
    val data: String,
    val hash: String
)

val GenesisBlock = Block(
    index = 0,
    previousHash = "0",
    timestamp = 1465154705,
    data = "Genesis Block",
    hash = "816534932c2b7154836da6afc367695e6337db8a921823784c14378abed4f7d7"
)