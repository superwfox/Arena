package sudark2.Sudark.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

import static sudark2.Sudark.arena.Arena.get;
import static sudark2.Sudark.arena.MobContainer.mobs;
import static sudark2.Sudark.arena.MobContainer.monsters;

public class MobChain {

    public static void monitor(Player pl) {

        //对玩家创建异步任务 开始检测
        new BukkitRunnable() {
            float oldPitch = pl.getPitch();

            @Override
            public void run() {

                if (!pl.isOnline()) cancel();

                //AFK
                if (pl.getPitch() == oldPitch) {
                } else {
                    oldPitch = pl.getPitch();
                }

                trySpawn(pl);
                consumeLvl(pl);

            }
        }.runTaskTimerAsynchronously(get(), 0, 100);

    }

    private static void consumeLvl(Player pl) {
        pl.giveExp(-pl.getExpToLevel() / 10);
        if (pl.getLevel() < 1)
            pl.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 1, false, false, false), true);
    }

    private static void trySpawn(Player pl, int stage) {
        Location base = pl.getLocation();
        Random r = new Random();

        float yaw = base.getYaw() + (r.nextBoolean() ? 180 : 0) + (r.nextFloat() * 60 - 30);
        double rad = Math.toRadians(yaw);
        double dist = 6 + r.nextDouble() * 2; // 距离 6~8 格

        double dx = -Math.sin(rad) * dist;
        double dz = Math.cos(rad) * dist;
        Location loc = base.clone().add(dx, -1, dz);

        while (loc.getBlock().getType() == Material.AIR && loc.getY() < 128) {
            loc.add(0, 1, 0);
        }

        Mob mob = (Mob) loc.getWorld().spawnEntity(loc,mobs[stage][r.nextInt(mobs[stage].length)]);
        monsters.addEntity(mob);
    }

    public static ItemStack enchantItem(Material material, List<Enchantment> enchants, int lvl) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        for (Enchantment enchant : enchants) {
            meta.addEnchant(enchant, lvl, true);
        }
        item.setItemMeta(meta);
        return item;
    }

}
