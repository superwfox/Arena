package sudark2.Sudark.arena;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import static sudark2.Sudark.arena.Arena.arenaName;
import static sudark2.Sudark.arena.Arena.get;

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

    @EventHandler
    public void onNewChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;

        Chunk chunk = event.getChunk();
        if (!chunk.getWorld().getName().equals(arenaName)) return;

        Bukkit.getScheduler().runTaskLater(get(), () -> {
            World world = chunk.getWorld();
            world.getBlockAt(chunk.getBlock(7, -63, 7).getLocation()).setType(Material.GOLD_BLOCK);
            world.getBlockAt(chunk.getBlock(7, -63, 7).getLocation()).setType(Material.GILDED_BLACKSTONE);
            world.getBlockAt(chunk.getBlock(0, -63, 0).getLocation()).setType(Material.PURPLE_STAINED_GLASS);
            world.getBlockAt(chunk.getBlock(15, -63, 15).getLocation()).setType(Material.BLACK_STAINED_GLASS);
        }, 1L);
    }

    public void inquireTp(Player pl) {
        pl.sendMessage("Do you want to teleport to the arena? (y/n)");
    }
}
