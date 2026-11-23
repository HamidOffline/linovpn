package app.slipnet.tunnel

data class ResolverConfig(
    val host: String,
    val port: Int = 53,
    val authoritative: Boolean = false
)
