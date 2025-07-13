package cn.mmf.energyblade.compat;

import cn.mmf.energyblade.Energyblade;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEICompat implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Energyblade.MODID, Energyblade.MODID);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(Energyblade.FORGE_ENERGY_BLADE.get(), (stack, context) -> {
			// 同步nbt到Cap
			stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(cap -> {
				cap.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"));
			});

			return stack.getCapability(ItemSlashBlade.BLADESTATE).map(cap -> cap.getTranslationKey()).orElse("");
		});
	}

}
