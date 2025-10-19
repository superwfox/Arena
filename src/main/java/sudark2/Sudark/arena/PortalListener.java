package sudark2.Sudark.arena;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;

public class PortalListener implements Listener {

    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent event) {
        Location loc = event.getLocation();
        for (int i = -2; i < 2; i++) {
            for (int j = -2; j < 2; j++) {
                if (loc.getBlock().getRelative(i, -1, j).getType() == Material.GILDED_BLACKSTONE) {
                    event.setCancelled(true);
                    Player pl = (Player) event.getEntity();
                    inquireTp(pl);
                    return;
                }
            }
        }

    }

//    @EventHandler
//    public void onNewChunkLoad(ChunkLoadEvent event) {
//        if (!event.isNewChunk()) return;
//
//        Chunk chunk = event.getChunk();
//        if (!chunk.getWorld().getName().equals(arenaName)) return;
//
//        Bukkit.getScheduler().runTaskLater(get(), () -> {
//            World world = chunk.getWorld();
//            int chunkX = chunk.getX() << 4;
//            int chunkZ = chunk.getZ() << 4;
//            int y = -63;
//
//            Block b1 = world.getBlockAt(chunkX + 7, y, chunkZ + 7);
//            Block b2 = world.getBlockAt(chunkX + 0, y, chunkZ + 0);
//            Block b3 = world.getBlockAt(chunkX + 15, y, chunkZ + 15);
//
//            b1.setType(Material.GILDED_BLACKSTONE, false);
//            b2.setType(Material.PURPLE_STAINED_GLASS, false);
//            b3.setType(Material.WHITE_TERRACOTTA, false);
//        }, 8L);
//
//    }


    public void inquireTp(Player pl) {
        pl.sendMessage("Do you want to teleport to the arena? (y/n)");
    }
}
