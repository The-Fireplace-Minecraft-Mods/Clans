package dev.the_fireplace.clans.config;

import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.clans.config.state.*;
import dev.the_fireplace.clans.domain.config.*;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.chat.interfaces.Translator;
import dev.the_fireplace.lib.api.client.injectables.ConfigScreenBuilderFactory;
import dev.the_fireplace.lib.api.client.interfaces.ConfigScreenBuilder;
import dev.the_fireplace.lib.api.client.interfaces.OptionBuilder;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.math.exception.ParsingException;
import dev.the_fireplace.lib.api.math.injectables.FormulaParserFactory;
import dev.the_fireplace.lib.api.math.interfaces.FormulaParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Singleton
public final class ClansConfigScreenFactory
{
    private static final String TRANSLATION_BASE = "text.config." + ClansConstants.MODID + ".";
    private static final String DYNMAP_TRANSLATION_BASE = TRANSLATION_BASE + "dynmap.";
    private static final String GLOBAL_TRANSLATION_BASE = TRANSLATION_BASE + "clan.";
    private static final String PER_CLAN_TRANSLATION_BASE = TRANSLATION_BASE + "clanDefault.";
    private static final String PER_CLAN_ECONOMICS_TRANSLATION_BASE = TRANSLATION_BASE + "clanDefaultEconomics.";
    private static final String PER_CLAN_PROTECTION_TRANSLATION_BASE = TRANSLATION_BASE + "clanDefaultProtection.";
    private static final String RAID_TRANSLATION_BASE = TRANSLATION_BASE + "raid.";
    private static final String RAID_ECONOMICS_TRANSLATION_BASE = TRANSLATION_BASE + "raidEconomics.";
    private static final String WORLD_PROTECTION_TRANSLATION_BASE = TRANSLATION_BASE + "worldProtection.";

    private static final String ECONOMICS_TRANSLATION_KEY = TRANSLATION_BASE + "economics";
    private static final String FORMULA_SUFFIX_TRANSLATION_KEY = TRANSLATION_BASE + "formula";

    private final Translator translator;
    private final ConfigStateManager configStateManager;
    private final ConfigScreenBuilderFactory configScreenBuilderFactory;
    private final FormulaParserFactory formulaParserFactory;

    private final DynmapConfigState dynmapState;
    private final DynmapConfig dynmapDefaults;
    private final GlobalClanConfigState globalState;
    private final GlobalClanConfig globalDefaults;
    private final PerClanConfigState perClanState;
    private final PerClanConfig perClanDefaults;
    private final PerClanEconomicsConfigState perClanEconomicsState;
    private final PerClanEconomicsConfig perClanEconomicsDefaults;
    private final PerClanProtectionConfigState perClanProtectionState;
    private final PerClanProtectionConfig perClanProtectionDefaults;
    private final RaidConfigState raidState;
    private final RaidConfig raidDefaults;
    private final RaidEconomicsConfigState raidEconomicsState;
    private final RaidEconomicsConfig raidEconomicsDefaults;
    private final WorldProtectionConfigState worldProtectionState;
    private final WorldProtectionConfig worldProtectionDefaults;

    private ConfigScreenBuilder configScreenBuilder;

    @Inject
    public ClansConfigScreenFactory(
        TranslatorFactory translatorFactory,
        ConfigStateManager configStateManager,
        ConfigScreenBuilderFactory configScreenBuilderFactory,
        FormulaParserFactory formulaParserFactory,
        DynmapConfigState dynmapState,
        @Named("default") DynmapConfig dynmapDefaults,
        GlobalClanConfigState globalState,
        @Named("default") GlobalClanConfig globalDefaults,
        PerClanConfigState perClanState,
        @Named("default") PerClanConfig perClanDefaults,
        PerClanEconomicsConfigState perClanEconomicsState,
        @Named("default") PerClanEconomicsConfig perClanEconomicsDefaults,
        PerClanProtectionConfigState perClanProtectionState,
        @Named("default") PerClanProtectionConfig perClanProtectionDefaults,
        RaidConfigState raidState,
        @Named("default") RaidConfig raidDefaults,
        RaidEconomicsConfigState raidEconomicsState,
        @Named("default") RaidEconomicsConfig raidEconomicsDefaults,
        WorldProtectionConfigState worldProtectionState,
        @Named("default") WorldProtectionConfig worldProtectionDefaults
    ) {
        this.translator = translatorFactory.getTranslator(ClansConstants.MODID);
        this.configStateManager = configStateManager;
        this.configScreenBuilderFactory = configScreenBuilderFactory;
        this.formulaParserFactory = formulaParserFactory;
        this.dynmapState = dynmapState;
        this.dynmapDefaults = dynmapDefaults;
        this.globalState = globalState;
        this.globalDefaults = globalDefaults;
        this.perClanState = perClanState;
        this.perClanDefaults = perClanDefaults;
        this.perClanEconomicsState = perClanEconomicsState;
        this.perClanEconomicsDefaults = perClanEconomicsDefaults;
        this.perClanProtectionState = perClanProtectionState;
        this.perClanProtectionDefaults = perClanProtectionDefaults;
        this.raidState = raidState;
        this.raidDefaults = raidDefaults;
        this.raidEconomicsState = raidEconomicsState;
        this.raidEconomicsDefaults = raidEconomicsDefaults;
        this.worldProtectionState = worldProtectionState;
        this.worldProtectionDefaults = worldProtectionDefaults;
    }

