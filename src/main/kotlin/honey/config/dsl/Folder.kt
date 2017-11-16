package honey.config.dsl

data class Folder(
  val path: String,
  val owner: User = User.omit,
  val rights: Rights = Rights.omit
)