package cn.mmf.energyblade;

import cn.mmf.energyblade.item.ItemFEBlade;
import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.item.ItemTierSlashBlade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(Energyblade.MODID)
public class Energyblade {
    public static final String MODID = "energyblade";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    public static final Supplier<Item> FORGE_ENERGY_BLADE = ITEMS.register("forge_energy_blade",
            () -> new ItemFEBlade(new ItemTierSlashBlade(40, 4F), 4, -2.4F, (new Item.Properties())));

    public Energyblade(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkPacketHandler::registerMessage);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
