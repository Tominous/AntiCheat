package me.rida.anticheat.checks.movement;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.checks.CheckType;
import me.rida.anticheat.utils.MathUtil;
import me.rida.anticheat.utils.PlayerUtil;
import me.rida.anticheat.utils.ServerUtil;

public class FastLadderA extends Check {

	public static Map<Player, Integer> count;

	public FastLadderA(AntiCheat AntiCheat) {
		super("FastLadderA", "FastLadder", CheckType.Movement, true, true, false, true, false, 7, 1, 600000L, AntiCheat);
		count = new WeakHashMap<>();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();

		if(e.isCancelled()
				|| (e.getFrom().getY() == e.getTo().getY())
				|| p.getAllowFlight()
				|| getAntiCheat().getLastVelocity().containsKey(p.getUniqueId())
				|| !PlayerUtil.isOnClimbable(p, 1)
				|| !PlayerUtil.isOnClimbable(p, 0)
				|| getAntiCheat().getLag().getTPS() < getAntiCheat().getTPSCancel()
				|| getAntiCheat().getLag().getPing(p) > getAntiCheat().getPingCancel()
				|| PlayerUtil.isNearSlime(e.getFrom())
				|| PlayerUtil.isNearSlime(p)
				|| PlayerUtil.isNearSlime(e.getTo())) {
			return;
		}

		if (!ServerUtil.isBukkitVerison("1_8")
				&&!ServerUtil.isBukkitVerison("1_7")) {
			if (p.hasPotionEffect(PotionEffectType.LEVITATION)) {
				return;
			}
		}
		int Count = count.getOrDefault(p, 0);
		final double OffsetY = MathUtil.offset(MathUtil.getVerticalVector(e.getFrom().toVector()),
				MathUtil.getVerticalVector(e.getTo().toVector()));
		final double Limit = 0.13;

		final double updown = e.getTo().getY() - e.getFrom().getY();
		if (updown <= 0) {
			return;
		}

		if (OffsetY > Limit) {
			Count++;
			this.dumplog(p, "Logged for FastLadder Type A;  New Count: " + Count + " (+1); Speed: " + OffsetY + "; Max: " + Limit);
		} else {
			Count = Count > -2 ? Count - 1 : 0;
		}

		final long percent = Math.round((OffsetY - Limit) * 120);

		if (Count > 11) {
			Count = 0;
			this.dumplog(p,
					"Logged for FastLadder Type A; Speed:" + OffsetY + "; Max: " + Limit + "; New Count: " + Count);
			this.getAntiCheat().logCheat(this, p, percent + "% faster than normal", "(Type: A)");
		}
		count.put(p, Count);
	}
}
