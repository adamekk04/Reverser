package org.reverser;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.World;
import org.bukkit.WorldCreator;
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

        for (int x = -100; x < 100; x++) {
            for (int z = -100; z < 100; z++) {
                for (int y = -64; y < 320; y++) {
                    String fwPrefix = x + ";" + y + ";" + z;

                    try {
                        FileWriter temperatureFW = new FileWriter(temperature);
                        FileWriter humidityFW = new FileWriter(humidity);

                        temperatureFW.write(fwPrefix + world.getTemperature(x, y, z));
                        humidityFW.write(fwPrefix + world.getHumidity(x, y, z));

                        temperatureFW.close();
                        humidityFW.close();
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Something went wrong while writing into files.", e);
                    }
                }
            }
        }
        getLogger().info("Finished writing temperature and humidity.");

        // Fetching continentness, erosion, weirdness and depth by accessing NMS
        for (int x = -100; x < 100; x++) {
            for (int z = -100; z < 100; z++) {
                for (int y = -64; y < 320; y++) {
                    try {
                        String fwPrefix = x + ";" + y + ";" + z;

                        FileWriter continentnessFW = new FileWriter(continentness);
                        FileWriter erosionFW = new FileWriter(erosion);
                        FileWriter weirdnessFW = new FileWriter(weirdness);
                        FileWriter depthFW = new FileWriter(depth);

                        continentnessFW.write(fwPrefix + getContinentness(x, y, z, world));
                        erosionFW.write(fwPrefix + getErosion(x, y, z, world));
                        weirdnessFW.write(fwPrefix + getWeirdness(x, y, z, world));
                        depthFW.write(fwPrefix + getDepth(x, y, z, world));

                        continentnessFW.close();
                        erosionFW.close();
                        weirdnessFW.close();
                        depthFW.close();
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Something went wrong while writing into files.", e);
                    }
                }
            }
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
        ServerLevel serverLevel = (ServerLevel) world;
        return serverLevel.getChunkSource().randomState();
    }
}
