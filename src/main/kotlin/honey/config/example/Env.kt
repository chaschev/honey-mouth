package honey.config.example

object Env {
    operator fun get(env: String): String?
        = System.getenv(env)
}