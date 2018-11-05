package me.rida.anticheat.checks.movement;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.utils.Color;
import me.rida.anticheat.utils.SetBackSystem;
import me.rida.anticheat.utils.UtilCheat;
import me.rida.anticheat.utils.UtilVelocity;
import me.rida.anticheat.utils.needscleanup.UtilsB;
import me.rida.anticheat.utils.needscleanup.ExtraUtils;

public class Spider extends Check {

	public Spider(AntiCheat AntiCheat) {
		super("Spider", "Spider (WallClimb)", AntiCheat);

		this.setEnabled(true);
		this.setBannable(false);
		setViolationResetTime(1000);
		setViolationsToNotify(2);
		setMaxViolations(5);
	}

    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();
    	double OffSet = event.getFrom().getY() - event.getTo().getY();
		if (OffSet <= 0.0 || OffSet > 0.16) {
			
		}
    }
	private Map<UUID, Map.Entry<Long, Double>> AscensionTicks = new HashMap<UUID, Map.Entry<Long, Double>>();
	@EventHandler(ignoreCancelled = true)
	public void CheckSpider(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		
        if (player.getGameMode().equals(GameMode.CREATIVE)
                || player.getAllowFlight()
                || event.getPlayer().getVehicle() != null
                || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SPONGE
                || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SLIME_BLOCK
                || UtilsB.isOnClimbable(player, 0)
                || UtilsB.isOnClimbable(player, 1) 
				|| !getAntiCheat().isEnabled()
				|| (UtilsB.isNearLiquid(player) && UtilsB.isNearHalfBlock(player))
				|| ExtraUtils.isNotSpider(player)
				|| getAntiCheat().isSotwMode()
                || UtilVelocity.didTakeVelocity(player)) return;
		

		long Time = System.currentTimeMillis();
		double TotalBlocks = 0.0D;
		if (this.AscensionTicks.containsKey(player.getUniqueId())) {
			Time = AscensionTicks.get(player.getUniqueId()).getKey().longValue();
			TotalBlocks = AscensionTicks.get(player.getUniqueId()).getValue().doubleValue();
		}
		long MS = System.currentTimeMillis() - Time;
        double OffsetY = UtilsB.offset(UtilsB.getVerticalVector(event.getFrom().toVector()), UtilsB.getVerticalVector(event.getTo().toVector()));

		boolean ya = false;
		List<Material> Types = new ArrayList<Material>();
		Types.add(player.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType());
		Types.add(player.getLocation().getBlock().getRelative(BlockFace.NORTH).getType());
		Types.add(player.getLocation().getBlock().getRelative(BlockFace.WEST).getType());
		Types.add(player.getLocation().getBlock().getRelative(BlockFace.EAST).getType());
		for (Material Type : Types) {
			if ((Type.isSolid()) && (Type != Material.LADDER) && (Type != Material.VINE) && (Type != Material.AIR)) {
				ya = true;
				break;
			}
		}
		if (OffsetY > 0.0D) {
			TotalBlocks += OffsetY;
		} else if ((!ya) || (!UtilCheat.blocksNear(player))) {
			TotalBlocks = 0.0D;
		} else if (((event.getFrom().getY() > event.getTo().getY()) || (UtilsB.isOnGround(player)))) {
			TotalBlocks = 0.0D;
		}
		double Limit = 0.5D;
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				if (effect.getType().equals(PotionEffectType.JUMP)) {
					int level = effect.getAmplifier() + 1;
					Limit += Math.pow(level + 4.2D, 2.0D) / 16.0D;
					break;
				}
			}
		}
		if ((ya) && (TotalBlocks > Limit)) {
			if (MS > 500L) {
				getAntiCheat().logCheat(this, player, Color.Red + "False flag if the player is falling next to a wall!", null);
				Time = System.currentTimeMillis();
				if (TotalBlocks > 2) {
					SetBackSystem.setBack(player);
				}
			}
		} else {
			Time = System.currentTimeMillis();
		}
		this.AscensionTicks.put(player.getUniqueId(), new AbstractMap.SimpleEntry<>(Time, TotalBlocks));
	}

}