package mvp.cli

object Commands {

  def showHelp: Unit =
    println(
      """
Usage: [GROUP_NAME] [COMMAND] -[ARGUMENT_1]=[VAL_1] -[ARGUMENT_2]=[VAL_2]

Group name    Command          Argument       Meaning
--------------------------------------------------------------------------------
node          shutdown         None           Shutdown the node
app           help             None           Show all supported commands
      """)

  def nodeShutdown(code: Int = 0): Nothing = sys.exit(code)
}
