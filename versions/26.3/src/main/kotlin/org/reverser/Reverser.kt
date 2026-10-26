package org.reverser

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.reverser.commands.GetNoiseCommand
import org.reverser.commands.IsPeriodicCommand

class Reverser : ModInitializer {

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            GetNoiseCommand.register(dispatcher)
            IsPeriodicCommand.register(dispatcher)
        }

        println("Reverser is loaded; commands /getnoise and /isperiodic registered.")
    }
}
