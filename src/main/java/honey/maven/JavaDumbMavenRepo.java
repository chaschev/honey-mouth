package honey.maven;

public class JavaDumbMavenRepo implements MavenRepo {
    protected final String root;

    public JavaDumbMavenRepo(String root) {
        this.root = root;
    }

    @Override
    public String root() {
        return root;
    }

    @Override
    public String resolveUrl(String group, String module, String version) {
        return root + "/" + group.replace('.', '/') + "/" + module + "/" + version + "/" + file(module, version);
    }
}
