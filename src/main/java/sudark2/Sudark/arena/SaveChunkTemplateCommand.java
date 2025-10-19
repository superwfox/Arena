package sudark2.Sudark.arena;


import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        List<Integer> listX = new ArrayList<>();
        List<Integer> listY = new ArrayList<>();
        List<Integer> listZ = new ArrayList<>();
        List<Material> listBlocks = new ArrayList<>();
        List<Integer> listType = new ArrayList<>();

        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.isEmpty()) continue;

                    listX.add(x);
                    listY.add(y - minY); // 相对高度
                    listZ.add(z);
                    listBlocks.add(block.getType());

                    // 使用 BlockPropertyUtil 获取 type
                    int type = BlockPropertyUtil.getPropertyCode(block);
                    listType.add(type);
                }
            }
        }


        StringBuilder sb = new StringBuilder();
        sb.append("ChunkTemplate template = new ChunkTemplate(\n");

        // dx
        sb.append("    new int[]{");
        for (int i = 0; i < listX.size(); i++) sb.append(listX.get(i)).append(i == listX.size() - 1 ? "" : ", ");
        sb.append("},\n");

        // dy
        sb.append("    new int[]{");
        for (int i = 0; i < listY.size(); i++) sb.append(listY.get(i)).append(i == listY.size() - 1 ? "" : ", ");
        sb.append("},\n");

        // dz
        sb.append("    new int[]{");
        for (int i = 0; i < listZ.size(); i++) sb.append(listZ.get(i)).append(i == listZ.size() - 1 ? "" : ", ");
        sb.append("},\n");

        // blocks
        sb.append("    new Material[]{");
        for (int i = 0; i < listBlocks.size(); i++)
            sb.append("Material.").append(listBlocks.get(i).name())
                    .append(i == listBlocks.size() - 1 ? "" : ", ");
        sb.append("},\n");

        // type
        sb.append("    new int[]{");
        for (int i = 0; i < listType.size(); i++)
            sb.append(listType.get(i)).append(i == listType.size() - 1 ? "" : ", ");
        sb.append("}\n");

        sb.append(");");

        Bukkit.getLogger().info("[ChunkTemplate] Y " + minY + " → " + maxY + " (" + listBlocks.size() + " blocks)");
        fileLoad(sb.toString());
        p.sendMessage("§aTemplate saved! Height=" + (maxY - minY + 1) + ", blocks=" + listBlocks.size());
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

