package honey.pack

data class SystemdInstall(
  val description: String,
  val exec: String
) {
  fun write(): String {
    return """[Unit]
Description=$description

[Service]

ExecStart=$exec

[Install]
WantedBy=multi-user.target
"""
  }
}

