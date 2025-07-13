package cn.mmf.energyblade.energy;

import mods.flammpfeil.slashblade.capability.slashblade.NamedBladeStateCapabilityProvider;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class FECapabilityProvider extends NamedBladeStateCapabilityProvider {
	private final LazyOptional<IEnergyStorage> lazyOptional;
	private final FEBladeStorage energyStorage;

	public FECapabilityProvider(ItemStack stack, int energy, int capacity, int powerupExtract, int standbyExtract,
			boolean energyDurability) {
		super(stack);
		this.energyStorage = new FEBladeStorage(energy, capacity, powerupExtract, standbyExtract, energyDurability);
		this.lazyOptional = LazyOptional.of(() -> energyStorage);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ENERGY) {
			return lazyOptional.cast();
		}
		if (cap == ItemSlashBlade.BLADESTATE) {
			return super.getCapability(cap, side);
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = super.serializeNBT();
		tag.put("Energy", energyStorage.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		if (nbt.contains("Energy")) {
			energyStorage.deserializeNBT(nbt.getCompound("Energy"));
		}
	}
}