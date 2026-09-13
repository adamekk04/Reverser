package org.reverser;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;

public final class Reverser extends JavaPlugin {

    @Override
    public void onEnable() {
        // Fetching seed
        String seedFromArg = System.getProperty("reverser.seed");
        long seed;
        try {
            seed = Long.parseLong(seedFromArg);
        } catch (NumberFormatException e) {
            getLogger().warning("Invalid seed: " + seedFromArg + ", using ranodm one");
            Random random = new Random();
            seed = random.nextLong();
        }
        getLogger().info("Fetched seed: " + seed);

        // Creating data folder
        getLogger().info(getDataFolder().mkdir() ? "Created data folder." : "Unable to create data folder.");

        // File creation
        File temperature = new File(getDataFolder(), "temperature.txt");
        File humidity = new File(getDataFolder(), "humidity.txt");
        File continentness = new File(getDataFolder(), "continentness.txt");
        File erosion = new File(getDataFolder(), "erosion.txt");
        File weirdness = new File(getDataFolder(), "weirdness.txt");
        File depth = new File(getDataFolder(), "depth.txt");

        try {
            temperature.createNewFile();
            humidity.createNewFile();
            continentness.createNewFile();
            erosion.createNewFile();
            weirdness.createNewFile();
            depth.createNewFile();
            getLogger().info("Created all files.");
        } catch (IOException e) {
            getLogger().severe("Something went wrong while creating files");
        }

        // Creating world by using PaperAPI
        WorldCreator worldCreator = new WorldCreator("reverser");
        worldCreator.seed(seed);
        World world = worldCreator.createWorld();
        getLogger().info("Created world");

        // Fetching temperature, humidity, continentness, erosion, weirdness and depth by accessing NMS
        try (
                FileWriter temperatureFW = new FileWriter(temperature);
                FileWriter humidityFW = new FileWriter(humidity);
                FileWriter continentnessFW = new FileWriter(continentness);
                FileWriter erosionFW = new FileWriter(erosion);
                FileWriter weirdnessFW = new FileWriter(weirdness);
                FileWriter depthFW = new FileWriter(depth)
        ) {
            // Creating NMS World
            CraftWorld craftWorld = (CraftWorld) world;
            ServerLevel serverLevel = craftWorld.getHandle();
            RandomState randomState = serverLevel.getChunkSource().randomState();
            Climate.Sampler sampler = randomState.sampler();

            for (int x = -100; x < 100; x++) {
                for (int z = -100; z < 100; z++) {
                    for (int y = -64; y < 320; y++) {
                        String prefix = x + ";" + y + ";" + z + ";";
                        DensityFunction.FunctionContext context = new DensityFunction.SinglePointContext(x, y, z);

                        temperatureFW.write(prefix + sampler.temperature().compute(context) + "\n");
                        humidityFW.write(prefix + sampler.humidity().compute(context) + "\n");
                        continentnessFW.write(prefix + sampler.continentalness().compute(context) + "\n");
                        erosionFW.write(prefix + sampler.erosion().compute(context) + "\n");
                        weirdnessFW.write(prefix + sampler.weirdness().compute(context) + "\n");
                        depthFW.write(prefix + sampler.depth().compute(context) + "\n");
                    }
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE,"Something went wrong while writing into files.",  e);
        }
        getLogger().info("Finished writing continentness, erosion, weirdness and depth.");

        getLogger().info("FINISHED EVERYTHING");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
