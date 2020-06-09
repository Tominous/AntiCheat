package me.rida.anticheat.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.data.DataPlayer;
import me.rida.anticheat.utils.BlockUtil;
import me.rida.anticheat.utils.MathUtil;
import me.rida.anticheat.utils.PlayerUtil;
import me.rida.anticheat.utils.TimerUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MoveEvent implements Listener {


	public static int defaultWait = 15; // This is in ticks
	public static Map<UUID, Long> lastMove = new WeakHashMap<>();

	// We need to create hashmaps to store the amount of time left

	public static HashMap<String, Integer> ticksLeft = new HashMap(); // This is the amount of ticks left to wait
	public static HashMap<String, BukkitRunnable> cooldownTask = new HashMap(); // This is the task event

	// Checks to see if the person is in the timer
	public boolean inTimer(Player player) {
		if(ticksLeft.isEmpty() || !ticksLeft.containsKey(player.getName().toString())) {
			return false;
		}
		// Just making sure!
		if(ticksLeft.containsKey(player.getName().toString())) {
			return true;
		}
		return false;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();
		if (p == null) {
			return;
		}
		lastMove.put(p.getUniqueId(), System.currentTimeMillis());
		if(inTimer(p) == true) {
			return;
		}else {
			AntiCheat.Instance.startTimer(p);
		}
		if (e.getFrom().getX() != e.getTo().getX()
				|| e.getFrom().getY() != e.getTo().getY()
				|| e.getFrom().getZ() != e.getTo().getZ()) {
			final DataPlayer data = AntiCheat.getInstance().getDataManager().getDataPlayer(p);
			if (data != null) {
				data.setOnGround(PlayerUtil.isOnTheGround(p));
				data.onGround = PlayerUtil.isOnGround4(p);
				data.setOnStairSlab(PlayerUtil.isInStairs(p));
				data.onStairSlab = PlayerUtil.isInStairs(p);
				data.setInLiquid(PlayerUtil.isInLiquid(p));
				data.inLiquid = PlayerUtil.isInLiquid(p);
				data.setOnIce(PlayerUtil.isOnIce(p));
				data.onIce = PlayerUtil.isOnIce(p);
				data.setOnClimbable(PlayerUtil.isOnClimbable(p));
				data.onClimbable = PlayerUtil.isOnClimbable(p);
				data.setUnderBlock(PlayerUtil.inUnderBlock(p));
				data.underBlock = PlayerUtil.inUnderBlock(p);

				if(data.isOnGround()) {
					data.groundTicks++;
					data.airTicks = 0;
				} else {
					data.airTicks++;
					data.groundTicks = 0;
				}

				data.iceTicks = Math.max(0, data.isOnIce() ? data.iceTicks + 1  : data.iceTicks - 1);
				data.liquidTicks = Math.max(0, data.isInLiquid() ? data.liquidTicks + 1  : data.liquidTicks - 1);
				data.blockTicks = Math.max(0, data.isUnderBlock() ? data.blockTicks + 1  : data.blockTicks - 1);
			}
		}
		final DataPlayer data = AntiCheat.getInstance().getDataManager().getData(p);
		if (data.isNearIce()) {
			if (TimerUtil.elapsed(data.getIsNearIceTicks(),500L)) {
				if (!PlayerUtil.isNearIce(p)) {
					data.setNearIce(false);
				} else {
					data.setIsNearIceTicks(TimerUtil.nowlong());
				}
			}
		}

		final Location l = p.getLocation();
		final int x = l.getBlockX();
		final int y = l.getBlockY();
		final int z = l.getBlockZ();
		final Location loc1 = new Location(p.getWorld(), x, y + 1, z);
		if (loc1.getBlock().getType() != Material.AIR) {
			if (!data.isBlockAbove_Set()) {
				data.setBlockAbove_Set(true);
				data.setBlockAbove(TimerUtil.nowlong());
			} else {
				if (TimerUtil.elapsed(data.getBlockAbove(),1000L)) {
					if (loc1.getBlock().getType() == Material.AIR) {
						data.setBlockAbove_Set(false);
					} else {
						data.setBlockAbove_Set(true);
						data.setBlockAbove(TimerUtil.nowlong());
					}
				}
			}
		} else {
			if (data.isBlockAbove_Set()) {
				if (TimerUtil.elapsed(data.getBlockAbove(), 1000L)) {
					if (loc1.getBlock().getType() == Material.AIR) {
						data.setBlockAbove_Set(false);
					} else {
						data.setBlockAbove_Set(true);
						data.setBlockAbove(TimerUtil.nowlong());
					}
				}
			}
		}

		if (PlayerUtil.isNearIce(p)) {
			if(data.getIceTicks() < 60) {
				data.setIceTicks(data.getIceTicks() + 1);
			}
		} else if(data.getIceTicks() > 0) {
			data.setIceTicks(data.getIceTicks() - 1);
		}
		final Location loc = p.getPlayer().getLocation();
		loc.setY(loc.getY() -1);

		final Block block = loc.getWorld().getBlockAt(loc);
		if(block.getType().equals(Material.AIR)) {
			if (!(DataPlayer.lastAir.contains(p.getPlayer().getName().toString()))) {
				DataPlayer.lastAir.add(p.getPlayer().getName().toString());
			}
		}
		if(!(block.getType().equals(Material.AIR))) {
			if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
				if (DataPlayer.lastAir.contains(p.getPlayer().getName().toString())) {
					DataPlayer.lastAir.remove(p.getPlayer().getName().toString());
				}
			}
		}
		if(PlayerUtil.isNearSlime(p.getLocation())) {
			if (!(DataPlayer.lastNearSlime.contains(p.getPlayer().getName().toString()))) {
				DataPlayer.lastNearSlime.add(p.getPlayer().getName().toString());
				Bukkit.broadcastMessage(p.getPlayer().getName().toString() + " is now added to the list");
			}
		}
		if(!(PlayerUtil.isNearSlime(p.getLocation()))) {
			if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
				if (DataPlayer.lastNearSlime.contains(p.getPlayer().getName().toString())) {
					DataPlayer.lastNearSlime.remove(p.getPlayer().getName().toString());
					Bukkit.broadcastMessage(p.getPlayer().getName().toString() + " is now removed from the list");
				}
			}
		}
		if(DataPlayer.lastAir.contains(p.getPlayer().getName().toString())) {
			if (DataPlayer.getWasSpider() < 2) {
				DataPlayer.setWasSpider(2);
			}
		}
		if(!DataPlayer.lastAir.contains(p.getPlayer().getName().toString())) {
			if (DataPlayer.getWasSpider() > 0) {
				DataPlayer.setWasSpider(DataPlayer.getWasSpider() - 1);
			}
		}
		if(p.getAllowFlight()) {
			if (DataPlayer.getWasFlying() < 5) {
				DataPlayer.setWasFlying(5);
			}
		}
		if(!p.getAllowFlight()) {
			if (DataPlayer.getWasFlying() > 0) {
				DataPlayer.setWasFlying(DataPlayer.getWasFlying() - 1);
			}
		}
		if (BlockUtil.isHalfBlock(p.getLocation().add(0,-0.50,0).getBlock())|| BlockUtil.isLessThanBlock(p.getLocation().add(0,-0.50,0).getBlock()) || PlayerUtil.isNearHalfBlock(p)) {
			if (!data.isHalfBlocks_MS_Set()) {
				data.setHalfBlocks_MS_Set(true);
				data.setHalfBlocks_MS(TimerUtil.nowlong());
			} else {
				if (TimerUtil.elapsed(data.getHalfBlocks_MS(),900L)) {
					if (BlockUtil.isHalfBlock(p.getLocation().add(0,-0.50,0).getBlock()) || PlayerUtil.isNearHalfBlock(p)) {
						data.setHalfBlocks_MS_Set(true);
						data.setHalfBlocks_MS(TimerUtil.nowlong());
					} else {
						data.setHalfBlocks_MS_Set(false);
					}
				}
			}
		} else {
			if (TimerUtil.elapsed(data.getHalfBlocks_MS(),900L)) {
				if (BlockUtil.isHalfBlock(p.getLocation().add(0,-0.50,0).getBlock()) || PlayerUtil.isNearHalfBlock(p)) {
					data.setHalfBlocks_MS_Set(true);
					data.setHalfBlocks_MS(TimerUtil.nowlong());
				} else {
					data.setHalfBlocks_MS_Set(false);
				}
			}
		}
		if (PlayerUtil.isNearIce(p) && !data.isNearIce()) {
			data.setNearIce(true);
			data.setIsNearIceTicks(TimerUtil.nowlong());
		} else if (PlayerUtil.isNearIce(p)) {
			data.setIsNearIceTicks(TimerUtil.nowlong());
		}

		final double distance = MathUtil.getVerticalDistance(e.getFrom(), e.getTo());

		final boolean onGround = PlayerUtil.isOnGround4(p);
		if(!onGround
				&& e.getFrom().getY() > e.getTo().getY()) {
			data.setFallDistance(data.getFallDistance() + distance);
		} else {
			data.setFallDistance(0);
		}

		if(onGround) {
			data.setGroundTicks(data.getGroundTicks() + 1);
			data.setAirTicks(0);
		} else {
			data.setAirTicks(data.getAirTicks() + 1);
			data.setGroundTicks(0);
		}

		if(PlayerUtil.isOnGround(p.getLocation().add(0, 2, 0))) {
			data.setAboveBlockTicks(data.getAboveBlockTicks() + 2);
		} else if(data.getAboveBlockTicks() > 0) {
			data.setAboveBlockTicks(data.getAboveBlockTicks() - 1);
		}
		if(PlayerUtil.isInWater(p.getLocation())
				|| PlayerUtil.isInWater(p.getLocation().add(0, 1, 0))) {
			data.setWaterTicks(data.getWaterTicks() + 1);
		} else if(data.getWaterTicks() > 0) {
			data.setWaterTicks(data.getWaterTicks() - 1);
		}
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onVelocity(PlayerVelocityEvent e) {
		final DataPlayer data = AntiCheat.getInstance().getDataManager().getDataPlayer(e.getPlayer());

		if(data == null) {
			return;
		}
		if(e.getVelocity().getY() > -0.078 || e.getVelocity().getY() < -0.08) {
			data.lastVelocityTaken = System.currentTimeMillis();
		}
	}

	public static Map<UUID, Long> getLastMove() {
		return lastMove;
	}

	public static void setLastMove(Map<UUID, Long> lastMove) {
		MoveEvent.lastMove = lastMove;
	}
}
