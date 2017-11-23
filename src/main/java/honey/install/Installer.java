package honey.install;

import honey.maven.JavaArtifactResolver;
import honey.maven.MavenRepo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

// ok todo MavenMetadata, MavenMetadataParser, MavenMetadataResolver (I)
//todo download the latest/specified version honey:badger:[version-optional]
//todo read resources from the downloaded jar
//todo go on with the old version
//todo make sure this folder is used as an installation folder only, required libraries are copied into the app folder, so we don't have to delete old jars
public class Installer {
  public static final String MAVEN_CENTRAL = "http://central.maven.org/maven2";

  public static final String MY_JAR = "build/libs/honey-mouth-0.0.1-SNAPSHOT.jar";
  private String art;
  private String repo;

  public Installer(String art, String repo) {
    this.art = art;
    this.repo = repo;
  }

  public static void main(String[] args) throws Exception {
    new Installer().install();

    final File myJar = getMyJar(Installer.class, MY_JAR);
    final String classpath = (myJar.isDirectory() ? myJar.getPath() : myJar.getName()) + ":lib/*";

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

  private File destDir = new File(".");

  private ModuleDependencies deps = null;

  public void install() {_install(false);}

  public void update(boolean forceUpdate) {_install(forceUpdate);}

  private void _install(boolean forceUpdate) {
    File libDir = new File(destDir,"lib");

    libDir.mkdirs();

//    May be next time will have his complexity
//    System.out.println("downloading runtime libraries...");
//
//    new JavaArtifactResolver(Arrays.asList(
//      new JavaDumbMavenRepo("http://dl.bintray.com/kotlin/kotlin-eap-1.2"),
//      new JavaDumbMavenRepo(MAVEN_CENTRAL))
//    ).resolveAll(libDir, Arrays.asList(artifacts));

    System.out.println("downloading app libraries...");

    final List<String> depStrings = getDeps().getDependencies(true);
    final List<MavenRepo> repoList = getDeps().getRepos(true);

    System.out.println("resolving " + depStrings + " artifacts in " + repoList.size() +
      " repositories...");

    new JavaArtifactResolver(repoList)
      .setForceUpdate(forceUpdate)
      .resolveAll(libDir, depStrings);
  }

  public static File getMyJar(Class<?> aClass, String fallbackJarPath) {
    try {
      File file = new File(aClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

      if(file.isDirectory()) {
        file = new File(fallbackJarPath);

        if(!file.exists() || file.isDirectory() || !file.getName().endsWith(".jar")) throw new RuntimeException("can't find my jar. you need to specify fallback jar for dev purposes");
      }

      return file;
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

  public synchronized ModuleDependencies getDeps() {
    if(deps == null) {
      deps = new ModuleDependencies(resourcesClass);
    }
    return deps;
  }

  @NotNull
  public Installer setDestDir(File dir) {
    destDir = dir;
    return this;
  }

  public String getVersion() {return getDeps().me.split(":")[2];}
}
