package mod.arcomit.energyblade.item;

import com.mojang.blaze3d.platform.InputConstants;
import mod.arcomit.energyblade.client.render.EnergyBladeBEWLR;
import mod.arcomit.energyblade.energy.FEBladeStorage;
import mod.arcomit.energyblade.energy.FECapabilityProvider;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

// 拓展子类拔刀剑(extends ItemSlashBlade)
public class ItemFEBlade extends ItemSlashBlade {

    public ItemFEBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .filter(FEBladeStorage.class::isInstance)
                .map(FEBladeStorage.class::cast)
                .filter(FEBladeStorage::isEnergyDurability) // 当启用能量代替耐久时
                .map(energy -> false)                       // 禁用原版耐久机制
                .orElseGet(() -> super.isDamageable(stack)); // 否则继承默认逻辑
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        this.appendForgeEnergyInfo(stack, tooltip);
    }

    //能量显示
    @OnlyIn(Dist.CLIENT)
    public void appendForgeEnergyInfo(ItemStack stack, List<Component> tooltip) {
        stack.getCapability(ForgeCapabilities.ENERGY)
                .filter(FEBladeStorage.class::isInstance)
                .map(FEBladeStorage.class::cast)
                .ifPresent(energy -> {
                    Minecraft mc = Minecraft.getInstance();
                    KeyMapping shift = mc.options.keyShift;
                    if (!isShiftKeyDown()) {
                        //动态实时显示具体按键
                        Component shiftKey = Component.literal("[")
                                .append(shift.getTranslatedKeyMessage().copy())
                                .append("]")
                                .withStyle(ChatFormatting.GOLD);
                        tooltip.add(Component.translatable("tip.energyblade.energy_info", shiftKey)
                                .withStyle(ChatFormatting.GRAY));
                    }else {
                        Component energyTip = Component.literal(energy.getEnergyStored() + " / " + energy.getMaxEnergyStored()).withStyle(ChatFormatting.GOLD);
                        tooltip.add(Component.translatable("tip.energyblade.forge_energy_info",energyTip)
                                .withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable("tip.energyblade.energy_expenditure_mode_info")
                                .withStyle(ChatFormatting.GRAY));
                    }
                        }
                );
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .filter(FEBladeStorage.class::isInstance)
                .map(FEBladeStorage.class::cast)
                .map(energy -> isShiftKeyDown())
                .orElse(false);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .filter(FEBladeStorage.class::isInstance)
                .map(FEBladeStorage.class::cast)
                .filter(energy -> energy.getMaxEnergyStored() > 0) // 防止除以零
                .map(energy -> {
                    double ratio = (double) energy.getEnergyStored() / energy.getMaxEnergyStored();
                    return (int) (ratio * MAX_BAR_WIDTH);
                })
                .orElse(0); // 无能量存储时返回0
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
        return new FECapabilityProvider(stack,10, 100, 1, 1, true);
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
}
