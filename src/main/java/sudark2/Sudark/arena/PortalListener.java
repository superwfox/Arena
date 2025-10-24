package sudark2.Sudark.arena;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static sudark2.Sudark.arena.Arena.*;
import static sudark2.Sudark.arena.MobChain.await;
import static sudark2.Sudark.arena.MobContainer.boss;

public class PortalListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerPortalEvent event) {
        Player pl = event.getPlayer();
        Location loc = pl.getLocation();
        for (int i = -2; i < 2; i++) {
            for (int j = -2; j < 2; j++) {
                Block block = loc.clone().add(i, -1, j).getBlock();
                if (block.getType() == Material.GILDED_BLACKSTONE) {
                    awaitTimeTable.merge(pl, 0, Integer::sum);
                    event.setTo(new Location(world, pl.getX() / 12, 8, pl.getZ() / 12));
                    await(pl, 1);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityPortalEnter(EntityPortalEvent event) {
        World arena = Bukkit.getWorld(arenaName);
        if (arena != null && event.getEntity().getWorld().equals(arena))
            event.setCancelled(true);
    }

    static Map<Player, Integer> awaitTimeTable = new HashMap<>();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity en = event.getEntity();
        if (!en.getWorld().getName().equals(arenaName)) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        event.setDroppedExp(killer.getExpToLevel() * awaitTimeTable.get(killer));

        if (Arrays.stream(boss).toList().contains(en.getType())) {
            awaitTimeTable.merge(killer, 1, Integer::sum);
        }
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        Player pl = event.getPlayer();
        if (pl.getWorld().getName().equals(arenaName)) {
            event.setDeathMessage(pl.getName() + " 在第 §e" + awaitTimeTable.get(pl) + " §f次争斗中战败而亡");
            event.setKeepInventory(false);
        }
    }

    @EventHandler
    public void onEntityChange(EntityTransformEvent event) {
        if (!event.getEntity().getWorld().getName().equals(arenaName)) return;

        // 保存装备
        if (event.getEntity() instanceof LivingEntity le) {
            EntityEquipment eq = le.getEquipment();
            ItemStack[] armor = eq.getArmorContents();
            ItemStack main = eq.getItemInMainHand();
            ItemStack off = eq.getItemInOffHand();

            // 取消变形
            event.setCancelled(true);

            // 恢复装备（防止 Bukkit 清空）
            Bukkit.getScheduler().runTaskLater(get(), () -> {
                eq.setArmorContents(armor);
                eq.setItemInMainHand(main);
                eq.setItemInOffHand(off);
            }, 1L);
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
}
