package honey.config.pack;

import honey.maven.JavaArtifactResolver;
import honey.maven.JavaDumbMavenRepo;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class BitbucketResolveTest {
  @Test
  @Ignore
  public void resolve() {
    new JavaArtifactResolver(
      Arrays.asList(
        new JavaDumbMavenRepo("test.u:test.p@https://bitbucket.org/chaschev/honey-badger/raw/repository")
      )
    ).resolveAll(new File("build"),
      Arrays.asList("honey:honey-badger:0.0.7-SNAPSHOT")
    );
  }
}
