package honey.install;

import honey.maven.JavaDumbMavenRepo;
import honey.maven.JavaArtifactResolver;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

public class Installer {

  public static final String MAVEN_CENTRAL = "http://central.maven.org/maven2";

  public static void main(String[] args) throws Exception {
    String[] artifacts = {
      "jline:jline:2.14.5",
      "org.jetbrains.kotlin:kotlin-stdlib:1.2.0-rc-39"
    };

    new Installer().install(artifacts);

    final File mySource = getMyJar(Installer.class);
    final String classpath = (mySource.isDirectory() ? mySource.getPath() : mySource.getName()) + ":lib/*";

    System.out.println("Using classpath: " + classpath);

    String[] command = {
      "java",
      "-cp",
      classpath,
      "honey.install.AppInstaller"
    };

    Process java = new ProcessBuilder(command)
      .inheritIO()
      .start();

    System.out.println("Waiting for child process and leaving...");

    for (int i = 0; i < 60000000; i++) {
      if (!java.isAlive()) {
        if (java.exitValue() != 0) {
          System.out.println("ERROR: child process ended with code " + java.exitValue());
        }

        System.exit(java.exitValue());
      }

      Thread.sleep(100);
    }
  }

  public void install(String[] artifacts) {
    File libDir = new File("lib");

    libDir.mkdir();

    System.out.println("downloading runtime libraries...");

    new JavaArtifactResolver(
      new JavaDumbMavenRepo("http://dl.bintray.com/kotlin/kotlin-eap-1.2"),
      new JavaDumbMavenRepo(MAVEN_CENTRAL)
    ).resolveAll(libDir, artifacts);

    //this
    System.out.println("downloading app libraries...");

    String jarsString = readResource(this.getClass(), "/jars");

    if(jarsString == null) {
      throw new RuntimeException("couldn't read app classpath in /jars");
    }

    int reposStartIndex = jarsString.indexOf("# Repos");
    int artsStartIndex = jarsString.indexOf("# Artifacts");

    String[] reposUrls = jarsString.substring(reposStartIndex + "# Repos".length()).trim().split("\n");
    String[] arts = jarsString.substring(artsStartIndex + "# Artifacts".length()).trim().split("\n");

    JavaDumbMavenRepo[] repos = new JavaDumbMavenRepo[reposUrls.length + 1];

    repos[0] = new JavaDumbMavenRepo(MAVEN_CENTRAL);

    for (int i = 0; i < reposUrls.length; i++) {
      repos[i + 1] = new JavaDumbMavenRepo(reposUrls[i]);
    }

    System.out.println("resolving " + arts.length + " artifacts...");

    new JavaArtifactResolver(repos).resolveAll(libDir, arts);
  }

  public static File getMyJar(Class<?> aClass) {
    try {
      return new File(aClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static String streamToString(InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  public static String readResource(Class<?> aClass, String resourcePath) {
    try {
      final InputStream stream = aClass.getResourceAsStream(resourcePath);

      if(stream == null) return null;

      return streamToString(stream);
    } catch (Exception e) {
      return null;
    }
  }
}
