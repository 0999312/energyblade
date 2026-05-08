package cn.mmf.energyblade;

import cn.mmf.energyblade.energy.FEBladeStorage;
import cn.mmf.energyblade.item.ItemFEBlade;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mods.flammpfeil.slashblade.item.ItemTierSlashBlade;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
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

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public record EnergyBladeData(int energy, int capacity, int maxReceive, int maxExtract,
            int powerupExtract, int standbyExtract, boolean energyDurability, boolean isPowered) {
        public static final EnergyBladeData DEFAULT = new EnergyBladeData(0, 2000000, 20000, 20000, 1000, 100, false, false);

        public static final Codec<EnergyBladeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("energy").forGetter(EnergyBladeData::energy),
                Codec.INT.fieldOf("capacity").forGetter(EnergyBladeData::capacity),
                Codec.INT.fieldOf("maxReceive").forGetter(EnergyBladeData::maxReceive),
                Codec.INT.fieldOf("maxExtract").forGetter(EnergyBladeData::maxExtract),
                Codec.INT.fieldOf("powerupExtract").forGetter(EnergyBladeData::powerupExtract),
                Codec.INT.fieldOf("standbyExtract").forGetter(EnergyBladeData::standbyExtract),
                Codec.BOOL.fieldOf("energyDurability").forGetter(EnergyBladeData::energyDurability),
                Codec.BOOL.fieldOf("isPowered").forGetter(EnergyBladeData::isPowered)
        ).apply(instance, EnergyBladeData::new));

        public static final StreamCodec<ByteBuf, EnergyBladeData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, EnergyBladeData::energy,
                ByteBufCodecs.INT, EnergyBladeData::capacity,
                ByteBufCodecs.INT, EnergyBladeData::maxReceive,
                ByteBufCodecs.INT, EnergyBladeData::maxExtract,
                ByteBufCodecs.INT, EnergyBladeData::powerupExtract,
                ByteBufCodecs.INT, EnergyBladeData::standbyExtract,
                ByteBufCodecs.BOOL, EnergyBladeData::energyDurability,
                ByteBufCodecs.BOOL, EnergyBladeData::isPowered,
                EnergyBladeData::new
        );
    }

    public static final Supplier<DataComponentType<EnergyBladeData>> ENERGY_BLADE_DATA =
            DATA_COMPONENTS.registerComponentType("energy_blade_data",
                    builder -> builder.persistent(EnergyBladeData.CODEC)
                            .networkSynchronized(EnergyBladeData.STREAM_CODEC));

    public Energyblade(IEventBus modBus) {
        ITEMS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        modBus.addListener(this::setup);
        modBus.addListener(this::registerCapabilities);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkPacketHandler::registerMessage);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> new FEBladeStorage(stack),
                FORGE_ENERGY_BLADE.get()
        );
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
