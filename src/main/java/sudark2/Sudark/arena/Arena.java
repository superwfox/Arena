package sudark2.Sudark.arena;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Arena extends JavaPlugin {

    static String arenaName = "ARENA-WORLD";
    static World world;

    @Override
    public void onEnable() {

        handleWorld();
        Bukkit.getPluginCommand("arena").setExecutor(new SaveChunkTemplateCommand());
        Bukkit.getPluginManager().registerEvents(new PortalListener(), this);

    }

    private static void handleWorld() {
        try {
            world = WorldManager.resetWorld();
            world.setGameRule(GameRule.DO_MOB_SPAWNING,false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static public Plugin get() {
        return Bukkit.getPluginManager().getPlugin("Arena");
    }

}
