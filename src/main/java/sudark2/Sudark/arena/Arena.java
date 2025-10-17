package sudark2.Sudark.arena;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Arena extends JavaPlugin {

    static String arenaName = "ARENA-WORLD";
    static World world;

    @Override
    public void onEnable() {

        try {
            world = WorldManager.resetWorld();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(new PortalListener(), this);

    }

    static public Plugin get(){
        return Bukkit.getPluginManager().getPlugin("Arena");
    }

}
