package the_fireplace.clans.forge.event;

import net.minecraft.entity.monster.IMob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.logic.TimerLogic;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class Timer {
	private static byte ticks = 0;
	private static int minuteCounter = 0;
	private static int fiveMinuteCounter = 0;
	private static boolean executing = false;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
        if(!executing) {
            if(++fiveMinuteCounter >= 20*60*5) {
                executing = true;
                fiveMinuteCounter -= 20*60*5;
                TimerLogic.runFiveMinuteLogic();
                executing = false;
            }
            if(++minuteCounter >= 20*60) {
                executing = true;
                minuteCounter -= 20*60;
                TimerLogic.runOneMinuteLogic();
                executing = false;
            }
            if(++ticks >= 20) {
                executing = true;
                ticks -= 20;
                TimerLogic.runOneSecondLogic();
                executing = false;
            }
        }
	}

    @SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(!event.player.getEntityWorld().isRemote) {
			if (event.player.getEntityWorld().getTotalWorldTime() % 20 == 0) {
				TimerLogic.runPlayerSecondLogic(event.player);
			}
			if (event.player.getEntityWorld().getTotalWorldTime() % 10 == 0) {
				TimerLogic.runPlayerHalfSecondLogic(event.player);
			}
		}
	}

	@SubscribeEvent
    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
	    if(event.getEntityLiving() instanceof IMob && event.getEntityLiving().getEntityWorld().getTotalWorldTime() % 100 == 0)
	        TimerLogic.runMobFiveSecondLogic(event.getEntityLiving());
    }
}
