package honey.maven;

import java.io.File;

import static honey.maven.JavaArtifactResolver.extractSha1;
import static honey.maven.StupidJavaMethods.downloadJavaWay;

public interface MavenRepo {
  String root();

  String resolveUrl(String group, String module, String version);

  String metadataUrl(String group, String module);

  default String file(String module, String version) {
    return module + "-" + version;
  }

  default String jar(String module, String version) {
    return file(module, version) + "jar";
  }

  default String sha1(String module, String version) {
    return file(module, version) + "jar.sha1";
  }

  String[] auth();

  default boolean isAuthEnabled() {return auth() != null;}

  default String downloadSha1(String baseUrl) {
    final String url = baseUrl + ".jar.sha1";

    String s;

    if(isAuthEnabled()) {
      s = StupidJavaMethods.downloadAsString(url);
    } else {
      s = StupidJavaMethods.downloadAsString(url, auth(), "POST");
    }

    return extractSha1(s);
  }

  default void downloadJar(String baseUrl, File dest) {
    final String url = baseUrl + ".jar";

    if(isAuthEnabled()) {
      downloadJavaWay(url, dest);
    } else {
      StupidJavaMethods.downloadFileWithAuth(url, auth(), dest, "POST");
    }
  }
}

