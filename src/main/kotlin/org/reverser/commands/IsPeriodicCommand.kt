package org.reverser.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.reverser.enums.NoiseType
import org.reverser.noise.Noise
import kotlin.random.Random

object IsPeriodicCommand {
    private const val MAX_SAMPLES = 1_000_000

    /**
     * Command:
     *   /isperiodic random <dimension> <type>
     *   /isperiodic <seed> <dimension> <type>
     *
     * Example:
     *   /isperiodic random minecraft:overworld continents
     */

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("isperiodic")
                .then(
                    Commands.literal("random")
                        .then(dimensionAndTypeBranch { ctx, _ -> Random.nextLong() })
                )
                .then(
                    Commands.argument("seed", LongArgumentType.longArg())
                        .then(dimensionAndTypeBranch { ctx, _ -> LongArgumentType.getLong(ctx, "seed") })
                )
        )
    }

    private fun dimensionAndTypeBranch(seedExtractor: (CommandContext<CommandSourceStack>, Unit) -> Long): RequiredArgumentBuilder<CommandSourceStack, Identifier> {

        return Commands.argument("dimension", DimensionArgument.dimension())
            .then(
                Commands.argument("type", StringArgumentType.word())
                .suggests { _, builder ->
                    for (type in NoiseType.suggestionList()) {
                        builder.suggest(type)
                    }
                    builder.buildFuture()
                }
                .executes { ctx ->
                    val seed = seedExtractor(ctx, Unit)
                    val dimensionKey = DimensionArgument.getDimension(ctx, "dimension").dimension()
                    val typeName = StringArgumentType.getString(ctx, "type")
                    val type = NoiseType.fromString(typeName)

                    if (type == null) {
                        ctx.source.sendFailure(Component.literal("Unknown argument"))
                        return@executes 0
                    }

                    ctx.source.sendSuccess(
                        { Component.literal("Collecting noise data (seed=$seed, dimension=${dimensionKey}, type=$type)...") },
                        false
                    )

                    val server = ctx.source.server

                    val sampler = Noise.createSampler(server, dimensionKey, seed, type)
                    if (sampler == null) {
                        ctx.source.sendFailure(Component.literal("Unable to create a noise sampler for this dimension."))
                        return@executes 0
                    }

                    val firstOccurrence = HashMap<Float, Int>()
                    for (x in 0 until MAX_SAMPLES) {
                        val value = sampler(x, 0, 0)
                        val previousX = firstOccurrence.putIfAbsent(value, x)

                        if (previousX != null) {
                            val repeatDistance = x - previousX
                            ctx.source.sendSuccess(
                                {
                                    Component.literal(
                                        "Found repeated value at x=$previousX and x=$x " +
                                            "(distance=$repeatDistance)"
                                    )
                                },
                                false
                            )
                            return@executes 1
                        }
                    }

                    ctx.source.sendFailure(
                        Component.literal("No repeated value found in the first $MAX_SAMPLES samples.")
                    )
                    return@executes 0
                }
            )
    }
}
