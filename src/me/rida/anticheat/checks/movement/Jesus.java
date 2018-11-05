package me.rida.anticheat.checks.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.utils.UtilCheat;

public class Jesus extends Check {
	public static Map<Player, Integer> onWater;
	public static List<Player> placedBlockOnWater;
	public static Map<Player, Integer> count;
	public static Map<Player, Long> velocity;

	public Jesus(AntiCheat AntiCheat) {
		super("Jesus", "Jesus", AntiCheat);

		this.setEnabled(true);
		this.setBannable(true);
		setViolationsToNotify(1);
		setMaxViolations(5);
		
		count = new WeakHashMap<Player, Integer>();
		placedBlockOnWater = new ArrayList<Player>();
		onWater = new WeakHashMap<Player, Integer>();
		velocity = new WeakHashMap<Player, Long>();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e) {
		if (placedBlockOnWater.contains(e.getPlayer())) {
			placedBlockOnWater.remove(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent e) {
		if (onWater.containsKey(e.getEntity())) {
			onWater.remove(e.getEntity());
		}
		if (placedBlockOnWater.contains(e.getEntity())) {
			placedBlockOnWater.remove(e.getEntity());
		}
		if (count.containsKey(e.getEntity())) {
			count.remove(e.getEntity());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onVelocity(PlayerVelocityEvent e) {
		velocity.put(e.getPlayer(), System.currentTimeMillis());
	}

	@EventHandler
	public void OnPlace(BlockPlaceEvent e) {
		if (e.getBlockReplacedState().getBlock().getType() == Material.WATER) {
			placedBlockOnWater.add(e.getPlayer());
		}
	}

	@EventHandler
	public void CheckJesus(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		
		/**False positive/optimization check **/
		if (event.isCancelled()
				|| (event.getFrom().getX() == event.getTo().getX()) && (event.getFrom().getZ() == event.getTo().getZ())
				|| getAntiCheat().isSotwMode() 
				|| p.getAllowFlight()
				|| UtilCheat.isOnLilyPad(p)
				|| p.getLocation().clone().add(0.0D, 0.4D, 0.0D).getBlock().getType().isSolid()
				|| placedBlockOnWater.remove(p)) {
			return;
		}

		int Count = count.getOrDefault(p, 0);

		if ((UtilCheat.cantStandAtWater(p.getWorld().getBlockAt(p.getLocation())))
				&& (UtilCheat.isHoveringOverWater(p.getLocation())) && (!UtilCheat.isFullyInWater(p.getLocation()))) {
			Count+= 2;
		} else {
			Count = Count > 0 ? Count - 1 : Count;
		}

		if (Count > 19) {
			Count = 0;
			getAntiCheat().logCheat(this, p, null, null);
		}
		
		count.put(p, Count);
	}

}