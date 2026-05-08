package cn.mmf.energyblade.energy;

import cn.mmf.energyblade.Energyblade;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.minecraft.world.item.ItemStack;

public class FEBladeStorage implements IEnergyStorage {
    private final ItemStack stack;

    public FEBladeStorage(ItemStack stack) {
        this.stack = stack;
    }

    private Energyblade.EnergyBladeData getData() {
        return stack.getOrDefault(Energyblade.ENERGY_BLADE_DATA.get(), Energyblade.EnergyBladeData.DEFAULT);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive()) return 0;
        Energyblade.EnergyBladeData data = getData();
        int energyReceived = Math.min(data.capacity() - data.energy(), Math.min(data.maxReceive(), toReceive));
        if (energyReceived > 0 && !simulate) {
            int newEnergy = data.energy() + energyReceived;
            stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                    newEnergy, data.capacity(), data.maxReceive(), data.maxExtract(),
                    data.powerupExtract(), data.standbyExtract(), data.energyDurability(), data.isPowered()));
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract()) return 0;
        Energyblade.EnergyBladeData data = getData();
        int energyExtracted = Math.min(data.energy(), Math.min(data.maxExtract(), toExtract));
        if (energyExtracted > 0 && !simulate) {
            int newEnergy = data.energy() - energyExtracted;
            boolean powered = newEnergy > 0 && data.isPowered();
            stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                    newEnergy, data.capacity(), data.maxReceive(), data.maxExtract(),
                    data.powerupExtract(), data.standbyExtract(), data.energyDurability(), powered));
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return getData().energy();
    }

    @Override
    public int getMaxEnergyStored() {
        return getData().capacity();
    }

    @Override
    public boolean canReceive() {
        return getData().maxReceive() > 0;
    }

    @Override
    public boolean canExtract() {
        return getData().maxExtract() > 0;
    }

    public boolean isPowered() {
        return getData().isPowered();
    }

    public void setPowered(boolean powered) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), data.capacity(), data.maxReceive(), data.maxExtract(),
                data.powerupExtract(), data.standbyExtract(), data.energyDurability(), powered));
    }

    public void setMaxEnergyStored(int capacity) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), capacity, data.maxReceive(), data.maxExtract(),
                data.powerupExtract(), data.standbyExtract(), data.energyDurability(), data.isPowered()));
    }

    public void setPowerupExtract(int powerupExtract) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), data.capacity(), data.maxReceive(), data.maxExtract(),
                powerupExtract, data.standbyExtract(), data.energyDurability(), data.isPowered()));
    }

    public int getPowerupExtract() {
        return getData().powerupExtract();
    }

    public int getStandbyExtract() {
        return getData().standbyExtract();
    }

    public void setStandbyExtract(int standbyExtract) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), data.capacity(), data.maxReceive(), data.maxExtract(),
                data.powerupExtract(), standbyExtract, data.energyDurability(), data.isPowered()));
    }

    public boolean isEnergyDurability() {
        return getData().energyDurability();
    }
}
