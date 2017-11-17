package honey.maven


interface MavenRepo {
    fun resolveUrl(group: String, module: String, version: String): Pair<String, String>

    fun file(module: String, version: String) = "$module-$version"

    fun jar(module: String, version: String) = "${file(module, version)}.jar"

    fun sha1(module: String, version: String) = "${file(module, version)}.jar.sha1"
}

class DumbMavenRepo(
 val root: String
) : MavenRepo {
    override fun resolveUrl(group: String, module: String, version: String): Pair<String, String> =
        Pair("$root/${group.replace('.', '/')}/$module/$version/${file(module, version)}", file(module, version))

}