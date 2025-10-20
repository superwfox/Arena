package sudark2.Sudark.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static sudark2.Sudark.arena.Arena.arenaName;
import static sudark2.Sudark.arena.Arena.get;
import static sudark2.Sudark.arena.MobContainer.mobs;
import static sudark2.Sudark.arena.MobContainer.monsters;

public class MobChain {

    public static void monitor(Player pl, int time) {

        //对玩家创建异步任务 开始检测
        new BukkitRunnable() {
            float oldPitch = pl.getPitch();
            int stage = 0;
            int s = 0;
            BossBar bar = Bukkit.createBossBar("§a§l" + time + "§r§f 次争斗 §l" + stage + 1 + "/5", BarColor.YELLOW, BarStyle.SOLID, BarFlag.DARKEN_SKY);

            @Override
            public void run() {

                if (!pl.isOnline()) {
                    bar.removeAll();
                    cancel();
                    return;
                }

                if (!pl.getWorld().getName().equals(arenaName)) {
                    bar.removeAll();
                    cancel();
                    return;
                }

                //AFK
                if (pl.getPitch() == oldPitch) {
                } else {
                    oldPitch = pl.getPitch();
                    s++;
                    bar.setProgress(s / 12f);
                }

                trySpawn(pl, stage, time);
                consumeLvl(pl);

                if (s > 12) {
                    s = 0;
                    stage++;
                    bar.setTitle("§a§l" + time + "§r§f 次争斗 §l" + stage + 1 + "/5");
                }

                if (stage > 4) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(get(), 0, 100);

    }

    private static void PlayerWon(Player pl, int time) {
        title(pl, "[§e§lWONDERFUL]", "恭喜你完成了 §b§l" + time + "§r§f 次争斗");
    }

    private static void consumeLvl(Player pl) {
        pl.giveExp(-pl.getExpToLevel() / 10);
        if (pl.getLevel() < 1)
            pl.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 1, false, false, false), true);
    }

    private static void trySpawn(Player pl, int stage, int time) {
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

        Bukkit.getScheduler().runTask(get(), () -> {
            Mob mob = (Mob) loc.getWorld().spawnEntity(loc, mobs[stage][r.nextInt(mobs[stage].length)]);

            double mobMaxHealth = mob.getMaxHealth() * (1 + time * 0.25);
            mob.setMaxHealth(mobMaxHealth);
            mob.setHealth(mobMaxHealth);

            monsters.addEntity(mob);

            mob.setGlowing(true);
            mob.setTarget(pl);
            if (stage < 5) equipMob(mob, stage, time);
        });
    }

    private static void equipMob(Mob mob, int worth, int time) {
        Random r = new Random();
        String[] tiers = {"LEATHER", "GOLDEN", "IRON", "DIAMOND", "NETHERITE"};
        int index = r.nextInt(worth, 5); // worth 决定品质层级
        String tier = tiers[index];

        EntityEquipment eq = mob.getEquipment();
        if (eq == null) return;

        boolean canHoldWeapon = mob instanceof Zombie || mob instanceof Skeleton || mob instanceof WitherSkeleton || mob instanceof Piglin;

        int enchantLvl = r.nextInt(time);
        List<Enchantment> ES = new ArrayList<>(normalEnchants);
        ES.add(getRE());

        if (canHoldWeapon)
            eq.setItemInMainHand(enchantItem(Material.valueOf(tier + "_SWORD"), List.of(getRE()), enchantLvl));

        eq.setHelmet(enchantItem(Material.valueOf(tier + "_HELMET"), ES, enchantLvl));
        eq.setHelmetDropChance(0.04f);
        eq.setChestplate(enchantItem(Material.valueOf(tier + "_CHESTPLATE"), ES, enchantLvl));
        eq.setChestplateDropChance(0.04f);
        eq.setLeggings(enchantItem(Material.valueOf(tier + "_LEGGINGS"), ES, enchantLvl));
        eq.setLeggingsDropChance(0.04f);
        eq.setBoots(enchantItem(Material.valueOf(tier + "_BOOTS"), ES, enchantLvl));
        eq.setBootsDropChance(0.04f);

    }

    private static Enchantment getRE() {
        Enchantment[] enchants = Enchantment.values();
        return enchants[new Random().nextInt(enchants.length)];
    }

    static List<Enchantment> normalEnchants = List.of(
            Enchantment.PROTECTION,
            Enchantment.BLAST_PROTECTION,
            Enchantment.FIRE_PROTECTION,
            Enchantment.PROJECTILE_PROTECTION
    );

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

    public static void title(Player pl, String t1, String t2) {
        new BukkitRunnable() {
            StringBuilder temt = new StringBuilder("_");
            int i = 0;

            @Override
            public void run() {
                temt.append(t2.toCharArray()[i]);
                pl.sendTitle(t1, temt + "§f_", 0, 50, 20);
                i++;
                if (i == t2.length()) {
                    cancel();
                }
            }
        }.runTaskTimer(get(), 0, 2);
    }
}
