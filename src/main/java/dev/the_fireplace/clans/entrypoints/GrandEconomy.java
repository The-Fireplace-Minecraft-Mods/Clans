package dev.the_fireplace.clans.entrypoints;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.economy.GrandEconomyWrapper;
import dev.the_fireplace.grandeconomy.api.entrypoints.GrandEconomyEntrypoint;
import dev.the_fireplace.grandeconomy.api.injectables.EconomyRegistry;

public final class GrandEconomy implements GrandEconomyEntrypoint
{
    @Override
    public void init(EconomyRegistry economyRegistry) {
        Economy.setExternalEconomy(DIContainer.get().getInstance(GrandEconomyWrapper.class));
    }
}
