package cn.mmf.energyblade.item;

import com.mojang.blaze3d.platform.InputConstants;

import cn.mmf.energyblade.Energyblade;
import cn.mmf.energyblade.energy.FEBladeStorage;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

// 拓展子类拔刀剑(extends ItemSlashBlade)
@EventBusSubscriber(modid = Energyblade.MODID)
public class ItemFEBlade extends ItemSlashBlade {

	public ItemFEBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
		super(tier, attackDamageIn, attackSpeedIn, builder);
	}

	@Override
	public boolean isDamageable(ItemStack stack) {
		if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage energy
				&& energy.isEnergyDurability()) {
			return false;
		}
		return super.isDamageable(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, @NotNull List<Component> tooltip,
			@NotNull TooltipFlag flagIn) {
		// TODO Auto-generated method stub
		super.appendHoverText(stack, context, tooltip, flagIn);
		this.appendForgeEnergyInfo(stack, tooltip);
	}

	// 能量显示
	@OnlyIn(Dist.CLIENT)
	public void appendForgeEnergyInfo(ItemStack stack, List<Component> tooltip) {
		if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage energy) {
					Minecraft mc = Minecraft.getInstance();
					KeyMapping shift = mc.options.keyShift;
					if (!isShiftKeyDown()) {
						// 动态实时显示具体按键
						Component shiftKey = Component.literal("[").append(shift.getTranslatedKeyMessage().copy())
								.append("]").withStyle(ChatFormatting.GOLD);
						tooltip.add(Component.translatable("tip.energyblade.energy_info", shiftKey)
								.withStyle(ChatFormatting.GRAY));
					} else {
						Component energyTip = Component
								.literal(energy.getEnergyStored() + " / " + energy.getMaxEnergyStored())
								.withStyle(ChatFormatting.GOLD);
						tooltip.add(Component.translatable("tip.energyblade.forge_energy_info", energyTip)
						.withStyle(ChatFormatting.GRAY));
				}
		}
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage energy) {
			return isShiftKeyDown() || energy.isPowered();
		}
		return false;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage energy
				&& energy.getMaxEnergyStored() > 0) {
			double ratio = (double) energy.getEnergyStored() / energy.getMaxEnergyStored();
			return (int) (ratio * MAX_BAR_WIDTH);
		}
		return 0;
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0xFFAA00; // 金色能量条，避免与原本耐久条混淆
	}

	private boolean isShiftKeyDown() {
		Minecraft mc = Minecraft.getInstance();
		KeyMapping shift = mc.options.keyShift;
		InputConstants.Key key = shift.getKey();
		long window = mc.getWindow().getWindow();
		if (key.getType() == InputConstants.Type.KEYSYM) {
			return GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
		} else if (key.getType() == InputConstants.Type.MOUSE) {
			return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
		}
		return false;
	}

	@SubscribeEvent
	public static void onSlashBladeUpdate(SlashBladeEvent.UpdateEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		LivingEntity living = (LivingEntity) event.getEntity();
		if (!event.isSelected()) {
			return;
		}

		IEnergyStorage energyStorage = event.getBlade().getCapability(Capabilities.EnergyStorage.ITEM);
		if (energyStorage instanceof FEBladeStorage bladeFE) {
			if (bladeFE.isPowered()) {
				if (bladeFE.extractEnergy(bladeFE.getStandbyExtract(), true) == bladeFE.getStandbyExtract()) {
					bladeFE.extractEnergy(bladeFE.getStandbyExtract(), false);
					var rank = living.getData(CapabilityConcentrationRank.RANK_POINT.get());
					if (rank != null) {
						rank.addRankPoint(living, rank.getMaxCapacity());
					}
				} else {
					bladeFE.setPowered(false);
					event.getEntity().playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F, 1F);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
		IEnergyStorage energyStorage = event.getBlade().getCapability(Capabilities.EnergyStorage.ITEM);
		if (energyStorage instanceof FEBladeStorage bladeFE) {
			if (bladeFE.isPowered()) {
				if (bladeFE.extractEnergy(bladeFE.getStandbyExtract(), true) == bladeFE.getStandbyExtract()) {
					bladeFE.extractEnergy(bladeFE.getStandbyExtract(), false);
				} else {
					bladeFE.setPowered(false);
					event.getUser().playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F, 1F);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onSlashBladePowered(SlashBladeEvent.PowerBladeEvent event) {
		IEnergyStorage energyStorage = event.getBlade().getCapability(Capabilities.EnergyStorage.ITEM);
		if (energyStorage instanceof FEBladeStorage bladeFE) {
			if (bladeFE.isPowered()) {
				event.setPowered(true);
			}
		}
	}
}
