package sudark2.Sudark.arena;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;

public class BlockPropertyUtil {

    /**
     * 在 ChunkData 指定位置放置方块，并应用 int 特性
     *
     * @param chunkData 区块数据
     * @param x 相对区块 x (0~15)
     * @param y 高度
     * @param z 相对区块 z (0~15)
     * @param material 方块类型
     * @param code 特性编码
     */
    public static void setBlockWithProperty(ChunkGenerator.ChunkData chunkData, int x, int y, int z, Material material, int code) {
        BlockData data = material.createBlockData();

        // 方向 + Half（1-8）
        if (code >= 1 && code <= 8) {
            Half half = (code <= 4) ? Half.TOP : Half.BOTTOM;

            if (data instanceof Bisected bis) {
                bis.setHalf(half);

                if (bis instanceof Directional dir) {
                    BlockFace face = switch ((code - 1) % 4) {
                        case 0 -> BlockFace.NORTH;
                        case 1 -> BlockFace.EAST;
                        case 2 -> BlockFace.SOUTH;
                        case 3 -> BlockFace.WEST;
                        default -> BlockFace.NORTH;
                    };
                    dir.setFacing(face);
                }
            }
        }

        // Slab 类型（9-10）
        if (code >= 9 && code <= 10 && data instanceof Slab slab) {
            slab.setType((code == 9) ? Type.TOP : Type.BOTTOM);
        }

        // 放置方块到 ChunkData
        chunkData.setBlock(x, y, z, data);
    }

    public static int getPropertyCode(Block block) {
        BlockData data = block.getBlockData();

        // 方向 + Half（1-8）
        if (data instanceof Bisected bis) {
            Half half = bis.getHalf();
            int halfOffset = (half == Half.TOP) ? 0 : 4; // TOP=0, BOTTOM=4

            if (bis instanceof Directional dir) {
                BlockFace face = dir.getFacing();
                int faceIndex = switch (face) {
                    case NORTH -> 0;
                    case EAST -> 1;
                    case SOUTH -> 2;
                    case WEST -> 3;
                    default -> 0;
                };
                return faceIndex + 1 + halfOffset; // 1~8
            }

            // 如果没有方向，也按 Half 编码（TOP=1~4，BOTTOM=5~8）
            return 1 + halfOffset;
        }

        // Slab 类型（9-10）
        if (data instanceof Slab slab) {
            return (slab.getType() == Type.TOP) ? 9 : 10;
        }

        // 默认 0
        return 0;
    }
}