    public Screen getConfigScreen(Screen parent) {
        this.configScreenBuilder = configScreenBuilderFactory.create(
            translator,
            TRANSLATION_BASE + "title",
            TRANSLATION_BASE + "clan",
            parent,
            () -> {
                configStateManager.save(dynmapState);
                configStateManager.save(globalState);
                configStateManager.save(perClanState);
                configStateManager.save(perClanEconomicsState);
                configStateManager.save(perClanProtectionState);
                configStateManager.save(raidState);
                configStateManager.save(raidEconomicsState);
                configStateManager.save(worldProtectionState);
            }
        );
        // Clan Globals tab
        addClanGlobalCategoryEntries();
        // Clan Defaults tab
        this.configScreenBuilder.startCategory(TRANSLATION_BASE + "clanDefault");
        addPerClanCategoryEntries();
        this.configScreenBuilder.startSubCategory(TRANSLATION_BASE + "protection");
        addPerClanProtectionCategoryEntries();
        this.configScreenBuilder.endSubCategory();
        if (hasEconomy()) {
            this.configScreenBuilder.startSubCategory(ECONOMICS_TRANSLATION_KEY);
            addPerClanEconomicsCategoryEntries();
            this.configScreenBuilder.endSubCategory();
        }
        // Raid tab
        this.configScreenBuilder.startCategory(TRANSLATION_BASE + "raid");
        addRaidCategoryEntries();
        if (hasEconomy()) {
            this.configScreenBuilder.startSubCategory(ECONOMICS_TRANSLATION_KEY);
            addRaidEconomicsCategoryEntries();
            this.configScreenBuilder.endSubCategory();
        }
        // World Protection tab
        this.configScreenBuilder.startCategory(TRANSLATION_BASE + "worldProtection");
        addWorldProtectionCategoryEntries();
        // Dynmap tab
        if (hasDynmap()) {
            this.configScreenBuilder.startCategory(TRANSLATION_BASE + "dynmap");
            addDynmapCategoryEntries();
        }

        return this.configScreenBuilder.build();
    }

    private OptionBuilder<String> addFormulaField(
        String optionTranslationBase,
        String currentValue,
        String defaultValue,
        Consumer<String> saveFunction,
        byte descriptionRowCount
    ) {
        return this.configScreenBuilder.addStringField(//TODO new Formula Builder GUI
                optionTranslationBase,
                currentValue,
                defaultValue,
                saveFunction
            ).setErrorSupplier(formula -> {
                FormulaParser formulaParser = this.formulaParserFactory.createParser(formula);
                //TODO fill in the variables with dummy values
                try {
                    formulaParser.parseDouble();
                } catch (ParsingException exception) {
                    return Optional.of(translator.getTranslatedText("text.config.clans.invalid_formula"));
                }
                return Optional.empty();
            })
            .setDescriptionRowCount(descriptionRowCount)
            .appendCustomDescriptionRow(translator.getTranslatedText(FORMULA_SUFFIX_TRANSLATION_KEY));
    }

