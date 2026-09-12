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
            throw new RuntimeException(e);
        }
        getLogger().info("Fetched seed " + seed);

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

        // Fetching temperature and humidity for 100x100 block radius by using PaperAPI
        WorldCreator worldCreator = new WorldCreator("reverser");
        worldCreator.seed(seed);
        World world = worldCreator.createWorld();
        getLogger().info("Created world");

        try (
                FileWriter temperatureFW = new FileWriter(temperature);
                FileWriter humidityFW = new FileWriter(humidity)
        ) {
            for (int x = -100; x < 100; x++) {
                for (int z = -100; z < 100; z++) {
                    for (int y = -64; y < 320; y++) {
                        String prefix = x + ";" + y + ";" + z + ";";

                        temperatureFW.write(prefix + world.getTemperature(x, y, z) + "\n");
                        humidityFW.write(prefix + world.getHumidity(x, y, z) + "\n");
                    }
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Something went wrong while writing into files.", e);
        }
        getLogger().info("Finished writing temperature and humidity.");

        // Fetching continentness, erosion, weirdness and depth by accessing NMS
        try (
                FileWriter continentnessFW = new FileWriter(continentness);
                FileWriter erosionFW = new FileWriter(erosion);
                FileWriter weirdnessFW = new FileWriter(weirdness);
                FileWriter depthFW = new FileWriter(depth)
        ) {
            for (int x = -100; x < 100; x++) {
                for (int z = -100; z < 100; z++) {
                    for (int y = -64; y < 320; y++) {
                        String prefix = x + ";" + y + ";" + z + ";";

                        continentnessFW.write(prefix + getContinentness(x, y, z, world) + "\n");
                        erosionFW.write(prefix + getErosion(x, y, z, world) + "\n");
                        weirdnessFW.write(prefix + getWeirdness(x, y, z, world) + "\n");
                        depthFW.write(prefix + getDepth(x, y, z, world) + "\n");
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

    private double getContinentness(int x, int y, int z, World world) {
        RandomState randomState = makeNMSWorld(world);
        Climate.Sampler sampler = randomState.sampler();
        DensityFunction.FunctionContext context = new DensityFunction.SinglePointContext(x, y, z);

        return sampler.continentalness().compute(context);
    }

    private double getErosion(int x, int y, int z, World world) {
        RandomState randomState = makeNMSWorld(world);
        Climate.Sampler sampler = randomState.sampler();
        DensityFunction.FunctionContext context = new DensityFunction.SinglePointContext(x, y, z);

        return sampler.erosion().compute(context);
    }

    private double getWeirdness(int x, int y, int z, World world) {
        RandomState randomState = makeNMSWorld(world);
        Climate.Sampler sampler = randomState.sampler();
        DensityFunction.FunctionContext context = new DensityFunction.SinglePointContext(x, y, z);

        return sampler.weirdness().compute(context);
    }

    private double getDepth(int x, int y, int z, World world) {
        RandomState randomState = makeNMSWorld(world);
        Climate.Sampler sampler = randomState.sampler();
        DensityFunction.FunctionContext context = new DensityFunction.SinglePointContext(x, y, z);

        return sampler.depth().compute(context);
    }

    private RandomState makeNMSWorld(World world) {
        CraftWorld craftWorld = (CraftWorld) world;
        ServerLevel serverLevel = craftWorld.getHandle();
        return serverLevel.getChunkSource().randomState();
    }
}
