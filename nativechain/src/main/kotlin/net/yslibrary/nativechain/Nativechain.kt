package net.yslibrary.nativechain

import net.yslibrary.nativechain.util.Clock
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

typealias Blockchain = MutableList<Block>

class Nativechain(private val clock: Clock) {
    val logger = LoggerFactory.getLogger("Nativechain")

    var blockchain: Blockchain = mutableListOf<Block>(GenesisBlock)

    fun calculateHash(
        index: Long,
        previousHash: String,
        timestamp: Long,
        data: String
    ): String {
        return MessageDigest.getInstance("SHA-256")
            .digest("$index$previousHash$timestamp$data".toByteArray(StandardCharsets.UTF_8))
            .joinToString(separator = "") { "%02X".format(it) }
    }

    fun calculateHashForBlock(block: Block): String = calculateHash(block.index, block.previousHash, block.timestamp, block.data)

    fun getLatestBlock(): Block = blockchain.last()

    fun generateNextBlock(
        blockData: String
    ): Block {
        val previousBlock = getLatestBlock()
        val nextIndex = previousBlock.index + 1
        val timestamp = clock.currentTimeSeconds()
        val nextHash = calculateHash(index = nextIndex, previousHash = previousBlock.hash, timestamp = timestamp, data = blockData)

        return Block(
            index = nextIndex,
            previousHash = previousBlock.hash,
            timestamp = timestamp,
            data = blockData,
            hash = nextHash
        )
    }

    fun isValidNewBlock(newBlock: Block, previousBlock: Block): Boolean {
        return when {
            previousBlock.index + 1 != newBlock.index -> {
                logger.error("invalid index")
                false
            }
            previousBlock.hash != newBlock.previousHash -> {
                logger.error("invalid previous hash")
                false
            }
            calculateHashForBlock(newBlock) != newBlock.hash -> {
                logger.error("invalid hash - expected: ${calculateHashForBlock(newBlock)}, actual: ${newBlock.hash}")
                false
            }
            else -> true
        }
    }

    fun isValidChain(blockchainToValidate: List<Block>): Boolean {
        if (blockchainToValidate.isEmpty()) {
            logger.info("blockchainToValidate is empty")
            return false
        }

        if (blockchainToValidate.first() != GenesisBlock) {
            logger.info("invalid GenesisBlock")
            return false
        }

        blockchainToValidate.zip(blockchainToValidate.drop(1)).forEachIndexed { index, pair ->
            if (!isValidNewBlock(pair.second, pair.first)) {
                logger.debug("Faced invalid block. index=${index + 1}")
                return false
            }
        }
        return true
    }

    fun addBlock(newBlock: Block) {
        if (isValidNewBlock(newBlock, getLatestBlock())) {
            blockchain.add(newBlock)
        }
    }

    fun replaceChain(newBlocks: List<Block>) {
        if (isValidChain(newBlocks) && newBlocks.size > blockchain.size) {
            logger.info("received blockchain is valid. Replacing current blockchain with received one")
            blockchain = newBlocks.toMutableList()
        } else {
            logger.info("Received blockchain is invalid")
        }
    }
}