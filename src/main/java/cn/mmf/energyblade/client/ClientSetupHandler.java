package cn.mmf.energyblade.client;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import static mods.flammpfeil.slashblade.client.ClientHandler.bakeBlade;

import cn.mmf.energyblade.Energyblade;

@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientSetupHandler {
	@SubscribeEvent
	public static void setModelUser(final FMLClientSetupEvent event) {
		Energyblade.ITEMS.getEntries().forEach(blade -> {
			if (blade.get() instanceof ItemSlashBlade) {
				ItemProperties.register(blade.get(), SlashBlade.prefix("user"), (stack, level, entity, seed) -> {
					BladeModel.user = entity;
					return 0;
				});
			}
		});
	}
	
    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(InputHandler.KEY_CHARGE);
    }


	@SubscribeEvent()
	public static void baked(final ModelEvent.ModifyBakingResult event) {
		Energyblade.ITEMS.getEntries().forEach(blade -> {
			if (blade.get() instanceof ItemSlashBlade) {
				bakeBlade(blade.get(), event);
			}
		});
	}
}
