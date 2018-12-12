package me.rida.anticheat.checks.combat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.checks.CheckType;
import me.rida.anticheat.utils.PlayerUtil;
import me.rida.anticheat.utils.ServerUtil;
import me.rida.anticheat.utils.TimeUtil;

public class RegenA extends Check {
	public RegenA(AntiCheat AntiCheat) {
		super("RegenA", "Regen",  CheckType.Combat, true, true, false, true, 12, 1, 600000L, AntiCheat);
	}

	public static Map<UUID, Long> LastHeal = new HashMap<UUID, Long>();
	public static Map<UUID, Map.Entry<Integer, Long>> FastHealTicks = new HashMap<UUID, Map.Entry<Integer, Long>>();

	private boolean checkFastHeal(Player p) {
		if (LastHeal.containsKey(p.getUniqueId())) {
			long l = LastHeal.get(p.getUniqueId()).longValue();
			LastHeal.remove(p.getUniqueId());
			if (System.currentTimeMillis() - l < 3000L) {
				return true;
			}
		}
		return false;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onHeal(EntityRegainHealthEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getEntity();
		if (!e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)
				|| !(p instanceof Player)
				|| p.getWorld().getDifficulty().equals(Difficulty.PEACEFUL)
				|| ServerUtil.isBukkitVerison("1_13")
				|| getAntiCheat().getLag().getTPS() < getAntiCheat().getTPSCancel()
				|| getAntiCheat().getLag().getTPS() < getAntiCheat().getTPSCancel()
				|| getAntiCheat().getLag().getPing(p) > getAntiCheat().getPingCancel()) {
			return;
		}
		int Count = 0;
		long Time = System.currentTimeMillis();
		if (FastHealTicks.containsKey(p.getUniqueId())) {
			Count = FastHealTicks.get(p.getUniqueId()).getKey().intValue();
			Time = FastHealTicks.get(p.getUniqueId()).getValue().longValue();
		}
		if (checkFastHeal(p) && !PlayerUtil.isFullyStuck(p) && !PlayerUtil.isPartiallyStuck(p)) {
			Count++;
		} else {
			Count = Count > 0 ? Count - 1 : Count;
		}

		if(Count > 2) {
			getAntiCheat().logCheat(this, p, null, "(Type: A)");
		}
		if (FastHealTicks.containsKey(p.getUniqueId()) && TimeUtil.elapsed(Time, 60000L)) {
			Count = 0;
			Time = TimeUtil.nowlong();
		}
		LastHeal.put(p.getUniqueId(), Long.valueOf(System.currentTimeMillis()));
		FastHealTicks.put(p.getUniqueId(),
				new AbstractMap.SimpleEntry<Integer, Long>(Count, Time));
	}
}