package sudark2.Sudark.arena;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class MobContainer {

    static Team monsters;

    static {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        monsters = board.getTeam("monsters");
        if (monsters == null) {
            monsters = board.registerNewTeam("monsters");
        }
        monsters.color(NamedTextColor.BLACK);
        monsters.setAllowFriendlyFire(false);
    }

    static EntityType[] easy = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.DROWNED,EntityType.EVOKER
    };
    static EntityType[] normal = {
            EntityType.WITHER_SKELETON, EntityType.WITCH, EntityType.BOGGED, EntityType.SPIDER
    };
    static EntityType[] hard = {
            EntityType.VINDICATOR, EntityType.PHANTOM, EntityType.PIGLIN_BRUTE, EntityType.POLAR_BEAR
    };
    static EntityType[] extreme = {
            EntityType.RAVAGER, EntityType.IRON_GOLEM, EntityType.ELDER_GUARDIAN, EntityType.STRAY
    };
    static EntityType[] boss = {
            EntityType.WARDEN, EntityType.WITHER
    };

    static EntityType[][] mobs = {easy, normal, hard, extreme, boss};

}
