package sudark2.Sudark.arena;


import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

import static sudark2.Sudark.arena.Arena.get;

public class SaveChunkTemplateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        Chunk chunk = p.getLocation().getChunk();
        World world = chunk.getWorld();

        int worldMin = world.getMinHeight();
        int worldMax = world.getMaxHeight();

        // 找最低非空气层（bottom trim）
        int minY = worldMax;
        outer:
        for (int y = worldMin; y < worldMax; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (!chunk.getBlock(x, y, z).isEmpty()) {
                        minY = y;
                        break outer;
                    }
                }
            }
        }

        // 找最高非空气层（top trim）
        int maxY = worldMin;
        outer:
        for (int y = worldMax - 1; y >= worldMin; y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (!chunk.getBlock(x, y, z).isEmpty()) {
                        maxY = y;
                        break outer;
                    }
                }
            }
        }

        if (maxY < minY) {
            p.sendMessage("§cThis chunk is empty!");
            return true;
        }

        int height = maxY - minY + 1;
        StringBuilder sb = new StringBuilder("Material[] template = new Material[" + (16 * 16 * height) + "];\n");
        sb.append("int height = ").append(height).append(";\n");
        sb.append("int baseY = ").append(minY).append(";\n\n");

        int index = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Material m = chunk.getBlock(x, y, z).getType();
                    sb.append("template[").append(index++).append("] = Material.")
                            .append(m.name()).append(";");
                }
            }
        }

        Bukkit.getLogger().info("[ChunkTemplate] Y " + minY + " → " + maxY + " (" + height + " layers, " + index + " blocks)");
        fileLoad(sb.toString());
        p.sendMessage("§aTemplate saved! Height=" + height + ", range=" + minY + "~" + maxY);
        return true;
    }

    private void fileLoad(String text) {
        try {
            File file = new File(Bukkit.getPluginsFolder(), "template.txt");
            file.createNewFile();
            java.nio.file.Files.write(file.toPath(), text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

