package cn.mmf.energyblade.compat;

import cn.mmf.energyblade.Energyblade;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import mods.flammpfeil.slashblade.compat.jei.SlashBladeSubtypeInterpreter;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEICompat implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, Energyblade.MODID);
	}

	@Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE.get(), SlashBladeSubtypeInterpreter.INSTANCE);
    }

}
