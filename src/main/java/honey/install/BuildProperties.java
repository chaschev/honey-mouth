package honey.install;

import java.util.Date;
import java.util.Properties;

public class BuildProperties {
  public final String artifact;
  public final String name;
  public final String version;
  public final String revision;
  public final Date buildTime;
  public final String timestamp;

  public BuildProperties(Class<?> aClass) {
    final Properties properties = StupidJavaResources.readResourceProperties(aClass, "/build.properties");

     artifact = properties.getProperty("artifact");
     name = properties.getProperty("name");
     version = properties.getProperty("version");
     revision = properties.getProperty("revision");
     buildTime = new Date(Long.parseLong(properties.getProperty("buildTimeMillis")));
     timestamp = properties.getProperty("timestamp");
  }

  @Override
  public String toString() {
    return name + " " + version + " (" + timestamp + ") rev. " + revision;
  }
}
