package plugin

import arc.util.Log
import arc.util.Reflect
import arc.util.serialization.Jval
import mindustry.Vars
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.net.Administration.Config
import plugin.commands.handlers.ChatListener

object Foo {
    private val sb = StringBuilder()
    private val version by lazy { Vars.mods.getMod("serverlugin").meta.version }
    private val transmissions = Config(
        "fooForwardTransmissions",
        "Whether client transmissions (chat, dms, and more) are relayed through the server",
        true
    ) { enableTransmissions() }
    private val commands =
        Config("fooCommandList", "Whether Foo's users are sent the command list on join (for autocomplete)", true)

    /** Called after command creation */
    fun init() {
        Log.info("Initialized Foo's integration v$version")

        /** @since v1 Plugin presence check */
        Vars.netServer.addPacketHandler("fooCheck") { player, _ ->
            Call.clientPacketReliable(player.con, "fooCheck", version)
            enableTransmissions(player)
            sendCommands(player)
        }

        /** @since v1 Client transmission forwarding */
        Vars.netServer.addPacketHandler("fooTransmission") { player, content ->
            if (!transmissions.bool()) return@addPacketHandler
            sb.append(player.id).append(" ").append(content)
            Call.clientPacketReliable("fooTransmission", sb.toString())
            sb.setLength(0) // Reset the builder
        }
    }

    /** @since v2 Informs clients of the transmission forwarding state. When [player] is null, the status is sent to everyone */
    private fun enableTransmissions(player: Player? = null) {
        val enabled = transmissions.bool().toString()
        if (player != null) Call.clientPacketReliable(player.con, "fooTransmissionEnabled", enabled)
        else Call.clientPacketReliable("fooTransmissionEnabled", enabled)
    }

    /** @since v2 Sends the list of commands to a player */
    private fun sendCommands(player: Player) {
        if (!commands.bool()) return
        with(Jval.newObject()) {
            add("prefix", Reflect.get<String>(Vars.netServer.clientCommands, "prefix"))
            add("commands", Jval.newObject().apply {
                Vars.netServer.clientCommands.commandList.forEach {
                    add(it.text, it.paramText)
                }
                for (commandname in ChatListener.getCOMMANDS().keys) {
                    add(commandname)
                }
            })
            Call.clientPacketReliable(player.con, "commandList", this.toString())
        }
    }
}