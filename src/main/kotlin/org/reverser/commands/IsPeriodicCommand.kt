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
    private const val MIN_REPETITIONS = 3

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

                    val samples = IntArray(MAX_SAMPLES)
                    for (x in 0 until MAX_SAMPLES) {
                        samples[x] = sampler(x, 0, 0).toBits()
                    }

                    val period = findPeriod(samples, MIN_REPETITIONS)
                    if (period != null) {
                        ctx.source.sendSuccess(
                            {
                                Component.literal("Verified period=$period over x=0..${MAX_SAMPLES - 1} " + "(${MAX_SAMPLES / period} complete repetitions).")
                            },
                            false
                        )
                        return@executes 1
                    }

                    ctx.source.sendFailure(
                        Component.literal("No period with at least $MIN_REPETITIONS complete repetitions " + "was found in x=0..${MAX_SAMPLES - 1}.")
                    )
                    return@executes 0
                }
            )
    }

    private fun findPeriod(values: IntArray, minRepetitions: Int): Int? {
        if (values.isEmpty() || minRepetitions < 2) return null

        val prefix = IntArray(values.size)
        for (i in 1 until values.size) {
            var matched = prefix[i - 1]
            while (matched > 0 && values[i] != values[matched]) {
                matched = prefix[matched - 1]
            }
            if (values[i] == values[matched]) {
                matched++
            }
            prefix[i] = matched
        }

        val period = values.size - prefix.last()
        return period.takeIf {
            it < values.size && it.toLong() * minRepetitions <= values.size.toLong()
        }
    }
}
