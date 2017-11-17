package honey.maven;

import java.io.File;
import java.util.ArrayList;

public class JavaInstaller {
    public static void main(String[] args) {
        new JavaInstaller().install();
    }

    public void install() {
        File libDir = new File("lib");

        libDir.mkdir();

        System.out.println("downloading runtime libraries...");

        new JavaQuickArtifactResolver(
                new JavaDumbMavenRepo("http://dl.bintray.com/kotlin/kotlin-eap-1.2"),
                new JavaDumbMavenRepo("http://central.maven.org/maven2")
        ).resolve("jline:jline:2.14.5", libDir
//                "org.jetbrains.kotlin:kotlin-stdlib:1.2.0-rc-39",

        );
    }
}
