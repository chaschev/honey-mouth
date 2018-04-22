package fast.dsl

class GlobalSessionRuntimeContext {

}

class SessionRuntimeContext {

}

data class TaskResult(
  val ok: Boolean = true,
  val modified: Boolean = true,
  val stdout: String = "",
  val stderr: String? = null,
  val code: Int = 0,
  val comment: String? = null
) {

}

open class Task(name: String, desc: String? = null) {
  val before = TaskSet()
  val after = TaskSet()

  open internal fun run(context: TaskContext) : TaskResult {
    TODO()
  }

  /*

  TODO: running a task
   thisNode.session = ..., context=... , startMs =
   add a named task to task tree, set tree.currentRunNode = thisNode
   run
    save & log output
    get task result, record time
    (lazy evaluation) apply task result to
     session's global result
     parent task result
*/
  fun run(): TaskResult {
    return run(null!!)
  }
}

open class LambdaTask(name: String, val block: (TaskContext) -> TaskResult): Task(name) {
  override fun run(context: TaskContext) : TaskResult {

    return block.invoke(context)
  }
}

// TODO: consider - can be a composite task
class TaskSet(
) {
  private val tasks = ArrayList<Task>()

  fun append(task: Task) = tasks.add(task)

  fun insertFirst(task: Task) = tasks.add(0, task)

  fun append(name: String = "", block: TaskContext.() -> TaskResult) {
    tasks.add(LambdaTask(name, block))
  }
}

open class NamedExtTasks {

}

abstract class DeployFastExtension {
  /* Named extension tasks */
  open val tasks: NamedExtTasks = TODO("not implemented")

  /* Has state means extension represents a ONE process run on the host which state can be changed */
  open val hasState = true
  open val hasFacts = false

  open fun isInstalled(): Boolean = TODO()

  // there can be several installations and running instances
  // each extension instance corresponds to ONE such process
  open fun isRunning(): Boolean = TODO()
}

class SystemServiceAppExtension: DeployFastExtension() {
  override val tasks: NamedExtTasks
    get() = TODO()
}


class TaskContext(
  val ctx: SessionRuntimeContext

)

class InfoDSL(
) {
  var name: String = ""
  var author: String = ""
  var description: String = ""
}

class TasksDSL {
//  private val tasks = ArrayList<Task>()
  internal val taskSet = TaskSet()

  fun init(block: () -> Unit)= block.invoke()

  fun task(name:String = "", block: TaskContext.() -> TaskResult): Unit {
    taskSet.append(LambdaTask(name, block))
  }

//  infix fun String.task(block: TaskContext.() -> TaskResult) = task(this, block)
}

/**
 * TODO: rename ext into i.e. extensions,
 */
class DeployFastDSL<EXT : DeployFastExtension>(val ext: EXT) {
  internal var info: InfoDSL? = null

  var tasks: TaskSet? = null
  var beforeTasks: TaskSet? = null
  var afterTasks: TaskSet? = null

  fun autoInstall(): Unit = TODO()

  fun info(block: InfoDSL.() -> Unit): Unit {
    info = InfoDSL().apply(block)
  }

  //TODO: convert to TaskSet DSL Init

  fun tasks(block: TasksDSL.() -> Unit) {
    tasks = TasksDSL().apply(block).taskSet
  }

  fun beforeTasks(block: TasksDSL.() -> Unit) {
    beforeTasks = TasksDSL().apply(block).taskSet
  }

  fun afterTasks(block: TasksDSL.() -> Unit) {
    afterTasks = TasksDSL().apply(block).taskSet
  }

  companion object {
    fun  <EXT : DeployFastExtension> deployFast(ext: EXT, block: DeployFastDSL<EXT>.() -> Unit): DeployFastDSL<EXT> {
      val deployFastDSL = DeployFastDSL<DeployFastExtension>(ext) as DeployFastDSL<EXT>

      deployFastDSL.apply(block)

      return deployFastDSL
    }
  }
}

fun main(args: Array<String>) {
}