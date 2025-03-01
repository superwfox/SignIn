package sudark.signin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

import java.util.concurrent.ConcurrentHashMap;

public final class SignIn extends JavaPlugin {

    @Override
    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(new SignListener(), this);

    }

    Boolean late = false;
    ConcurrentHashMap<Player, Integer> sign = new ConcurrentHashMap<>();

    public void LateHarvest(Player pl) {
        if (!late) {
            (new BukkitRunnable() {
                public void run() {
                    late = false;
                    Score Sign = pl.getScoreboard().getObjective("sign").getScore(pl);
                    Sign.setScore(Sign.getScore() + 1);
                    if (sign.get(pl) != null) {
                        pl.sendTitle("[§e SIGN §f]", "§7签到成功 本次签到获得§b " + sign.get(pl) + "§e§lLevel");
                        sign.remove(pl);
                    }
                }
            }).runTaskLater(this, 80L);
        }
    }

    public class SignListener implements Listener {

        @EventHandler
        public void onSignChange(PlayerInteractEvent event) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.NOTE_BLOCK) {
                Player pl = event.getPlayer();
                int clickTime = 1;
                if (block.getWorld().equals(Bukkit.getWorld("BEEF-Main"))) {
                    int signTurn = pl.getScoreboard().getObjective("sign").getScore(pl).getScore();
                    if (signTurn > 1) {
                        pl.sendTitle("[§e SIGN §f]", "§7今天已经签到过了");
                    } else {
                        LateHarvest(pl);
                        late = true;
                        if (signTurn == 0) {
                            Location boxLoc = block.getLocation().add(0.5, 1.2, 0.5);

                            //点击效果
                            Particle.DustTransition dustTransition = new Particle.DustTransition(Color.YELLOW, Color.AQUA, 0.8F);
                            ItemStack goldBlockItem = new ItemStack(Material.GOLD_INGOT);
                            pl.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 2, 1));
                            pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2, 1));
                            pl.playSound(pl.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0F, 1.0F);
                            pl.sendTitle("[§e SIGN §f]", "§7签到中  连击增加§l§e MIM§r§7奖励");
                            final Item gold = block.getWorld().dropItemNaturally(boxLoc, goldBlockItem);
                            gold.setCanPlayerPickup(false);

                            //处理多次点击
                            (new BukkitRunnable() {
                                public void run() {
                                    gold.remove();
                                }
                            }).runTaskLater(SignIn.this, 80L);
                            block.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, boxLoc, 10, 0.5, 0.5, 0.5, 0.10000000149011612, dustTransition);
                            sign.putIfAbsent(pl, 0);
                            if ((Integer) sign.get(pl) <= 45) {
                                pl.giveExpLevels(1);
                                pl.sendActionBar("§l§eLevel + 1");
                                if (sign.containsKey(pl)) {
                                    clickTime = (Integer) sign.get(pl) + 1;
                                }

                                sign.put(pl, clickTime);
                            }
                        }
                    }
                }
            }
        }
    }
}
