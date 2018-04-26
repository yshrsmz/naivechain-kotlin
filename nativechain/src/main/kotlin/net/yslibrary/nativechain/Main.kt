package net.yslibrary.nativechain

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.yslibrary.nativechain.util.Clock
import net.yslibrary.nativechain.util.NativechainJsonAdapterFactory
import net.yslibrary.nativechain.websocket.Peer
import net.yslibrary.nativechain.websocket.WebSocketServer
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("WebSocketServer")

    val httpPort = args.getOrNull(0)?.toInt() ?: run {
        logger.error("http port is required")
        return
    }
    val webSocketPort = args.getOrNull(1)?.toInt() ?: run {
        logger.error("websocket port is required")
        return
    }
    val peers = args.getOrNull(2)?.let {
        listOf(Peer(it))
    } ?: emptyList()

    val clock = Clock.getDefault()
    val moshi = Moshi.Builder().add(NativechainJsonAdapterFactory.INSTANCE).build()
    val nativechain = Nativechain(clock)

    val peerAdapter = moshi.adapter(Peer::class.java)
    val blockDataAdapter = moshi.adapter(BlockData::class.java)

    val websocketServer = WebSocketServer(nativechain, moshi)

    websocketServer.connectToPeers(peers)

    websocketServer.startP2PServer(webSocketPort)

    embeddedServer(Netty, port = httpPort) {
        routing {
            get("/blocks") {
                val adapter = moshi.adapter<List<Block>>(Types.newParameterizedType(MutableList::class.java, Block::class.java))
                val blockchain = adapter.toJson(nativechain.blockchain)
                call.respondText(blockchain, ContentType.Application.Json)
            }

            post("/mine") {
                val data = call.receiveText()
                val blockData = blockDataAdapter.fromJson(data)
                if (blockData != null) {
                    nativechain.addBlock(nativechain.generateNextBlock(blockData.data))
                    websocketServer.broadcastLatestMessage()
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/peers") {
                logger.debug("connected peers: ${websocketServer.sockets()}")
                call.respondText(websocketServer.sockets().joinToString("\n") { it.first.host })
            }

            post("/peers") {
                val data = call.receiveText()
                val peer = peerAdapter.fromJson(data)
                logger.info("peer request received: $peer")

                if (peer != null) {
                    websocketServer.connectToPeers(listOf(peer))
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

            get("/") {
                call.respondText("Hello, World", ContentType.Text.Plain)
            }
        }
    }.start(wait = true)
}