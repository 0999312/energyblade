package cn.mmf.energyblade.item;

import com.mojang.blaze3d.platform.InputConstants;

import cn.mmf.energyblade.client.render.EnergyBladeBEWLR;
import cn.mmf.energyblade.energy.FEBladeStorage;
import cn.mmf.energyblade.energy.FECapabilityProvider;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

// 拓展子类拔刀剑(extends ItemSlashBlade)
@EventBusSubscriber(modid = Energyblade.MODID)
public class ItemFEBlade extends ItemSlashBlade {

	public ItemFEBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
		super(tier, attackDamageIn, attackSpeedIn, builder);
	}

	@Override
	public boolean isDamageable(ItemStack stack) {
		return stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
				.map(FEBladeStorage.class::cast).filter(FEBladeStorage::isEnergyDurability) // 当启用能量代替耐久时
				.map(energy -> false) // 禁用原版耐久机制
				.orElseGet(() -> super.isDamageable(stack)); // 否则继承默认逻辑
	}

	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		var tag = stack.getOrCreateTag();
		stack.getCapability(BLADESTATE).ifPresent(state -> {
			if (!state.isEmpty())
				tag.put("bladeState", state.serializeNBT());
		});
		stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
				.map(FEBladeStorage.class::cast).ifPresent(energy -> {
					tag.put("Energy", energy.serializeNBT());
				});
		return tag;
	}

	@Override
	public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
		if (nbt != null) {
			if (nbt.contains("bladeState"))
				stack.getCapability(BLADESTATE).ifPresent(state -> state.deserializeNBT(nbt.getCompound("bladeState")));
			if (nbt.contains("Energy"))
				stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
						.map(FEBladeStorage.class::cast)
						.ifPresent(energy -> energy.deserializeNBT(nbt.getCompound("Energy")));
		}
		super.readShareTag(stack, nbt);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		this.appendForgeEnergyInfo(stack, tooltip);
	}

	// 能量显示
	@OnlyIn(Dist.CLIENT)
	public void appendForgeEnergyInfo(ItemStack stack, List<Component> tooltip) {
		stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
				.map(FEBladeStorage.class::cast).ifPresent(energy -> {
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
				});
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
				.map(FEBladeStorage.class::cast)
				.map(energy -> 
					isShiftKeyDown() || energy.isPowered()
					)
				.orElse(false);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
				.map(FEBladeStorage.class::cast).filter(energy -> energy.getMaxEnergyStored() > 0) // 防止除以零
				.map(energy -> {
					double ratio = (double) energy.getEnergyStored() / energy.getMaxEnergyStored();
					return (int) (ratio * MAX_BAR_WIDTH);
				}).orElse(0); // 无能量存储时返回0
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0xFFAA00; // 金色能量条，避免与原本耐久条混淆
	}

	// 覆写该方法用以修改拔刀剑渲染(具体参考EnergyBladeBEWLR类)
	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			BlockEntityWithoutLevelRenderer renderer = new EnergyBladeBEWLR(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}

	// 覆写该方法用以修改拔刀剑的能力(具体参考FECapabilityProvider类)
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		super.initCapabilities(stack, nbt);

		return new FECapabilityProvider(stack, 0, 2000000, 1000, 100, false);
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack arg0, int arg1, T arg2, Consumer<T> arg3) {
		// TODO 电量耐久适配消耗
		return super.damageItem(arg0, arg1, arg2, arg3);
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

		event.getBlade().getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
			if (energy instanceof FEBladeStorage bladeFE) {
				if (bladeFE.isPowered()) {
					if (bladeFE.extractEnergy(bladeFE.getStandbyExtract(), true) == bladeFE.getStandbyExtract()) {
						bladeFE.extractEnergy(bladeFE.getStandbyExtract(), false);
						living.getCapability(CapabilityConcentrationRank.RANK_POINT)
	                    .ifPresent(cap->cap.addRankPoint(living, cap.getMaxCapacity()));
					} else {
						bladeFE.setPowered(false);
						event.getEntity().playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F, 1F);
					}
				}
			}
		});
	}
	
	@SubscribeEvent
	public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
		event.getBlade().getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
			if (energy instanceof FEBladeStorage bladeFE) {
				if (bladeFE.isPowered()) {
					if (bladeFE.extractEnergy(bladeFE.getStandbyExtract(), true) == bladeFE.getStandbyExtract()) {
						bladeFE.extractEnergy(bladeFE.getStandbyExtract(), false);
					} else {
						bladeFE.setPowered(false);
						event.getUser().playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F, 1F);
					}
				}
			}
		});
	}

	@SubscribeEvent
	public static void onSlashBladePowered(SlashBladeEvent.PowerBladeEvent event) {
		event.getBlade().getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
			if (energy instanceof FEBladeStorage bladeFE) {
				if (bladeFE.isPowered()) {
					event.setPowered(true);
				}
			}
		});
	}
}
