package mod.arcomit.energyblade.client;

import mod.arcomit.energyblade.Energyblade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import static mods.flammpfeil.slashblade.client.ClientHandler.bakeBlade;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
// 用于自动为拔刀剑类物品处理渲染相关内容
public class BladeRenderHandler {
    @SubscribeEvent
    public static void setModelUser(final FMLClientSetupEvent event) {
        Energyblade.ITEMS.getEntries().forEach(blade -> {
            if (blade.get() instanceof ItemSlashBlade) {
                ItemProperties.register(blade.get(), new ResourceLocation("slashblade:user"),
                        (stack, level, entity, seed) -> {
                            BladeModel.user = entity;
                            return 0;
                        }
                );
            }
        });
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