    private void addClanGlobalCategoryEntries() {
        configScreenBuilder.addBoolToggle(
            GLOBAL_TRANSLATION_BASE + "allowMultipleLeaders",
            globalState.allowMultipleLeaders(),
            globalDefaults.allowMultipleLeaders(),
            globalState::setAllowMultipleLeaders
        );
        configScreenBuilder.addBoolToggle(
            GLOBAL_TRANSLATION_BASE + "allowMultipleClanMembership",
            globalState.allowMultipleClanMembership(),
            globalDefaults.allowMultipleClanMembership(),
            globalState::setAllowMultipleClanMembership
        );
        configScreenBuilder.addIntField(
            GLOBAL_TRANSLATION_BASE + "maximumNameLength",
            globalState.getMaximumNameLength(),
            globalDefaults.getMaximumNameLength(),
            globalState::setMaximumNameLength
        ).setMinimum(0).setDescriptionRowCount((byte) 3);
        configScreenBuilder.addStringField(
            GLOBAL_TRANSLATION_BASE + "newPlayerDefaultClan",
            globalState.getNewPlayerDefaultClan(),
            globalDefaults.getNewPlayerDefaultClan(),
            globalState::setNewPlayerDefaultClan
        );
        configScreenBuilder.addIntField(
            GLOBAL_TRANSLATION_BASE + "minimumDistanceBetweenHomes",
            globalState.getMinimumDistanceBetweenHomes(),
            globalDefaults.getMinimumDistanceBetweenHomes(),
            globalState::setMinimumDistanceBetweenHomes
        ).setMinimum(0);
        configScreenBuilder.addDoubleField(
            GLOBAL_TRANSLATION_BASE + "initialClaimSeparationMultiplier",
            globalState.getInitialClaimSeparationDistanceMultiplier(),
            globalDefaults.getInitialClaimSeparationDistanceMultiplier(),
            globalState::setInitialClaimSeparationDistanceMultiplier
        ).setMinimum(0.0).setDescriptionRowCount((byte) 2);
        configScreenBuilder.addBoolToggle(
            GLOBAL_TRANSLATION_BASE + "enforceInitialClaimSeparation",
            globalState.enforceInitialClaimSeparation(),
            globalDefaults.enforceInitialClaimSeparation(),
            globalState::setEnforceInitialClaimSeparation
        ).setDescriptionRowCount((byte) 3);
        if (hasEconomy()) {
            configScreenBuilder.startSubCategory(ECONOMICS_TRANSLATION_KEY);
            configScreenBuilder.addDoubleField(
                GLOBAL_TRANSLATION_BASE + "formationCost",
                globalState.getFormationCost(),
                globalDefaults.getFormationCost(),
                globalState::setFormationCost
            ).setMinimum(0.0);
            configScreenBuilder.addDoubleField(
                GLOBAL_TRANSLATION_BASE + "initialBankAmount",
                globalState.getInitialBankAmount(),
                globalDefaults.getInitialBankAmount(),
                globalState::setInitialBankAmount
            ).setMinimum(0.0);
            configScreenBuilder.endSubCategory();
        }
    }

    private void addDynmapCategoryEntries() {

    }

    private void addPerClanCategoryEntries() {
        this.configScreenBuilder.addIntField(
            PER_CLAN_TRANSLATION_BASE + "homeTeleportWarmupTime",
            perClanState.getHomeTeleportWarmupTime(),
            perClanDefaults.getHomeTeleportWarmupTime(),
            perClanState::setHomeTeleportWarmupTime
        ).setMinimum(-1).setDescriptionRowCount((byte) 3);
        this.configScreenBuilder.addIntField(
            PER_CLAN_TRANSLATION_BASE + "homeTeleportCooldownTime",
            perClanState.getHomeTeleportCooldownTime(),
            perClanDefaults.getHomeTeleportCooldownTime(),
            perClanState::setHomeTeleportCooldownTime
        ).setMinimum(0).setDescriptionRowCount((byte) 2);
        this.addFormulaField(
            PER_CLAN_TRANSLATION_BASE + "maxClaimCountFormula",
            perClanState.getMaxClaimCountFormula(),
            perClanDefaults.getMaxClaimCountFormula(),
            perClanState::setMaxClaimCountFormula,
            (byte) 1
        );
        this.configScreenBuilder.addStringField(
            PER_CLAN_TRANSLATION_BASE + "chatPrefix",
            perClanState.getChatPrefix(),
            perClanDefaults.getChatPrefix(),
            perClanState::setChatPrefix
        ).setDescriptionRowCount((byte) 3);
        this.configScreenBuilder.addBoolToggle(
            PER_CLAN_TRANSLATION_BASE + "isHomeFallbackSpawnpoint",
            perClanState.isHomeFallbackSpawnpoint(),
            perClanDefaults.isHomeFallbackSpawnpoint(),
            perClanState::setHomeFallbackSpawnpoint
        );
    }

    private void addPerClanEconomicsCategoryEntries() {

    }

    private void addPerClanProtectionCategoryEntries() {

    }

    private void addRaidCategoryEntries() {

    }

    private void addRaidEconomicsCategoryEntries() {

    }

    private void addWorldProtectionCategoryEntries() {

    }

    private boolean hasEconomy() {
        return FabricLoader.getInstance().isModLoaded("grandeconomy");
    }

    private boolean hasDynmap() {
        //TODO is this the right ID?
        return FabricLoader.getInstance().isModLoaded("dynmap");
    }
}
