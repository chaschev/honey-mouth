package honey.config


interface HostConfig {
  fun getIps(): List<String>
}

interface Hosts {
  fun getAllIps(): Set<String>
  fun getHosts(): List<HostConfig>
}