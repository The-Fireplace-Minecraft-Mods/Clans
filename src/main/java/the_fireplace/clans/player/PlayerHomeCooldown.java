package the_fireplace.clans.player;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHomeCooldown
{
    private static final Map<UUID, Integer> COOLDOWN_TIMERS = new ConcurrentHashMap<>();
    private static Timer timer = new Timer();
    private static boolean timerRunning = false;

    public static int getCooldown(UUID player) {
        return COOLDOWN_TIMERS.getOrDefault(player, 0);
    }

    public static boolean isCoolingDown(UUID player) {
        return COOLDOWN_TIMERS.containsKey(player);
    }

    public static void setCooldown(UUID player, int cooldown) {
        COOLDOWN_TIMERS.put(player, cooldown);
        if (!timerRunning) {
            startTimer();
        }
    }

    private static void startTimer() {
        timerRunning = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run() {
                decrementCooldownTimers();
                removeExpiredTimers();
                stopTimerIfUnused();
            }
        }, 0, 1000);
    }

    private static void decrementCooldownTimers() {
        COOLDOWN_TIMERS.replaceAll((k, v) -> v - 1);
    }

    private static void removeExpiredTimers() {
        for (Map.Entry<UUID, Integer> cooldownEntry : COOLDOWN_TIMERS.entrySet()) {
            if (cooldownEntry.getValue() < 1) {
                COOLDOWN_TIMERS.remove(cooldownEntry.getKey());
            }
        }
    }

    private static void stopTimerIfUnused() {
        if (timerRunning && COOLDOWN_TIMERS.isEmpty()) {
            timer.cancel();
            timerRunning = false;
        }
    }
}
