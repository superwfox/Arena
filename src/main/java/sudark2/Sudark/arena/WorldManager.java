package sudark2.Sudark.arena;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static sudark2.Sudark.arena.Arena.arenaName;
import static sudark2.Sudark.arena.ChunkTemplate.temps;

public class WorldManager {

    public static World resetWorld() throws IOException {
        deleteWorld();
        return createVoidWorld(arenaName);
    }

    private static void deleteWorld() throws IOException {
        // if (world != null) Bukkit.unloadWorld(world, false);
        FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer(), arenaName));
    }

    public static World createVoidWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);

        creator.environment(World.Environment.NORMAL)
                .type(WorldType.FLAT)
                .generateStructures(false)
                .generator(new TemplateGenerator());

        return creator.createWorld();
    }

    private static class TemplateGenerator extends ChunkGenerator {
        @Override
        public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random,
                                  int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
            temps[0].pasteAtChunkData(chunkData);
        }
    }

}
