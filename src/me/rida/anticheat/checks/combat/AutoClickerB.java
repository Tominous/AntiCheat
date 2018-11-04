package me.rida.anticheat.checks.combat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.wrappers.EnumWrappers;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.packets.events.PacketUseEntityEvent;
import me.rida.anticheat.utils.UtilTime;

public class AutoClickerB extends Check {

	public static Map<UUID, Long> LastMS;
	public static Map<UUID, List<Long>> Clicks;
	public static Map<UUID, Map.Entry<Integer, Long>> ClickTicks;

	public AutoClickerB(AntiCheat AntiCheat) {
		super("AutoClickerB", "AutoClicker (Type: B)", AntiCheat);

		LastMS = new HashMap<UUID, Long>();
		Clicks = new HashMap<UUID, List<Long>>();
		ClickTicks = new HashMap<UUID, Map.Entry<Integer, Long>>();
		setViolationsToNotify(2);
		setEnabled(true);
		setBannable(false);
		setMaxViolations(5);
	}

	@EventHandler
	public void onLog(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();

		if (LastMS.containsKey(uuid)) {
			LastMS.remove(uuid);
		}
		if (Clicks.containsKey(uuid)) {
			Clicks.remove(uuid);
		}
		if (ClickTicks.containsKey(uuid)) {
			Clicks.remove(uuid);
		}
	}

	@EventHandler
	public void UseEntity(PacketUseEntityEvent e) {
		if (e.getAction() != EnumWrappers.EntityUseAction.ATTACK
				|| !((e.getAttacked()) instanceof Player)) {
			return;
		}
		
        Player damager = e.getAttacker();
		int Count = 0;
		long Time = System.currentTimeMillis();
		if (ClickTicks.containsKey(damager.getUniqueId())) {
			Count = ClickTicks.get(damager.getUniqueId()).getKey();
			Time = ClickTicks.get(damager.getUniqueId()).getValue();
		}
		if (LastMS.containsKey(damager.getUniqueId())) {
			long MS = UtilTime.nowlong() - LastMS.get(damager.getUniqueId());
			if (MS > 500L || MS < 5L) {
				LastMS.put(damager.getUniqueId(), UtilTime.nowlong());
				return;
			}
			if (Clicks.containsKey(damager.getUniqueId())) {
				List<Long> Clicks = this.Clicks.get(damager.getUniqueId());
				if (Clicks.size() == 3) {
					this.Clicks.remove(damager.getUniqueId());
					Collections.sort(Clicks);
					long Range = Clicks.get(Clicks.size() - 1) - Clicks.get(0);

					if (Range >= 0 && Range <= 2) {
						++Count;
						Time = System.currentTimeMillis();
						this.dumplog(damager,
								"New Count: " + Count + "; Range: " + Range + "; Ping: "
										+ getAntiCheat().getLag().getPing(damager) + "; TPS: "
										+ getAntiCheat().getLag().getTPS());
					}
				} else {
					Clicks.add(MS);
					this.Clicks.put(damager.getUniqueId(), Clicks);
				}
			} else {
				List<Long> Clicks = new ArrayList<Long>();
				Clicks.add(MS);
				this.Clicks.put(damager.getUniqueId(), Clicks);
			}
		}
		if (ClickTicks.containsKey(damager.getUniqueId()) && UtilTime.elapsed(Time, 5000L)) {
			Count = 0;
			Time = UtilTime.nowlong();
		}
		if ((Count > 4 && this.getAntiCheat().getLag().getPing(damager) < 100)
				|| (Count > 6 && this.getAntiCheat().getLag().getPing(damager) < 200)) {
			this.dumplog(damager, "Logged. Count: " + Count);
			Count = 0;
			this.getAntiCheat().logCheat(this, damager, "Continuous/Repeating Patterns", null);
			ClickTicks.remove(damager.getUniqueId());
		} else if (this.getAntiCheat().getLag().getPing(damager) > 250) {
			this.dumplog(damager, "Would set off Autoclicker (Constant) but latency is too high!");
		}
		LastMS.put(damager.getUniqueId(), UtilTime.nowlong());
		ClickTicks.put(damager.getUniqueId(), new AbstractMap.SimpleEntry<Integer, Long>(Count, Time));
	}

}