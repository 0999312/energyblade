package cn.mmf.energyblade;

import cn.mmf.energyblade.energy.FEBladeStorage;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import net.minecraft.world.item.ItemStack;

public class EmpowerSlashBladeEvent extends SlashBladeEvent {
	private final FEBladeStorage FEStorage;
	private final boolean powered;
	public EmpowerSlashBladeEvent(ItemStack blade, ISlashBladeState state, FEBladeStorage FEStorage, boolean isPowered) {
		super(blade, state);
		this.FEStorage = FEStorage;
		this.powered = isPowered;
	}
	
	public boolean isPowered() {
		return powered;
	}
	
	public FEBladeStorage getFEStorage() {
		return FEStorage;
	}

}
