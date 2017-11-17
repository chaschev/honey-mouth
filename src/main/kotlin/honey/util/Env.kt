package rocket.util

object Env {
    operator fun get(env: String): String?
        = System.getenv(env)
}