package dev.daboa.mc.forge.summonbyudp;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Summonbyudp.MODID)
public class Summonbyudp {

    private final int PORT = 5565;
    private Thread udpReceiverThread;
    private UDPReceiver udpReceiver;
    private int tickCounter = 0;

    // Define mod id in a common place for everything to reference
    public static final String MODID = "summonbyudp";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Summonbyudp(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");

        udpReceiver = new UDPReceiver(PORT);
        udpReceiverThread = new Thread(udpReceiver);
        udpReceiverThread.start();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {

        LOGGER.info("HELLO from server stopping");

        if (udpReceiver != null) {
            udpReceiver.stop();
            try {
                udpReceiverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            if (tickCounter >= 60) {
                tickCounter = 0;
                if(udpReceiver.getSpawn()) {
                    spawnZombies(event);
                }
            }
        }
    }
    private static void spawnZombies(TickEvent.ServerTickEvent event) {
        for (ServerLevel world : event.getServer().getAllLevels()) {
            world.players().forEach(player -> {
                Vec3 pos = player.position();
                BlockPos spawnPos = new BlockPos((int)pos.x + getRandomOffset(), (int)pos.y, (int)pos.z + getRandomOffset());

                if (world.isLoaded(spawnPos)) {
                    Zombie zombie = new Zombie(EntityType.ZOMBIE,world);
                    if (zombie != null) {
                        zombie.moveTo(spawnPos, 0.0F, 0.0F);
                        world.addFreshEntity(zombie);
                    }
                }
            });
        }
    }
    private static int getRandomOffset() {
        return (int) (Math.random() * 10) - 5; // -5～+5の範囲でランダムにオフセット
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
