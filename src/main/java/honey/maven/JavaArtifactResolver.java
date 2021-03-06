package honey.maven;

import honey.install.MavenMetadata;
import honey.install.MavenMetadataParser;
import honey.install.MavenMetadataResolver;
import honey.install.StupidJavaResources;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class JavaArtifactResolver implements MavenMetadataResolver{
  protected List<MavenRepo> repos;
  protected boolean forceUpdate = false;

  public JavaArtifactResolver(List<MavenRepo> repos) {
    this.repos = repos;
  }

  public Map<String, File> resolveAll(File cacheFolder, List<String> arts) {
    Map<String, File> map = new HashMap<>();

    for (String art : arts) {
      final ResolveResult r = resolve(art, cacheFolder);

      if (r == null) {
        throw new RuntimeException("couldn't resolve " + art);
      }

      map.put(art, r.jarFile);
    }

    return map;
  }

  public JavaArtifactResolver setForceUpdate(boolean forceUpdate) {
    this.forceUpdate = forceUpdate;
    return this;
  }


  @Nullable
  public ResolveResult resolve(String art, File cacheFolder) {
    String group, module, version;

    {
      String[] r = art.split(":");
      group = r[0];
      module = r[1];
      version = r[2];
    }

    for (MavenRepo repo : repos) {
      String file = repo.file(module, version);
      String url = repo.resolveUrl(group, module, version);

      File jarFile = new File(cacheFolder, file + ".jar");
      File sha1File = new File(cacheFolder, file + ".jar.sha1");


      if(forceUpdate) {
        try {
          String sha1 = repo.downloadSha1(url);
          StupidJavaMethods.writeFile(sha1File, sha1);
        } catch (Exception e) {
          continue;
        }
      }

      ResolveResult result = new ResolveResult(jarFile, sha1File);

      ResolveResult isCached = isCached(sha1File, jarFile, file, result);

      if (isCached != null) return isCached;

      String sha1;

      try {
        sha1 = repo.downloadSha1(url);
      } catch (Exception e) {
        sha1 = null;
      }

      if(sha1 == null) continue;

      try {
        StupidJavaMethods.writeFile(sha1File, sha1);

        System.out.printf("GET %s.jar... ", url);

        repo.downloadJar(url, jarFile);

        String actualSha1 = StupidJavaMethods.getSha1(jarFile.toPath());

        if (!Objects.equals(sha1, actualSha1)) {
          System.out.println("downloaded a file, and sha1 didn't match: " + actualSha1 + " (actual) vs " + sha1 + " (expected)");
          //sha1 file can be corrupt (404)
          continue;
        }

        System.out.println("ok");

        return result;
      } catch (Exception e) {
        throw new RuntimeException("can't download url " + url + ".jar for artifact " + art);
      }
    }

    return null;
  }

  public static String extractSha1(String line) {
    line = line.trim();

    int indexOfSpace = line.indexOf(" ");

    String sha1 = indexOfSpace == -1 ? line : line.substring(0, indexOfSpace);

    validateSha1(sha1);

    return sha1;
  }


  private static void validateSha1(String sha1) {
    if(sha1.length() > 100 || sha1.length() < 16 ||
      !sha1.matches("^[0-9a-f]{16,100}$")) throw new RuntimeException("something is not ok about sha1");
  }

  private static ResolveResult isCached(File sha1File, File jarFile, String file, ResolveResult result) {
    ResolveResult cached;

    if (sha1File.exists() && jarFile.exists()) {
      String sha1 = extractSha1(StupidJavaMethods.readFile(sha1File.toPath()));
      String actualSha1 = StupidJavaMethods.getSha1(jarFile.toPath());

      if (!sha1.equals(actualSha1)) {
        System.out.printf("downloaded a file, and sha1 didn't match: %s (actual) vs %s (expected)%n", actualSha1, sha1);

        cached = null;
      } else {
        System.out.println("cached: " + file);

        cached = result;
      }
    } else {
      cached = null;
    }

    return cached;
  }

  public void install() {
    File libDir = new File("lib");

    libDir.mkdir();

    System.out.println("downloading runtime libraries...");
  }

  @Override
  public MavenMetadata resolveMetadata(String art) {
    String group, module;

    {
      String[] r = art.split(":");
      group = r[0];
      module = r[1];
    }

    for (MavenRepo repo : repos) {
      try {
        String xml =  StupidJavaMethods.downloadAsString(repo.metadataUrl(group, module));
        return new MavenMetadataParser().parse(xml);
      } catch (Exception e) {
        //ignore
      }
    }

    return null;  }
}
