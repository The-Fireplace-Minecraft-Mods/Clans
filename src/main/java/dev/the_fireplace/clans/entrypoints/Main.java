package dev.the_fireplace.clans.entrypoints;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModInitializer;
import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;

public final class Main implements DIModInitializer
{
    @Override
    public void onInitialize(Injector diContainer) {
        diContainer.getInstance(TranslatorFactory.class).addTranslator(ClansConstants.MODID);
    }
}
