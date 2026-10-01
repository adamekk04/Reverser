package org.reverser.noise

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.NoiseRouter
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction
import org.reverser.WorldCreator
import org.reverser.enums.NoiseType
import java.io.File
import java.time.Instant

object Noise {

    private const val FIXED_Y = 64

    fun collect(
        server: MinecraftServer,
        dimensionKey: ResourceKey<Level>,
        seed: Long,
        radius: Int,
        type: NoiseType
    ): File? {

        val worldCreator = WorldCreator.create(server, dimensionKey, seed) ?: return null
        val router: NoiseRouter = worldCreator.router

        val function = pickFunction(router, type)

        val outDir = server.serverDirectory
            .resolve("noise_output")
            .toFile()
            .apply { mkdirs() }

        val timestamp = Instant.now().epochSecond

        val outFile = File(
            outDir,
            "noise_${dimensionKey.identifier().path}" +
                    "_${seed}" +
                    "_${type.name.lowercase()}" +
                    "_$timestamp.csv"
        )

        outFile.bufferedWriter().use { writer ->

            writer.write("x,y,z,value\n")

            for (x in -radius..radius) {
                for (z in -radius..radius) {

                    val value =
                        worldCreator.randomState.sampleBlockValueUncached(function, x, FIXED_Y, z)

                    writer.write(
                        "$x,$FIXED_Y,$z,$value\n"
                    )
                }
            }
        }

        println(
            "Done. " +
                    "Seed=$seed " +
                    "dim=${dimensionKey.identifier()} " +
                    "type=$type -> ${outFile.absolutePath}"
        )

        return outFile
    }

    fun createSampler(
        server: MinecraftServer,
        dimensionKey: ResourceKey<Level>,
        seed: Long,
        type: NoiseType
    ): ((Int, Int, Int) -> Float)? {
        val worldCreator = WorldCreator.create(server, dimensionKey, seed) ?: return null
        val function = pickFunction(worldCreator.router, type)

        return { x, y, z ->
            worldCreator.randomState.sampleBlockValueUncached(function, x, y, z)
        }
    }

    private fun pickFunction(
        router: NoiseRouter,
        type: NoiseType
    ): DensityFunction {

        return when (type) {

            NoiseType.CONTINENTS ->
                router.continents()

            NoiseType.EROSION ->
                router.erosion()

            NoiseType.RIDGES ->
                router.ridges()

            NoiseType.DEPTH ->
                router.depth()

            NoiseType.TEMPERATURE ->
                router.temperature()

            NoiseType.VEGETATION ->
                router.vegetation()

            NoiseType.CHUNK_SURFACE_LEVEL ->
                router.chunkSurfaceLevel()

            NoiseType.FINAL_DENSITY ->
                router.finalDensity()
        }
    }
}
