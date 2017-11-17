package honey.maven;

public interface MavenRepo {
    String root();

    String resolveUrl(String group, String module, String version);

    default String file(String module, String version) {
        return module + "-" + version;
    }

    default String jar(String module, String version) {
        return file(module, version) + "jar";
    }

    default String sha1(String module, String version) {
        return file(module, version) + "jar.sha1";
    }
}

