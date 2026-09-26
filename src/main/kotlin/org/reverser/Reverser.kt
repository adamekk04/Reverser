package org.reverser

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.reverser.commands.GetNoiseCommand

class Reverser : ModInitializer {

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            GetNoiseCommand.register(dispatcher)
        }

        println("] Mod is loaded, command /getnoise registered.")
    }
}
