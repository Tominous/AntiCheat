package me.rida.anticheat.checks.other;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.rida.anticheat.AntiCheat;
import me.rida.anticheat.checks.Check;
import me.rida.anticheat.checks.CheckType;

public class InvMoveC extends Check {
	public InvMoveC(AntiCheat AntiCheat) {
		super("InvMoveC", "InvMove", CheckType.Other, true, false, false, false, true, 15, 1, 600000L, AntiCheat);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void InventoryClickEvent(InventoryClickEvent e) {
		final Player p = (Player) e.getWhoClicked();
		if (getAntiCheat().getLag().getTPS() < getAntiCheat().getTPSCancel()
				|| getAntiCheat().getLag().getPing(p) > getAntiCheat().getPingCancel()
				|| p.getAllowFlight()
				|| p.getGameMode().equals(GameMode.CREATIVE)) {
			return;
		}
		if (p.isSprinting()) {
			getAntiCheat().logCheat(this, p, "Sprinting while having a gui open!", "(Type: C)");
		}
	}
}