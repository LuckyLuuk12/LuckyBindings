package nl.kablan.luckybindings.keybinds;

import net.minecraft.client.Minecraft;
import nl.kablan.luckybindings.action.Action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeyBindTickHandler {
    private static final Map<KeyBind, Integer> REPEAT_TIMERS = new HashMap<>();

    public static void onTick(Minecraft client) {
        if (client.player == null) return;

        List<KeyBind> allKeybinds = KeyBindManager.getActiveKeyBinds();

        // Clean up timers for removed keybinds to avoid stale entries.
        Set<KeyBind> activeSet = new HashSet<>(allKeybinds);
        REPEAT_TIMERS.keySet().removeIf(kb -> !activeSet.contains(kb));

        // Tick every action every game tick so tick-based actions (e.g. follow
        // path movement) run exactly once per game tick with no scheduler drift.
        for (KeyBind kb : allKeybinds) {
            if (kb.isEnabled()) {
                for (Action action : kb.getActions()) {
                    action.tick(client);
                }
            }
        }

        // Hold-to-repeat handling.
        // Hold-to-repeat: wait for all currently-running actions to finish before
        // refiring, regardless of sequential/concurrent mode.
        //
        // Sequential mode: actions run in order; isRunning() on the in-progress
        //   action stays true until it completes.
        // Concurrent mode: all actions start simultaneously; actions that have a
        //   background lifecycle (FollowPathAction, HighlightPathAction) keep
        //   isRunning()=true while active, so the timer waits naturally.
        //   Once the path finishes, isRunning() → false, and the timer refires to
        //   recompute a fresh path — perfect for tracking a moving player.
        for (KeyBind kb : allKeybinds) {
            if (kb.isEnabled() && kb.isHoldToRepeat()) {
                if (isKeyHeld(kb)) {
                    if (!REPEAT_TIMERS.containsKey(kb)) {
                        REPEAT_TIMERS.put(kb, Math.max(0, kb.getRepeatDelayTicks()));
                        continue;
                    }

                    int timer = REPEAT_TIMERS.get(kb) - 1;
                    if (timer <= 0) {
                        if (KeyBindManager.isKeyBindExecutionComplete(kb)) {
                            KeyBindManager.executeKeyBind(kb);
                            timer = Math.max(0, kb.getRepeatDelayTicks());
                        } else {
                            timer = 0; // hold at 0 until actions finish
                        }
                    }
                    REPEAT_TIMERS.put(kb, timer);
                } else {
                    REPEAT_TIMERS.remove(kb);
                }
            }
        }
    }

    private static boolean isKeyHeld(KeyBind keyBind) {
        return keyBind != null && KeyInputState.isComboHeld(keyBind.getKey());
    }
}