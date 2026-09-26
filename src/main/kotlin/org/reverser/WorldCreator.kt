package org.reverser

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings
import net.minecraft.world.level.levelgen.NoiseRouter
import net.minecraft.world.level.levelgen.RandomState

/**
 * Creates new world (genContext) for a given seed/dimension, without real ServerLevel.
 */
class WorldCreator private constructor(
    val seed: Long,
    val dimensionKey: ResourceKey<Level>,
    val generator: NoiseBasedChunkGenerator,
    val settings: NoiseGeneratorSettings,
    val randomState: RandomState
) {
    val router: NoiseRouter get() = settings.noiseRouter()

    companion object {
        /**
         * Returns null, if dimension doesn't exist or if generator is not noise-based.
         */
        fun create(server: MinecraftServer, dimensionKey: ResourceKey<Level>, seed: Long): WorldCreator? {
            val registryAccess = server.registryAccess()

            val stemKey = ResourceKey.create(Registries.LEVEL_STEM, dimensionKey.identifier())
            val stemHolder = registryAccess.lookupOrThrow(Registries.LEVEL_STEM).get(stemKey)
            if (stemHolder.isEmpty) {
                println("Dimension ${dimensionKey.identifier()} doesn't have LevelStem in registry")
                return null
            }

            val generator = stemHolder.get().value().generator()
            val noiseBasedGenerator = generator as? NoiseBasedChunkGenerator
            if (noiseBasedGenerator == null) {
                println("Generator for ${dimensionKey.identifier()} isn't NoiseBasedChunkGenerator.")
                return null
            }

            val noiseSettingsHolder = noiseBasedGenerator.generatorSettings()
            val settings = noiseSettingsHolder.value()
            val noiseRegistry = registryAccess.lookupOrThrow(Registries.NOISE)
            val randomState = RandomState.create(noiseRegistry, seed, settings)

            return WorldCreator(seed, dimensionKey, noiseBasedGenerator, settings, randomState)
        }
    }
}