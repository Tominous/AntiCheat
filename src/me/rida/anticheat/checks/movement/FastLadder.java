package me.rida.anticheat.checks.movement;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.utils.needscleanup.UtilsB;

public class FastLadder extends Check {
	
	public Map<Player, Integer> count;

	public FastLadder(AntiCheat AntiCheat) {
		super("FastLadder", "FastLadder", AntiCheat);

		this.setEnabled(true);
		this.setBannable(true);
		this.setMaxViolations(7);
		
		count = new WeakHashMap<Player, Integer>();
	}

	@EventHandler
	public void checkFastLadder(PlayerMoveEvent e) {
		Player player = e.getPlayer();

		/** False flag check **/
		if(e.isCancelled()
				|| (e.getFrom().getY() == e.getTo().getY())
				|| getAntiCheat().isSotwMode()
				|| player.getAllowFlight()
				|| getAntiCheat().getLastVelocity().containsKey(player.getUniqueId())
				|| !UtilsB.isOnClimbable(player, 1) || 
				!UtilsB.isOnClimbable(player, 0)) {
			return;
		}

		int Count = count.getOrDefault(player, 0);
		double OffsetY = UtilsB.offset(UtilsB.getVerticalVector(e.getFrom().toVector()),
				UtilsB.getVerticalVector(e.getTo().toVector()));
		double Limit = 0.13;
		
		double updown = e.getTo().getY() - e.getFrom().getY();
		if (updown <= 0) {
			return;
		}

		
		/** Checks if Y Delta is greater than Limit **/
		
		if (OffsetY > Limit) {
			Count++;
			this.dumplog(player, "[Illegitmate] New Count: " + Count + " (+1); Speed: " + OffsetY + "; Max: " + Limit);
		} else {
			Count = Count > -2 ? Count - 1 : 0;
		}

		long percent = Math.round((OffsetY - Limit) * 120);
		
		/**If verbose count is greater than 11, flag **/
		if (Count > 11) {
			Count = 0;
			this.dumplog(player,
					"Flagged for FastLadder; Speed:" + OffsetY + "; Max: " + Limit + "; New Count: " + Count);
			this.getAntiCheat().logCheat(this, player, percent + "% faster than normal", null);
		}
		count.put(player, Count);
	}

}