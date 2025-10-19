package sudark2.Sudark.arena;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static sudark2.Sudark.arena.Arena.get;

public class MobChain {

    public static void monitor(Player pl) {

        //对玩家创建异步任务 开始检测
        new BukkitRunnable() {
            float oldPitch = pl.getPitch();

            @Override
            public void run() {

                if (!pl.isOnline()) cancel();

                if (pl.getPitch() == oldPitch) {

                } else {
                    oldPitch = pl.getPitch();
                    trySpawn(pl);
                }

                consumeLvl(pl);

            }
        }.runTaskTimerAsynchronously(get(), 0, 20);

    }

    private static void consumeLvl(Player pl) {
        pl.giveExp(-pl.getExpToLevel() / 10);

    }

    private static void trySpawn(Player pl) {

    }
}
