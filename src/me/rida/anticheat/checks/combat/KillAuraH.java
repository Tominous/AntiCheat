package me.rida.anticheat.checks.combat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.checks.CheckType;

public class KillAuraH extends Check {
	public static Map<Player, Map.Entry<Integer, Long>> lastAttack;
	public KillAuraH(AntiCheat AntiCheat) {
		super("KillAuraH", "KillAura",  CheckType.Combat, true, false, false, false, true, 7, 1, 600000L, AntiCheat);
		lastAttack = new HashMap<>();
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void Damage(EntityDamageByEntityEvent e) {
		if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
				|| !((e.getEntity()) instanceof Player)
				|| !(e.getDamager() instanceof Player)) {
			return;
		}
		final Player p = (Player) e.getDamager();
		if (p == null) {
			return;
		}
		if (getAntiCheat().getLag().getTPS() < getAntiCheat().getTPSCancel()
				|| getAntiCheat().getLag().getPing(p) > getAntiCheat().getPingCancel()) {
			return;
		}
		if (lastAttack.containsKey(p)) {
			final Integer entityid = lastAttack.get(p).getKey();
			final Long time = lastAttack.get(p).getValue();
			if (entityid != e.getEntity().getEntityId() && System.currentTimeMillis() - time < 6L) {
				this.getAntiCheat().logCheat(this, p, "MultiAura", "(Type: H)");
			}
			lastAttack.remove(p);
		} else {
			lastAttack.put(p, new AbstractMap.SimpleEntry<>(e.getEntity().getEntityId(),
					System.currentTimeMillis()));
		}
	}
}