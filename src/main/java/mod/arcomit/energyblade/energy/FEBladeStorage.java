package mod.arcomit.energyblade.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

@AutoRegisterCapability//用于自动注册能力(capbility)
public class FEBladeStorage implements IEnergyStorage, INBTSerializable<CompoundTag> {
    //当前存储的能量
    protected int energy;
    //最大可存储的能量
    protected int capacity;

    //每tick最大可接收的能量
    protected int maxReceive;

    //每tick最大可提取的能量
    protected int maxExtract;

    //能量替换耐久显示(即无耐久设定)
    protected boolean energyDurability;

    public FEBladeStorage(int energy, int capacity, int maxReceive, int maxExtract, boolean energyDurability) {
        this.energy = energy;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energyDurability = energyDurability;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate)
            energy += energyReceived;
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate)
            energy -= energyExtracted;
        return energyExtracted;
    }

    @Override
    public int getEnergyStored()
    {
        return energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return capacity;
    }

    @Override
    public boolean canReceive()
    {
        return this.maxReceive > 0;
    }

    @Override
    public boolean canExtract()
    {
        return this.maxExtract > 0;
    }

    public boolean isEnergyDurability()
    {
        return energyDurability;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Energy", energy);
        tag.putInt("Capacity", capacity);
        tag.putInt("MaxReceive", maxReceive);
        tag.putInt("MaxExtract", maxExtract);
        tag.putBoolean("EnergyDurability", energyDurability);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt != null) {
            energy = nbt.getInt("Energy");
            capacity = nbt.getInt("Capacity");
            maxReceive = nbt.getInt("MaxReceive");
            maxExtract = nbt.getInt("MaxExtract");
            energyDurability = nbt.getBoolean("EnergyDurability");
        }
    }
}
