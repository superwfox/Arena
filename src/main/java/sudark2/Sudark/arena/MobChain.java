package sudark2.Sudark.arena;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static sudark2.Sudark.arena.Arena.*;
import static sudark2.Sudark.arena.MobContainer.*;
import static sudark2.Sudark.arena.PortalListener.awaitTimeTable;

public class MobChain {

    public static void await(Player pl, int time) {
        pl.sendMessage("[§e争斗 §f持续下蹲离开此世界]");
        new BukkitRunnable() {
            String barName = "§e" + time + "§r§f 次争斗 §e等待中§r§f";
            BossBar bar = Bukkit.createBossBar(barName, BarColor.WHITE, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
            int wait = 35;

            @Override
            public void run() {
                if (wait == -1) {
                    monitor(pl, time);
                    cancel();
                    bar.removeAll();
                    awaitTimeTable.put(pl, time);
                    return;
                }
                bar.setProgress(wait / 35f);
                bar.addPlayer(pl);

                if (!pl.isOnline()) {
                    bar.removeAll();
                    cancel();
                    return;
                }

                if (pl.isSneaking() && pl.getTargetBlockExact(5).getType() == Material.YELLOW_GLAZED_TERRACOTTA) {
                    pl.teleport(pl.getRespawnLocation() == null ? new Location(Bukkit.getWorld("BEEF-DUNE"), 8.5, 36, 7) : pl.getRespawnLocation());
                    bar.removeAll();
                    cancel();
                    return;
                }

                wait--;
            }
        }.runTaskTimerAsynchronously(get(), 0, 20);
    }

    public static void monitor(Player pl, int time) {

        //对玩家创建异步任务 开始检测
        new BukkitRunnable() {
            int stage = 0;
            int s = 0;
            BossBar bar = Bukkit.createBossBar("§a§l" + time + "§r§f 次争斗 §l" + stage + 1 + "/5", BarColor.YELLOW, BarStyle.SOLID, BarFlag.DARKEN_SKY);

            @Override
            public void run() {

                bar.addPlayer(pl);

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

                //判断是否完成BOSS击杀
                if (stage > 4) {
                    if (time != awaitTimeTable.get(pl)) {
                        cancel();
                        await(pl, time + 1);
                        bar.removeAll();
                        PlayerWon(pl, time);
                    }
                    return;
                }

                //小波次循环
                if (stage == 4) {
                    hasBossAsync().thenAccept(has -> {
                        if (has) {
                            s += 15;//BOSS只生成两只
                        }
                    });
                } else {
                    s++;
                }

                bar.setProgress(Math.min(s / 12f, 1));

                //大波次循环 小波次刷怪
                if (s > 12) {
                    if (s == 15) {
                        bar.setTitle("§a§l" + time + "§r§f 次争斗 §e最后一搏!");
                    } else {
                        s = 0;
                        stage++;
                        bar.setTitle("§a§l" + time + "§r§f 次争斗 §l" + (stage + 1) + "/5");
                    }
                } else {
                    if (!trySpawn(pl, stage, time)) {
                        if (s > 1) s--;
                    }
                }

                consumeLvl(pl);
            }
        }.runTaskTimerAsynchronously(get(), 0, 80);
    }

    public static CompletableFuture<Boolean> hasBossAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(get(), () ->
                future.complete(world.getEntities().stream()
                        .anyMatch(en -> Arrays.asList(boss).contains(en.getType())))
        );
        return future;
    }


    private static void PlayerWon(Player pl, int time) {
        title(pl, "[§e§lWONDERFUL§r§f]", "恭喜你完成了 §b§l" + time + "§r§f 次争斗");

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == 0) world.getEntities().forEach(en -> {
                    if (en instanceof Mob) en.remove();
                });
                i++;
                if (i == 5) cancel();
                Firework fw = (Firework) pl.getWorld().spawnEntity(pl.getLocation().add(0, 3, 0), EntityType.FIREWORK_ROCKET);
                fw.setGlowing(true);

                FireworkMeta fwm = fw.getFireworkMeta();
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(Color.YELLOW) // 黄色
                        .withFade(Color.ORANGE) // 橙色
                        .with(FireworkEffect.Type.BURST)       // 烟火形状（球状）
                        .flicker(true)          // 闪烁效果
                        .trail(true)// 拖尾效果
                        .build();

                fwm.setPower(0);
                fwm.addEffect(effect);
                fw.setFireworkMeta(fwm);
            }
        }.runTaskTimer(get(), 0, 30L);
    }

    private static void consumeLvl(Player pl) {
        pl.giveExp(-pl.getExpToLevel() / 10);
        if (pl.getLevel() < 1)
            pl.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 1, false, false, false), true);
    }

    private static boolean trySpawn(Player pl, int stage, int time) {
        Location base = pl.getLocation();
        Random r = ThreadLocalRandom.current();

        float yaw = base.getYaw() + (r.nextBoolean() ? 180 : 0) + (r.nextFloat() * 60 - 30);
        double rad = Math.toRadians(yaw);
        double dist = 6 + r.nextDouble() * 2; // 距离 6~8 格

        double dx = -Math.sin(rad) * dist;
        double dz = Math.cos(rad) * dist;
        Location loc = base.clone().add(dx, 15, dz);

        // 向下找地面（避免递归）
        for (int i = 20; i > 0; i--) { // 最多检查20格
            if (loc.getBlock().getType().isSolid()) {
                loc.add(0, 1, 0);
                break;
            }
            loc.add(0, -1, 0);
            if (i == 1) return false;
        }

        // 检查是否找到合适位置
        Bukkit.getScheduler().runTask(get(), () -> {
            EntityType type = mobs[stage][r.nextInt(mobs[stage].length)];
            Mob mob = (Mob) loc.getWorld().spawnEntity(loc, type);

            double mobMaxHealth = mob.getMaxHealth() * (1 + time * 0.25);
            mob.setMaxHealth(mobMaxHealth);
            mob.setHealth(mobMaxHealth);

            monsters.addEntity(mob);

            mob.setGlowing(true);
            mob.setTarget(pl);
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0, false, false, false));

            if (stage < 5) equipMob(mob, stage, time);
        });

        Bukkit.getScheduler().runTask(get(), () -> {
            if (stage == 0) return;
            int typeIndex = r.nextInt(stage);
            int spcIndex = r.nextInt(mobs[typeIndex].length);
            EntityType type = mobs[typeIndex][spcIndex];
            Mob mob = (Mob) loc.getWorld().spawnEntity(loc, type);

            double mobMaxHealth = mob.getMaxHealth() * (1 + time * 0.25);
            mob.setMaxHealth(mobMaxHealth);
            mob.setHealth(mobMaxHealth);

            monsters.addEntity(mob);

            mob.setGlowing(true);
            mob.setTarget(pl);
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0, false, false, false));

            if (stage < 5) equipMob(mob, stage, time);
        });

        return true;

    }


    private static void equipMob(Mob mob, int stage, int time) {
        Random r = new Random();
        String[] tiers = {"LEATHER", "GOLDEN", "IRON", "DIAMOND", "NETHERITE"};
        int index = r.nextInt(stage, 5); // worth 决定品质层级
        String tier = tiers[index];

        EntityEquipment eq = mob.getEquipment();
        if (eq == null) return;

        boolean canHoldWeapon = mob instanceof Zombie || mob instanceof Skeleton || mob instanceof WitherSkeleton || mob instanceof Piglin;

        int enchantLvl = r.nextInt(time);
        List<Enchantment> ES = new ArrayList<>(normalEnchants);
        ES.add(getRE());

        if (canHoldWeapon && stage > 3)
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
