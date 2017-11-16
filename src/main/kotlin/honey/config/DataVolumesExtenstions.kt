package honey.config

  fun Int.bytes() = DataVolume.MB(this.toLong())
  fun Int.kB() = DataVolume.MB(this.toLong())
  fun Int.MB() = DataVolume.MB(this.toLong())
  fun Int.GB() = DataVolume.GB(this.toLong())