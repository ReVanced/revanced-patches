@file:JvmName("LoginHookWebServer")
package app.revanced.extension.spotify.misc.fix

import app.revanced.extension.shared.Logger
import io.ktor.serialization.kotlinx.protobuf.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
fun startServer() {
    CoroutineScope(Dispatchers.IO).launch {
        Logger.printInfo { "Starting server on port 4345" }
        embeddedServer(Netty, port = 4345, host = "127.0.0.1") {
            install(ContentNegotiation) {
                protobuf(ProtoBuf {
                    serializersModule = SerializersModule {}
                })
            }
            routing {
                post("/v3/login") {
                    Logger.printInfo { "Got login v3 post" }
                }
                post("/v4/login") {
                    Logger.printInfo { "Got login v3 post" }
                }
            }
        }.start(wait = false)

        Logger.printInfo { "Server started on port 4345" }
    }
}
