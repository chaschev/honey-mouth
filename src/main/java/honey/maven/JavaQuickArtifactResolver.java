package honey.maven;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

public class JavaQuickArtifactResolver {
    protected List<MavenRepo> repos;

    public JavaQuickArtifactResolver(MavenRepo... repos) {
        this.repos = Arrays.asList(repos);
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

            ResolveResult result = new ResolveResult(jarFile, sha1File);

            ResolveResult isCached = isCached(sha1File, jarFile, file, result);

            if (isCached != null) return isCached;

            String sha1;

            try {
                downloadJavaWay(url + ".jar.sha1", sha1File);

                String line = readFile(sha1File.toPath()).trim();
                int indexOfSpace = line.indexOf(" ");

                sha1 = indexOfSpace == -1 ? line : line.substring(0, indexOfSpace);
            } catch (Exception e) {
                sha1 = null;
            }

            if (sha1 == null) {
                sha1File.delete();
            } else {
                try {
                    System.out.printf("GET %s.jar... ", url);

                    downloadJavaWay(url + ".jar", jarFile);

                    String actualSha1 = getSha1(jarFile.toPath());

                    if (!Objects.equals(sha1, actualSha1)) {
                        System.out.println("downloaded a file, and sha1 didn't match: " + actualSha1 + " (actual) vs " + sha1 + " (expected)");
                        return null;
                    }

                    System.out.println("ok");

                    return result;
                } catch (Exception e) {
                    throw new RuntimeException("can't download url " + url + ".jar for artifact " + art);
                }
            }
        }

        return null;

    }

    private static ResolveResult isCached(File sha1File, File jarFile, String file, ResolveResult result) {
        ResolveResult cached;

        if (sha1File.exists() && jarFile.exists()) {
            String sha1 = readFile(sha1File.toPath());
            String actualSha1 = getSha1(jarFile.toPath());

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

    @NotNull
    public static String readFile(Path path) {
        try {
            byte[] encoded = Files.readAllBytes(path);

            return new String(encoded);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getSha1(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(Files.readAllBytes(path));

            return byteToHex(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String byteToHex(final byte[] hash) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        }
    }

    void downloadJavaWay(String url, File dest) {
//        System.out.printf("GET %s  ", url);

        URL webUrl = null;

        //oh, stupid java
        try {
            webUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String message = "ok";

        try (ReadableByteChannel rbc = Channels.newChannel(webUrl.openStream());
             FileOutputStream fos = new FileOutputStream(dest)) {

            fos.getChannel().transferFrom(rbc, 0, java.lang.Long.MAX_VALUE);
        } catch (IOException e) {
            message = "error: " + e.toString();

            throw new RuntimeException(e);
        } finally {
//            System.out.println(message);
        }
    }


}
