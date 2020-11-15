package the_fireplace.clans.networking;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public final class SynchronizedMessageQueue {
    private static final Map<ICommandSender, SynchronizedMessageQueue> MESSAGE_QUEUES = new ConcurrentHashMap<>();

    private static SynchronizedMessageQueue getOrCreateQueue(ICommandSender messageTarget) {
        MESSAGE_QUEUES.computeIfAbsent(messageTarget, SynchronizedMessageQueue::new);
        return MESSAGE_QUEUES.get(messageTarget);
    }

    public static void queueMessages(ICommandSender messageTarget, ITextComponent... messages) {
        getOrCreateQueue(messageTarget).queueMessages(messages);
    }

    private final Queue<ITextComponent> messages = new ArrayDeque<>();
    private final ICommandSender messageTarget;
    private boolean sendingMessages = false;

    private SynchronizedMessageQueue(ICommandSender messageTarget) {
        this.messageTarget = messageTarget;
    }

    private synchronized void queueMessages(ITextComponent... messages) {
        this.messages.addAll(Arrays.asList(messages));
        if (!sendingMessages)
            ConcurrentExecutionManager.runKillable(this::sendMessages);
    }

    private synchronized void sendMessages() {
        sendingMessages = true;
        while (!messages.isEmpty()) {
            messageTarget.sendMessage(messages.remove());
        }
        sendingMessages = false;
    }
}
