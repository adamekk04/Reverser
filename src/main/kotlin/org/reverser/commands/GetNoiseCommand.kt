package org.reverser.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
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

/**
 * Command:
 *   /getnoise random <dimension> <radius> <type>
 *   /getnoise <seed> <dimension> <radius> <type>
 *
 * Example:
 *   /getnoise random minecraft:overworld 200 continents
 *   /getnoise 123456789 minecraft:overworld 200 final_density
 */
object GetNoiseCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("getnoise")
                .then(
                    Commands.literal("random")
                        .then(radiusAndTypeBranch { ctx, _ -> Random.nextLong() })
                )
                .then(
                    Commands.argument("seed", LongArgumentType.longArg())
                        .then(radiusAndTypeBranch { ctx, _ -> LongArgumentType.getLong(ctx, "seed") })
                )
        )
    }


    /**
     * Common branch "<dimension> <radius> <type>".
     */
    private fun radiusAndTypeBranch(seedExtractor: (CommandContext<CommandSourceStack>, Unit) -> Long): RequiredArgumentBuilder<CommandSourceStack, Identifier> {

        return Commands.argument("dimension", DimensionArgument.dimension())
            .then(
                Commands.argument("radius", IntegerArgumentType.integer(1, 50_000))
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
                                val radius = IntegerArgumentType.getInteger(ctx, "radius")
                                val typeName = StringArgumentType.getString(ctx, "type")
                                val type = NoiseType.fromString(typeName)

                                if (type == null) {
                                    ctx.source.sendFailure(Component.literal("Unknown argument"))
                                    return@executes 0
                                }

                                ctx.source.sendSuccess(
                                    { Component.literal("Collecting noise data (seed=$seed, radius=$radius, type=$type)...") },
                                    false
                                )

                                val server = ctx.source.server
                                val outFile = Noise.collect(server, dimensionKey, seed, radius, type)

                                if (outFile != null) {
                                    ctx.source.sendSuccess(
                                        { Component.literal("Saved to ${outFile.absolutePath}") },
                                        false
                                    )
                                    1
                                } else {
                                    ctx.source.sendFailure(Component.literal("Collection file is null."))
                                    0
                                }
                            }
                    )
            )
    }
}