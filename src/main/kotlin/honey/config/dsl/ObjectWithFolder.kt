package honey.config.dsl

interface ObjectWithFolder<T : ObjectWithFolder<T>> {
  var folderPath: String

  fun build(): T
}