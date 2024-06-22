package lod.dragoonmodifier;

import com.github.slugify.Slugify;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import legend.core.GameEngine;
import legend.core.gte.MV;
import legend.game.EngineStateEnum;
import legend.game.Scus94491BpeSegment_8002;
import legend.game.Scus94491BpeSegment_8007;
import legend.game.Scus94491BpeSegment_800b;
import legend.game.characters.Addition04;
import legend.game.characters.Element;
import legend.game.characters.ElementSet;
import legend.game.characters.TurnBasedPercentileBuff;
import legend.game.characters.VitalsStat;
import legend.game.combat.Battle;
import legend.game.combat.bent.AttackEvent;
import legend.game.combat.bent.BattleEntity27c;
import legend.game.combat.bent.BattleEntityStat;
import legend.game.combat.bent.MonsterBattleEntity;
import legend.game.combat.bent.PlayerBattleEntity;
import legend.game.combat.types.AdditionHitProperties10;
import legend.game.combat.types.AdditionHits80;
import legend.game.combat.types.AttackType;
import legend.game.combat.types.CombatantStruct1a8;
import legend.game.input.InputAction;
import legend.game.inventory.Equipment;
import legend.game.inventory.EquipmentRegistryEvent;
import legend.game.inventory.Item;
import legend.game.inventory.ItemRegistryEvent;
import legend.game.inventory.SpellRegistryEvent;
import legend.game.inventory.WhichMenu;
import legend.game.inventory.screens.ShopScreen;
import legend.game.inventory.screens.TextColour;
import legend.game.modding.coremod.CoreMod;
import legend.game.modding.events.battle.AttackSpGainEvent;
import legend.game.modding.events.battle.BattleDescriptionEvent;
import legend.game.modding.events.battle.BattleEndedEvent;
import legend.game.modding.events.battle.BattleEntityTurnEvent;
import legend.game.modding.events.battle.BattleStartedEvent;
import legend.game.modding.events.battle.DragonBlockStaffOffEvent;
import legend.game.modding.events.battle.DragonBlockStaffOnEvent;
import legend.game.modding.events.battle.DragoonDeffEvent;
import legend.game.modding.events.battle.EnemyRewardsEvent;
import legend.game.modding.events.battle.ItemIdEvent;
import legend.game.modding.events.battle.MonsterStatsEvent;
import legend.game.modding.events.battle.SelectedItemEvent;
import legend.game.modding.events.battle.SingleMonsterTargetEvent;
import legend.game.modding.events.battle.SpellItemDeffEvent;
import legend.game.modding.events.battle.SpellStatsEvent;
import legend.game.modding.events.battle.StatDisplayEvent;
import legend.game.modding.events.battle.TemporaryItemStatsEvent;
import legend.game.modding.events.characters.AdditionUnlockEvent;
import legend.game.modding.events.characters.XpToLevelEvent;
import legend.game.modding.events.config.ConfigLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.game.modding.events.input.InputPressedEvent;
import legend.game.modding.events.input.InputReleasedEvent;
import legend.game.modding.events.inventory.GiveEquipmentEvent;
import legend.game.modding.events.inventory.GiveItemEvent;
import legend.game.modding.events.inventory.RepeatItemReturnEvent;
import legend.game.modding.events.inventory.ShopEquipmentEvent;
import legend.game.modding.events.inventory.ShopItemEvent;
import legend.game.modding.events.inventory.ShopTypeEvent;
import legend.game.modding.events.inventory.TakeItemEvent;
import legend.game.modding.events.submap.SubmapWarpEvent;
import legend.game.saves.ConfigEntry;
import legend.game.saves.ConfigRegistryEvent;
import legend.game.scripting.ScriptState;
import legend.game.submap.SMap;
import legend.game.submap.SubmapState;
import legend.game.types.ActiveStatsa0;
import legend.game.types.EquipmentSlot;
import legend.game.types.InventoryMenuState;
import legend.game.types.ItemStats0c;
import legend.game.types.LevelStuff08;
import legend.game.types.MagicStuff08;
import legend.game.types.SpellStats0c;
import legend.game.wmap.WMap;
import legend.game.wmap.WmapState;
import legend.lodmod.LodEquipment;
import legend.lodmod.LodItems;
import legend.lodmod.LodMod;
import legend.lodmod.items.FileBasedItem;
import lod.dragoonmodifier.configs.DamageTrackerConfig;
import lod.dragoonmodifier.configs.DifficultyEntryConfig;
import lod.dragoonmodifier.configs.ElementalBombConfig;
import lod.dragoonmodifier.configs.EnrageModeConfig;
import lod.dragoonmodifier.configs.FaustDefeatedConfig;
import lod.dragoonmodifier.configs.HellFlowerStormConfig;
import lod.dragoonmodifier.configs.MonsterHPNamesConfig;
import lod.dragoonmodifier.configs.UltimateBossConfig;
import lod.dragoonmodifier.configs.UltimateBossDefeatedConfig;
import lod.dragoonmodifier.events.DifficultyChangedEvent;
import lod.dragoonmodifier.events.HellModeAdjustmentEvent;
import lod.dragoonmodifier.values.DamageTracker;
import lod.dragoonmodifier.values.ElementalBomb;
import lod.dragoonmodifier.values.EnrageMode;
import lod.dragoonmodifier.values.MonsterHPNames;
import org.joml.Vector2f;
import org.legendofdragoon.modloader.Mod;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import static legend.core.GameEngine.CONFIG;
import static legend.core.GameEngine.REGISTRIES;
import static legend.core.GameEngine.RENDERER;
import static legend.game.SItem.getXpToNextLevel;
import static legend.game.Scus94491BpeSegment_8004._8004dd48;
import static legend.game.Scus94491BpeSegment_8004.currentEngineState_8004dd04;
import static legend.game.Scus94491BpeSegment_8004.engineState_8004dd20;
import static legend.game.Scus94491BpeSegment_8005.submapCut_80052c30;
import static legend.game.Scus94491BpeSegment_8006.battleState_8006e398;
import static legend.game.Scus94491BpeSegment_800b.battleStage_800bb0f4;
import static legend.game.Scus94491BpeSegment_800b.encounterId_800bb0f8;
import static legend.game.Scus94491BpeSegment_800b.fullScreenEffect_800bb140;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.Scus94491BpeSegment_800b.scriptStatePtrArr_800bc1c0;
import static legend.game.Scus94491BpeSegment_800b.spGained_800bc950;
import static legend.game.combat.Battle.spellStats_800fa0b8;
import static legend.game.combat.SEffe.transformToScreenSpace;

@Mod(id = DragoonModifier.MOD_ID)
public class DragoonModifier {
  public static final String MOD_ID = "dragoon_modifier";
  public static boolean DEBUG_MODE = true;
  public static boolean REGISTERED = false;
  public static final String[] charNames = {"Dart", "Lavitz", "Shana", "Rose", "Haschel", "Albert", "Meru", "Kongol", "???"};

  public static final List<String[]> monsterStats = new ArrayList<>();
  public static final List<String[]> monstersRewardsStats = new ArrayList<>();
  public static final List<String[]> additionStats = new ArrayList<>();
  public static final List<String[]> additionMultiStats = new ArrayList<>();
  public static final List<String[]> additionUnlockStats = new ArrayList<>();
  public static final List<String[]> characterStatsTable = new ArrayList<>();
  public static final List<String[]> dragoonStatsTable = new ArrayList<>();
  public static final List<String[]> xpNextStats = new ArrayList<>();
  public static final List<String[]> dxpNextStats = new ArrayList<>();
  public static final List<String[]> spellStats = new ArrayList<>();
  public static final List<String[]> equipStats = new ArrayList<>();
  public static final List<String[]> itemStats = new ArrayList<>();
  public static final List<String[]> shopItems = new ArrayList<>();
  public static final List<String[]> shopPrices = new ArrayList<>();
  public static final List<String[]> levelCaps = new ArrayList<>();
  public static final List<String[]> spBarColours = new ArrayList<>();
  public static final List<String[]> shanaSpGain = new ArrayList<>();
  public static final List<String[]> ultimateData = new ArrayList<>();
  public static final List<ItemStats0c> inventoryItemStats = new ArrayList<>();

  public static int maxCharacterLevel = 60;
  public static int maxDragoonLevel = 5;
  public static int maxAdditionLevel = 5;
  public static int additionsPerLevel = 20;
  public int currentPlayerSlot = 0;
  public boolean dragonBlockStaff = false;
  public int[] enrageMode = new int[10];
  public Element[] previousElement = new Element[3];
  public int[][] damageTrackerEquips = new int[3][5];
  public int[][] damageTracker = new int[3][5];
  public int[] damageTrackerPreviousHP = new int[10];
  public int damageTrackerPreviousCharacter = 0;
  public int damageTrackerPreviousCharacterID = 0;
  public int damageTrackerPreviousAttackType = 0;
  public ArrayList<String> damageTrackerLog = new ArrayList<>();
  public boolean damageTrackerPrinted = false;
  public boolean[] elementalAttack = new boolean[3];
  public int[] windMark = new int[10];
  public int[] thunderCharge = new int[10];
  public boolean flowerStormOverride = false;
  public boolean[] shanaStarChildrenHeal = new boolean[3];
  public boolean[] shanaRapidFireContinue = new boolean[3];
  public boolean[] shanaRapidFire = new boolean[3];
  public int[] shanaRapidFireCount = new int[3];
  public boolean[] meruBoost = new boolean[3];
  public int[] meruBoostTurns = new int[3];
  public int[] meruMDFSave = new int[3];
  public int[] meruMaxHpSave = new int[3];
  public Element[] elementalBombPreviousElement = new Element[10];
  public int[] elementalBombTurns = new int[10];
  public boolean swappedEXP = false;
  public int[] swapEXPParty = new int[3];
  public int[][] ultimateEncounter = {{487, 10}, {386, 3}, {414, 8},
    {461, 21}, {412, 16}, {413, 70}, {387, 5}, {415, 12},
    {449, 68}, {402, 23}, {403, 29}, {417, 31}, {418, 41}, {448, 68}, {416, 38}, {422, 42}, {423, 47}, {432, 69}, {430, 67}, {433, 56}, {431, 54}, {447, 68}
  };
  public boolean ultimateBattle = false;
  public int ultimateLevelCap = 30;
  public double[][] ultimatePenality = new double[3][2];
  public boolean[] bonusItemSP = new boolean[3];
  public boolean[] ouroboros = new boolean[3];
  public ArrayList<Element> elementArrowsElements = new ArrayList<>();
  public int[] ringOfElements = new int[3];
  public Element[] ringOfElementsElement = new Element[3];

  public static int selectedItemId = -1;
  public static int selectedFakeItemId = 0;
  public static ItemStats0c selectedItemStats = null;
  public static int lastSelectedMenuType = 0;

  public Set<InputAction> hotkey = new HashSet<>();

  public boolean burnStackMode = false;
  public int burnStacks = 0;
  public int previousBurnStacks = 0;
  public double dmgPerBurn = 0.1;
  public int burnStacksMax = 0;
  public double maxBurnAddition = 1;
  public final int burnStackFlameShot = 1;
  public final int burnStackExplosion = 2;
  public final int burnStackFinalBurst = 3;
  public final int burnStackRedEye = 4;
  public final int burnStackAddition = 1;
  public boolean burnAdded = false;
  public boolean faustBattle = false;
  public int armorOfLegendTurns = 0;
  public int legendCasqueTurns = 0;

  public static final Registrar<ConfigEntry<?>, ConfigRegistryEvent> DRAMOD_CONFIG_REGISTRAR = new Registrar<>(GameEngine.REGISTRIES.config, MOD_ID);
  public static final RegistryDelegate<DifficultyEntryConfig> DIFFICULTY = DRAMOD_CONFIG_REGISTRAR.register("difficulty", DifficultyEntryConfig::new);
  public static final RegistryDelegate<FaustDefeatedConfig> FAUST_DEFEATED = DRAMOD_CONFIG_REGISTRAR.register("faust_defeated", FaustDefeatedConfig::new);
  public static final RegistryDelegate<MonsterHPNamesConfig> MONSTER_HP_NAMES = DRAMOD_CONFIG_REGISTRAR.register("hp_names", MonsterHPNamesConfig::new);
  public static final RegistryDelegate<EnrageModeConfig> ENRAGE_MODE = DRAMOD_CONFIG_REGISTRAR.register("enrage_mode", EnrageModeConfig::new);
  public static final RegistryDelegate<HellFlowerStormConfig> FLOWER_STORM = DRAMOD_CONFIG_REGISTRAR.register("flower_storm", HellFlowerStormConfig::new);
  public static final RegistryDelegate<UltimateBossConfig> ULTIMATE_BOSS = DRAMOD_CONFIG_REGISTRAR.register("ultimate_boss", UltimateBossConfig::new);
  public static final RegistryDelegate<UltimateBossDefeatedConfig> ULTIMATE_BOSS_DEFEATED = DRAMOD_CONFIG_REGISTRAR.register("ultimate_boss_defeated", UltimateBossDefeatedConfig::new);
  public static final RegistryDelegate<ElementalBombConfig> ELEMENTAL_BOMB = DRAMOD_CONFIG_REGISTRAR.register("elemental_bomb", ElementalBombConfig::new);
  public static final RegistryDelegate<DamageTrackerConfig> DAMAGE_TRACKER = DRAMOD_CONFIG_REGISTRAR.register("damage_tracker", DamageTrackerConfig::new);

  //region Startup
  public DragoonModifier() {
    GameEngine.EVENTS.register(this);
  }

  public RegistryId id(final String entryId) {
    return new RegistryId(MOD_ID, entryId);
  }

  public void print(String text) { if(DEBUG_MODE) System.out.println("[DRAGOON MODIFIER] " + text); }

  @EventListener public void configRegistry(final ConfigRegistryEvent event) {
    print("Config Registry Event");
    DRAMOD_CONFIG_REGISTRAR.registryEvent(event);
    loadAllCsvs(GameEngine.CONFIG.getConfig(DIFFICULTY.get()));
  }

  @EventListener public void configLoaded(final ConfigLoadedEvent event) {
    if(event.storageLocation == DIFFICULTY.get().storageLocation) {
      print("Config Loaded Event" + equipStats.size());
      loadAllCsvs(event.configCollection.getConfig(DIFFICULTY.get()));
    }
  }

  @EventListener public void difficultyChanged(final DifficultyChangedEvent event) {
    loadAllCsvs(GameEngine.CONFIG.getConfig(DIFFICULTY.get()));
  }

  public List<String[]> loadCSV(final String path) {
    try (FileReader fr = new FileReader(path, StandardCharsets.UTF_8);
         CSVReader csv = new CSVReader(fr)) {
      List<String[]> list = csv.readAll();
      list.remove(0);
      return list;
    } catch (IOException | CsvException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadCsvIntoList(final String difficulty, final List<String[]> list, final String file) {
    list.clear();
    list.addAll(loadCSV("./mods/csvstat/" + difficulty + '/' + file));
  }

  private void loadAllCsvs(final String difficulty) {
    this.loadCsvIntoList(difficulty, monsterStats, "scdk-monster-stats.csv");
    this.loadCsvIntoList(difficulty, monstersRewardsStats, "scdk-monster-rewards.csv");
    this.loadCsvIntoList(difficulty, additionStats, "scdk-addition-stats.csv");
    this.loadCsvIntoList(difficulty, additionUnlockStats, "scdk-addition-unlock-levels.csv");
    this.loadCsvIntoList(difficulty, additionMultiStats, "scdk-addition-multiplier-stats.csv");
    this.loadCsvIntoList(difficulty, characterStatsTable, "scdk-character-stats.csv");
    this.loadCsvIntoList(difficulty, dragoonStatsTable, "scdk-dragoon-stats.csv");
    this.loadCsvIntoList(difficulty, xpNextStats, "scdk-exp-table.csv");
    this.loadCsvIntoList(difficulty, dxpNextStats, "scdk-dragoon-exp-table.csv");
    this.loadCsvIntoList(difficulty, spellStats, "scdk-spell-stats.csv");
    this.loadCsvIntoList(difficulty, equipStats, "scdk-equip-stats.csv");
    this.loadCsvIntoList(difficulty, itemStats, "scdk-thrown-item-stats.csv");
    this.loadCsvIntoList(difficulty, shopItems, "scdk-shop-items.csv");
    this.loadCsvIntoList(difficulty, shopPrices, "scdk-shop-prices.csv");
    this.loadCsvIntoList(difficulty, levelCaps, "scdk-level-caps.csv");
    this.loadCsvIntoList(difficulty, spBarColours, "scdk-sp-bar-colours.csv");
    this.loadCsvIntoList(difficulty, shanaSpGain, "scdk-shana-sp-gain.csv");
    this.loadCsvIntoList("Ultimate", ultimateData, "scdk-ultimate.csv");
    this.maxCharacterLevel = Integer.parseInt(levelCaps.get(0)[0]);
    this.maxDragoonLevel = Integer.parseInt(levelCaps.get(0)[1]);
    this.maxAdditionLevel = Integer.parseInt(levelCaps.get(0)[2]);
    this.additionsPerLevel = Integer.parseInt(levelCaps.get(0)[3]);

    print("Loaded using directory: " + difficulty + equipStats.size());
    configSwapped(); //TODO message to reload the game or quit
  }

  @EventListener public void newGame(final NewGameEvent event) {
    event.gameState.items_2e9.clear();
    event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i3").get());
    event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());
    event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());
    event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());
    event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());

    final Map<EquipmentSlot, Equipment> dart = event.gameState.charData_32c[0].equipment_14;
    dart.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e0").get());
    dart.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e76").get());
    dart.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e46").get());
    dart.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
    dart.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> lavitz = event.gameState.charData_32c[1].equipment_14;
    lavitz.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e20").get());
    lavitz.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e77").get());
    lavitz.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e47").get());
    lavitz.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
    lavitz.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> shana = event.gameState.charData_32c[2].equipment_14;
    shana.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e27").get());
    shana.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e82").get());
    shana.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e62").get());
    shana.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
    shana.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> rose = event.gameState.charData_32c[3].equipment_14;
    rose.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e13").get());
    rose.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e82").get());
    rose.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e63").get());
    rose.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e96").get());
    rose.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> haschel = event.gameState.charData_32c[4].equipment_14;
    haschel.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e40").get());
    haschel.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e78").get());
    haschel.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e57").get());
    haschel.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
    haschel.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> albert = event.gameState.charData_32c[5].equipment_14;
    albert.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e20").get());
    albert.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e77").get());
    albert.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e47").get());
    albert.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
    albert.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> meru = event.gameState.charData_32c[6].equipment_14;
    meru.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e34").get());
    meru.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e84").get());
    meru.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e64").get());
    meru.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e97").get());
    meru.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> kongol = event.gameState.charData_32c[7].equipment_14;
    kongol.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e8").get());
    kongol.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e78").get());
    kongol.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e53").get());
    kongol.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
    kongol.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final Map<EquipmentSlot, Equipment> miranda = event.gameState.charData_32c[8].equipment_14;
    miranda.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e27").get());
    miranda.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e82").get());
    miranda.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e62").get());
    miranda.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
    miranda.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      event.gameState.gold_94 = 200;
    } else {
      event.gameState.gold_94 = 20;
    }
  }

  @EventListener public void submapWarp(final SubmapWarpEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    if(submapCut_80052c30 == 676) {
      event.gameState.items_2e9.clear();
      event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i3").get());
      event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());
      event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());
      event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());
      event.gameState.items_2e9.add(REGISTRIES.items.getEntry("dragoon_modifier:i11").get());

      final Map<EquipmentSlot, Equipment> dart = event.gameState.charData_32c[0].equipment_14;
      dart.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e0").get());
      dart.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e76").get());
      dart.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e46").get());
      dart.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
      dart.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> lavitz = event.gameState.charData_32c[1].equipment_14;
      lavitz.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e20").get());
      lavitz.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e77").get());
      lavitz.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e47").get());
      lavitz.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
      lavitz.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> shana = event.gameState.charData_32c[2].equipment_14;
      shana.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e27").get());
      shana.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e82").get());
      shana.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e62").get());
      shana.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
      shana.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> rose = event.gameState.charData_32c[3].equipment_14;
      rose.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e13").get());
      rose.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e82").get());
      rose.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e63").get());
      rose.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e96").get());
      rose.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> haschel = event.gameState.charData_32c[4].equipment_14;
      haschel.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e40").get());
      haschel.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e78").get());
      haschel.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e57").get());
      haschel.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
      haschel.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> albert = event.gameState.charData_32c[5].equipment_14;
      albert.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e20").get());
      albert.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e77").get());
      albert.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e47").get());
      albert.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
      albert.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> meru = event.gameState.charData_32c[6].equipment_14;
      meru.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e34").get());
      meru.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e84").get());
      meru.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e64").get());
      meru.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e97").get());
      meru.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> kongol = event.gameState.charData_32c[7].equipment_14;
      kongol.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e8").get());
      kongol.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e78").get());
      kongol.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e53").get());
      kongol.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
      kongol.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      final Map<EquipmentSlot, Equipment> miranda = event.gameState.charData_32c[8].equipment_14;
      miranda.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e27").get());
      miranda.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e82").get());
      miranda.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e62").get());
      miranda.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e94").get());
      miranda.put(EquipmentSlot.ACCESSORY, REGISTRIES.equipment.getEntry("dragoon_modifier:e152").get());

      if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
        event.gameState.gold_94 = 200;
      } else {
        event.gameState.gold_94 = 20;
      }
    } else if(submapCut_80052c30 == 10) {
      if((difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) && gameState_800babc8.charData_32c[0].level_12 == 1) {
        gameState_800babc8.goods_19c[0] ^= 1 << 0;
        gameState_800babc8.goods_19c[0] ^= 1 << 1;
        gameState_800babc8.goods_19c[0] ^= 1 << 2;
        gameState_800babc8.goods_19c[0] ^= 1 << 3;
        gameState_800babc8.goods_19c[0] ^= 1 << 4;
        gameState_800babc8.goods_19c[0] ^= 1 << 5;
        gameState_800babc8.goods_19c[0] ^= 1 << 6;
      }
    }
  }

  public void configSwapped() {
    CoreMod.MAX_CHARACTER_LEVEL = this.maxCharacterLevel;
    CoreMod.MAX_DRAGOON_LEVEL = this.maxDragoonLevel;
    CoreMod.MAX_ADDITION_LEVEL = this.maxAdditionLevel;
    CoreMod.ADDITIONS_PER_LEVEL = this.additionsPerLevel;

    for(int i = 0; i < 9; i++) {
      //CoreMod.CHARACTER_DATA[i].xpTable = new int[this.maxCharacterLevel + 1];
      CoreMod.CHARACTER_DATA[i].statsTable = new LevelStuff08[this.maxCharacterLevel + 1];
      CoreMod.CHARACTER_DATA[i].dxpTable = new int[CoreMod.MAX_DRAGOON_LEVEL + 1];
      CoreMod.CHARACTER_DATA[i].dragoonStatsTable = new MagicStuff08[CoreMod.MAX_DRAGOON_LEVEL + 1];
      CoreMod.CHARACTER_DATA[i].additions = new ArrayList<>();
      CoreMod.CHARACTER_DATA[i].additionsMultiplier = new ArrayList<>();
      CoreMod.CHARACTER_DATA[i].dragoonAddition = new ArrayList<>();
    }

    for(int i = 0; i < 9; i++) {
      for(int x = 0; x < maxCharacterLevel + 1; x++) {
        CoreMod.CHARACTER_DATA[i].xpTable[x] = Integer.parseInt(xpNextStats.get((this.maxCharacterLevel + 1) * i + x)[0]);
        CoreMod.CHARACTER_DATA[i].statsTable[x] = new LevelStuff08(Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[5]), Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[6]),
          Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[0]), Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[1]),
          Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[2]), Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[3]),
          Integer.parseInt(characterStatsTable.get((this.maxCharacterLevel + 1) * i + x)[4]));
      }
    }

    for(int i = 0; i < 9; i++) {
      CoreMod.CHARACTER_DATA[i].spBarColours = new int[this.maxDragoonLevel + 2][6];
      for(int x = 0; x < this.maxDragoonLevel + 1; x++) {
        CoreMod.CHARACTER_DATA[i].dxpTable[x] = Integer.parseInt(dxpNextStats.get(i)[x]);
      }
      for(int x = 0; x < this.maxDragoonLevel + 1; x++) {
        int spellIndex = Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[1]);
        CoreMod.CHARACTER_DATA[i].dragoonStatsTable[x] = new MagicStuff08(Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[0]), spellIndex == 255 ? (byte) -1 : (byte) spellIndex,
          Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[2]), Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[3]),
          Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[4]), Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[5]),
          Integer.parseInt(dragoonStatsTable.get((this.maxDragoonLevel + 1) * i + x)[6]));
      }
    }

    for(int i = 0; i < 9; i++) {
      for(int x = 0; x < this.maxDragoonLevel + 2; x++) {
        int top = Integer.decode(spBarColours.get(i * 2)[x].replace("#", "0x"));
        int btm = Integer.decode(spBarColours.get(1 * 2 + 1)[x].replace("#", "0x"));

        final int[] topArray = {
          ((top >> 24) & 0xff),
          ((top >> 16) & 0xff),
          ((top >> 8) & 0xff),
          (top & 0xff)
        };

        final int[] btmArray = {
          ((btm >> 24) & 0xff),
          ((btm >> 16) & 0xff),
          ((btm >> 8) & 0xff),
          (btm & 0xff)
        };

        final int[] rgbArray = {topArray[1], topArray[2], topArray[3], btmArray[1], btmArray[2], btmArray[3]};
        CoreMod.CHARACTER_DATA[i].spBarColours[x] = rgbArray;
      }
    }

    loadCharacterAdditions(0, 0, 7);
    loadCharacterAdditions(1, 8, 13);
    loadCharacterAdditions(3, 14, 18);
    loadCharacterAdditions(7, 19, 22);
    loadCharacterAdditions(6, 23, 28);
    loadCharacterAdditions(4, 29, 35);
    loadCharacterAdditions(5, 36, 41);
    loadAdditionMultiplier(0, 0, 7);
    loadAdditionMultiplier(1, 8, 13);
    loadAdditionMultiplier(3, 14, 18);
    loadAdditionMultiplier(7, 19, 22);
    loadAdditionMultiplier(6, 23, 28);
    loadAdditionMultiplier(4, 29, 35);
    loadAdditionMultiplier(5, 36, 41);
    loadDragoonAddition(0, 7, false);
    loadDragoonAddition(1, 13, false);
    loadDragoonAddition(3, 18, false);
    loadDragoonAddition(7, 22, false);
    loadDragoonAddition(6, 28, false);
    loadDragoonAddition(4, 35, false);
    loadDragoonAddition(5, 41, false);
    loadDragoonAddition(0, 42, true);
    loadShanaAdditions();
    loadAdditionMultiplier(2, 7, 8);
    loadAdditionMultiplier(8, 7, 8);

    print("Config swapped."  + equipStats.size());
  }
  //endregion

  //region Additions
  public void loadCharacterAdditions(final int charIndex, final int additionStart, final int additionEnd) {
    CoreMod.CHARACTER_DATA[charIndex].additions = new ArrayList<>();

    for (int i = additionStart; i < additionEnd; i++) {
      final AdditionHitProperties10[] hits = new AdditionHitProperties10[8];

      for(int x = 0; x < 8; x++) {
        hits[x] = new AdditionHitProperties10();
        for(int y = 0; y < 16; y++) {
          hits[x].set(y, Short.parseShort(additionStats.get(i * 8 + x)[y]));
        }
      }

      CoreMod.CHARACTER_DATA[charIndex].additions.add(new AdditionHits80(hits));
    }
  }

  public void loadAdditionMultiplier(final int charIndex, final int additionStart, final int additionEnd) {
    CoreMod.CHARACTER_DATA[charIndex].additionsMultiplier = new ArrayList<>();

    for(int i = additionStart; i < additionEnd; i++) {
      final Addition04[] multipliers = new Addition04[this.maxAdditionLevel + 1];

      for(int x = 0; x < this.maxAdditionLevel + 1; x++) {
        multipliers[x] = new Addition04();
        multipliers[x]._00 = Integer.parseInt(additionMultiStats.get(i)[x * 4]);
        multipliers[x].spMultiplier_02 = Integer.parseInt(additionMultiStats.get(i)[x * 4 + 2]);
        multipliers[x].damageMultiplier_03 = Integer.parseInt(additionMultiStats.get(i)[x * 4 + 3]);
      }

      CoreMod.CHARACTER_DATA[charIndex].additionsMultiplier.add(multipliers);
    }
  }

  public void loadDragoonAddition(final int charIndex, final int dragoonIndex, final boolean divine) {
    CoreMod.CHARACTER_DATA[charIndex].dragoonAddition = new ArrayList<>();
    final AdditionHitProperties10[] hits = new AdditionHitProperties10[8];

    for(int x = 0; x < 8; x++) {
      hits[x] = new AdditionHitProperties10();
      for(int y = 0; y < 16; y++) {
        hits[x].set(y, Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[y]));
      }
    }

    CoreMod.CHARACTER_DATA[charIndex].dragoonAddition.add(new AdditionHits80(hits));
  }

  public void loadShanaAdditions() {
    CoreMod.CHARACTER_DATA[2].additions = new ArrayList<>();
    CoreMod.CHARACTER_DATA[8].additions = new ArrayList<>();
    CoreMod.CHARACTER_DATA[2].dragoonAddition = new ArrayList<>();
    CoreMod.CHARACTER_DATA[8].dragoonAddition = new ArrayList<>();

    final AdditionHitProperties10[] hits = new AdditionHitProperties10[8];
    for(int x = 0; x < 8; x++) {
      hits[x] = new AdditionHitProperties10();
      for(int y = 0; y < 16; y++) {
        hits[x].set(y, (short) 0);
      }
    }

    CoreMod.CHARACTER_DATA[2].additions.add(new AdditionHits80(hits));
    CoreMod.CHARACTER_DATA[2].dragoonAddition.add(new AdditionHits80(hits));
    CoreMod.CHARACTER_DATA[8].additions.add(new AdditionHits80(hits));
    CoreMod.CHARACTER_DATA[8].dragoonAddition.add(new AdditionHits80(hits));
  }

  @EventListener public void additionUnlock(final AdditionUnlockEvent unlock) {
    unlock.additionLevel = Integer.parseInt(additionUnlockStats.get(unlock.additionId)[0]);
  }

  /*@EventListener public void additionMulti(final AdditionHitMultiplierEvent multiplier) { //TODO: Is this needed? It's not needed keeping it here.
    multiplier.additionSpMulti = Integer.parseInt(additionMultiStats.get(multiplier.additionId)[(multiplier.additionLevel) * 4 + 2]);
    multiplier.additionDmgMulti = Integer.parseInt(additionMultiStats.get(multiplier.additionId)[(multiplier.additionLevel) * 4 + 3]);
  }*/
  //endregion

  //region Inventory
  @Deprecated public static final Int2ObjectMap<RegistryId> equipmentIdMap = new Int2ObjectOpenHashMap<>();
  @Deprecated public static final Object2IntMap<RegistryId> idEquipmentMap = new Object2IntOpenHashMap<>();
  private static final Slugify slug = Slugify.builder().locale(Locale.US).underscoreSeparator(true).customReplacement("'", "").customReplacement("-", "_").build();

  @EventListener public void equipmentRegistry(final EquipmentRegistryEvent event) {
    equipmentIdMap.clear();
    idEquipmentMap.clear();
    print("Equipment Registry Event - " + equipStats.size());

    for(int i = 0; i < equipStats.size(); i++) {
      final ElementSet elementalResistance = new ElementSet();
      final ElementSet elementalImmunity = new ElementSet();
      final int special1 = Integer.parseInt(equipStats.get(i)[11]);
      final int special2 = Integer.parseInt(equipStats.get(i)[12]);
      final int specialAmount = Integer.parseInt(equipStats.get(i)[13]);
      final int mpPerMagicalHit = (special1 & 0x1) != 0 ? specialAmount : 0;
      final int spPerMagicalHit = (special1 & 0x2) != 0 ? specialAmount : 0;
      final int mpPerPhysicalHit = (special1 & 0x4) != 0 ? specialAmount : 0;
      final int spPerPhysicalHit = (special1 & 0x8) != 0 ? specialAmount : 0;
      final int spMultiplier = (special1 & 0x10) != 0 ? specialAmount : 0;
      final boolean physicalResistance = (special1 & 0x20) != 0;
      final boolean magicalImmunity = (special1 & 0x40) != 0;
      final boolean physicalImmunity = (special1 & 0x80) != 0;
      final int mpMultiplier = (special2 & 0x1) != 0 ? specialAmount : 0;
      final int hpMultiplier = (special2 & 0x2) != 0 ? specialAmount : 0;
      final boolean magicalResistance = (special2 & 0x4) != 0;
      final int revive = (special2 & 0x8) != 0 ? specialAmount : 0;
      final int spRegen = (special2 & 0x10) != 0 ? specialAmount : 0;
      final int mpRegen = (special2 & 0x20) != 0 ? specialAmount : 0;
      final int hpRegen = (special2 & 0x40) != 0 ? specialAmount : 0;
      final int special2Flag80 = (special2 & 0x80) != 0 ? specialAmount : 0;
      final Equipment dmEquip = new Equipment(
        equipStats.get(i)[29],
        equipStats.get(i)[30].replace('\u00a7', '\n'),
        Integer.parseInt(equipStats.get(i)[28]),
        Integer.parseInt(equipStats.get(i)[0]),
        Integer.parseInt(equipStats.get(i)[1]),
        Integer.parseInt(equipStats.get(i)[2]),
        Integer.parseInt(equipStats.get(i)[3]),
        Element.fromFlag(Integer.parseInt(equipStats.get(i)[4])),
        Integer.parseInt(equipStats.get(i)[5]),
        elementalResistance,
        elementalImmunity,
        Integer.parseInt(equipStats.get(i)[8]),
        Integer.parseInt(equipStats.get(i)[9]),
        0,
        mpPerPhysicalHit,
        spPerPhysicalHit,
        mpPerMagicalHit,
        spPerMagicalHit,
        hpMultiplier,
        mpMultiplier,
        spMultiplier,
        magicalResistance,
        physicalResistance,
        magicalImmunity,
        physicalImmunity,
        revive,
        hpRegen,
        mpRegen,
        spRegen,
        special2Flag80,
        Integer.parseInt(equipStats.get(i)[14]),
        Integer.parseInt(equipStats.get(i)[15]),
        Integer.parseInt(equipStats.get(i)[16]) + Integer.parseInt(equipStats.get(i)[10]),
        Integer.parseInt(equipStats.get(i)[17]),
        Integer.parseInt(equipStats.get(i)[18]),
        Integer.parseInt(equipStats.get(i)[19]),
        Integer.parseInt(equipStats.get(i)[20]),
        Integer.parseInt(equipStats.get(i)[21]),
        Integer.parseInt(equipStats.get(i)[22]),
        Integer.parseInt(equipStats.get(i)[23]),
        Integer.parseInt(equipStats.get(i)[24]),
        Integer.parseInt(equipStats.get(i)[25]),
        Integer.parseInt(equipStats.get(i)[26]),
        Integer.parseInt(equipStats.get(i)[27])
      );

      final Equipment equipment = event.register(id("e" + i), dmEquip);
      equipmentIdMap.put(i, equipment.getRegistryId());
      idEquipmentMap.put(equipment.getRegistryId(), i);
    }
  }

  @Deprecated public static final Int2ObjectMap<RegistryId> itemIdMap = new Int2ObjectOpenHashMap<>();
  @Deprecated public static final Object2IntMap<RegistryId> idItemMap = new Object2IntOpenHashMap<>();

  @EventListener public void itemRegistry(final ItemRegistryEvent event) {
    inventoryItemStats.clear();

    for(int i = 0; i < itemStats.size(); i++) {
      final int target = Integer.parseInt(itemStats.get(i)[0]);
      final Set<Item.TargetType> targets = EnumSet.noneOf(Item.TargetType.class);
      final Set<Item.UsageLocation> usage = EnumSet.noneOf(Item.UsageLocation.class);
      int special1 = Integer.parseInt(itemStats.get(i)[3]);
      int special2 = Integer.parseInt(itemStats.get(i)[4]);
      int specialAmount = Integer.parseInt(itemStats.get(i)[6]);
      int powerDefence = (special1 & 0x80) != 0 ? specialAmount : 0;
      int powerMagicDefence = (special1 & 0x40) != 0 ? specialAmount : 0;
      int powerAttack = (special1 & 0x20) != 0 ? specialAmount : 0;
      int powerMagicAttack = (special1 & 0x10) != 0 ? specialAmount : 0;
      int powerAttackHit = (special1 & 0x8) != 0 ? specialAmount : 0;
      int powerMagicAttackHit = (special1 & 0x4) != 0 ? specialAmount : 0;
      int powerAttackAvoid = (special1 & 0x2) != 0 ? specialAmount : 0;
      int powerMagicAttackAvoid = (special1 & 0x1) != 0 ? specialAmount : 0;
      boolean physicalImmunity = (special2 & 0x80) != 0;
      boolean magicalImmunity = (special2 & 0x40) != 0;
      int speedUp = (special2 & 0x20) != 0 ? 100 : 0;
      int speedDown = (special2 & 0x10) != 0 ? -50 : 0;
      int spPerPhysicalHit = (special2 & 0x8) != 0 ? specialAmount : 0;
      int mpPerPhysicalHit = (special2 & 0x4) != 0 ? specialAmount : 0;
      int spPerMagicalHit = (special2 & 0x2) != 0 ? specialAmount : 0;
      int mpPerMagicalHit = (special2 & 0x1) != 0 ? specialAmount : 0;

      if((target & 0x2) != 0) {
        targets.add(Item.TargetType.ALL);
      }

      if((target & 0x4) != 0) {
        targets.add(Item.TargetType.ENEMIES);
      } else {
        targets.add(Item.TargetType.ALLIES);
      }

      if((target & 0x10) != 0) {
        usage.add(Item.UsageLocation.MENU);
      }

      usage.add(Item.UsageLocation.BATTLE);

      final Item dmItem = new FileBasedItem(
        itemStats.get(i)[13],
        itemStats.get(i)[14].replace('\u00a7', '\n'),
        itemStats.get(i)[15],
        Integer.parseInt(itemStats.get(i)[12]),
        targets,
        usage,
        Element.fromFlag(Integer.parseInt(itemStats.get(i)[1])),
        Integer.parseInt(itemStats.get(i)[2]),
        powerDefence,
        powerMagicDefence,
        powerAttack,
        powerMagicAttack,
        powerAttackHit,
        powerMagicAttackHit,
        powerAttackAvoid,
        powerMagicAttackAvoid,
        physicalImmunity,
        magicalImmunity,
        speedUp,
        speedDown,
        spPerPhysicalHit,
        mpPerPhysicalHit,
        spPerMagicalHit,
        mpPerMagicalHit,
        Integer.parseInt(itemStats.get(i)[5]),
        Integer.parseInt(itemStats.get(i)[7]),
        Integer.parseInt(itemStats.get(i)[8]),
        Integer.parseInt(itemStats.get(i)[9]),
        Integer.parseInt(itemStats.get(i)[10]),
        Integer.parseInt(itemStats.get(i)[11])
      );

      inventoryItemStats.add(new ItemStats0c(
        itemStats.get(i)[13],
        itemStats.get(i)[14].replace('\u00a7', '\n'),
        itemStats.get(i)[15],
        Integer.parseInt(itemStats.get(i)[0]),
        Element.fromFlag(Integer.parseInt(itemStats.get(i)[1])),
        Integer.parseInt(itemStats.get(i)[2]),
        powerDefence,
        powerMagicDefence,
        powerAttack,
        powerMagicAttack,
        powerAttackHit,
        powerMagicAttackHit,
        powerAttackAvoid,
        powerMagicAttackAvoid,
        physicalImmunity,
        magicalImmunity,
        speedUp,
        speedDown,
        spPerPhysicalHit,
        mpPerPhysicalHit,
        spPerMagicalHit,
        mpPerMagicalHit,
        Integer.parseInt(itemStats.get(i)[5]),
        Integer.parseInt(itemStats.get(i)[7]),
        Integer.parseInt(itemStats.get(i)[8]),
        Integer.parseInt(itemStats.get(i)[9]),
        Integer.parseInt(itemStats.get(i)[10]),
        Integer.parseInt(itemStats.get(i)[11])
        ));

      final Item item = event.register(id("i" + i), dmItem);
      itemIdMap.put(i, item.getRegistryId());
      idItemMap.put(item.getRegistryId(), i);
    }
  }

  @EventListener public void spellRegistry(final SpellRegistryEvent event) {
    for (int i = 0; i < spellStats.size(); i++) {
      spellStats_800fa0b8[i] = new SpellStats0c(spellStats.get(i)[12],
        spellStats.get(i)[13],
        Integer.parseInt(spellStats.get(i)[0]),
        Integer.parseInt(spellStats.get(i)[1]),
        Integer.parseInt(spellStats.get(i)[2]),
        Integer.parseInt(spellStats.get(i)[3]),
        Integer.parseInt(spellStats.get(i)[4]),
        Integer.parseInt(spellStats.get(i)[5]),
        Integer.parseInt(spellStats.get(i)[6]),
        Integer.parseInt(spellStats.get(i)[7]),
        Element.fromFlag(Integer.parseInt(spellStats.get(i)[8])),
        Integer.parseInt(spellStats.get(i)[9]),
        Integer.parseInt(spellStats.get(i)[10]),
        Integer.parseInt(spellStats.get(i)[11]));
    }
  }

  @EventListener public void giveItem(final GiveItemEvent event) {
    event.override = true;
    event.item = REGISTRIES.items.getEntry("dragoon_modifier:i" + LodMod.idItemMap.get(event.item.getRegistryId())).get();
  }

  @EventListener public void takeItem(final GiveEquipmentEvent event) {
    event.override = true;
    event.equip = REGISTRIES.equipment.getEntry("dragoon_modifier:e" + LodMod.idEquipmentMap.get(event.equip.getRegistryId())).get();
  }
  //endregion

  //region Inventory Battle
  @EventListener public void itemId(final ItemIdEvent event) {
    if(event.registryId.toString().contains("dragoon_modifier")) {
      final String item = event.registryId.toString().split(":")[1];
      event.itemId = Integer.parseInt(item.substring(1, item.length()));
      final int fakeItemId = Integer.parseInt(itemStats.get(event.itemId)[17]);
      print("Item ID: " + event.itemId + "/" + event.registryId + " Fake ID: " + fakeItemId);
      selectedItemId = event.itemId;
      selectedFakeItemId = fakeItemId;
    } else {
      selectedItemId = -1;
    }
  }

  @EventListener public void battleDescription(final BattleDescriptionEvent event) {
    print("Description: " + event.textType + "/" + event.textIndex);
    if(event.textType == 4) {
      if(selectedItemId > -1) {
        event.string = itemStats.get(selectedItemId)[15];
      }
    }
    lastSelectedMenuType = event.textType;
  }

  @EventListener public void temporaryItemStats(final TemporaryItemStatsEvent event) {
    if(event.bent instanceof PlayerBattleEntity) {
      if(event.attackType == 5) {
        event.itemStats = inventoryItemStats.get(event.itemId);
        selectedItemStats = event.itemStats;
      } else {
        if(selectedItemStats != null) {
          event.itemStats = selectedItemStats;
        }
      }
      print("Temporary Item Stats Percentage %: " + event.itemStats.percentage_09 + "/type: " + event.attackType + " ID: " + event.itemId);
    }
  }

  @EventListener public void selectedItem(final SelectedItemEvent event) {
    final String item = event.item.getRegistryId().toString().split(":")[1];
    final int fakeItemId = Integer.parseInt(itemStats.get(event.itemId)[17]);
    event.itemId = (short) fakeItemId;
  }

  @EventListener public void spellItemDeff(SpellItemDeffEvent event) {
    if(selectedItemId != -1 && lastSelectedMenuType == 4) {
      final int deffScriptId = Integer.parseInt(itemStats.get(selectedItemId)[16]);
      if(deffScriptId != -1) {
        event.scriptId = deffScriptId;
        event.s0 = 0;
      }
    }

    print("Item/Spell DEFF: " + event.scriptId + "+" + event.s0);
  }

  @EventListener public void dragoonDeff(final DragoonDeffEvent event) {
    print("Dragoon DEFF: " + event.scriptId);
    switch (event.scriptId) {
      case 4205: //Transform?
      case 4235: //Dart Attack
      case 4237: //Lavitz Attack
      case 4241: //Rose Attack
      case 4243: //Haschel Attack
      case 4245: //Albert Attack
      case 4247: //Meru Attack
      case 4249: //Kongol Attack
      case 4253: //Divine Attack
      case 4307: //Burn Out
      case 4311: //Spark Net
      case 4315: //???
      case 4317: //Pellet
      case 4319: //Spear Frost
      case 4321: //Spinning Gale
      case 4325: //Trans Light
      case 4327: //Dark Mist
        new Thread(() -> {
          for (int i = 0; i < 80; i++) {
            try {
              fullScreenEffect_800bb140.type_00 = 0;
              Thread.sleep(125);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        }).start();
        break;
      case 4208: //Blossom Storm
      case 4234: //Rose Storm
        final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
        if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
          flowerStormOverride = true;
        }
        break;
    }
  }
  //endregion

  //region Battle Monster
  @EventListener public void monsterStats(final MonsterStatsEvent event) {
    int ovrId = event.enemyId;
    event.hp = Integer.parseInt(monsterStats.get(ovrId)[1]);
    event.maxHp = Integer.parseInt(monsterStats.get(ovrId)[1]);
    event.attack = Integer.parseInt(monsterStats.get(ovrId)[3]);
    event.magicAttack = Integer.parseInt(monsterStats.get(ovrId)[4]);
    event.speed = Integer.parseInt(monsterStats.get(ovrId)[5]);
    event.defence = Integer.parseInt(monsterStats.get(ovrId)[6]);
    event.magicDefence = Integer.parseInt(monsterStats.get(ovrId)[7]);
    event.attackAvoid = Integer.parseInt(monsterStats.get(ovrId)[8]);
    event.magicAvoid = Integer.parseInt(monsterStats.get(ovrId)[9]);
    event.specialEffectFlag = Integer.parseInt(monsterStats.get(ovrId)[10]);
    event.elementFlag = Element.fromFlag(Integer.parseInt(monsterStats.get(ovrId)[12]));
    event.elementalImmunityFlag.clear();
    if(Integer.parseInt(monsterStats.get(ovrId)[13]) > 0)
      event.elementalImmunityFlag.add(Element.fromFlag(Integer.parseInt(monsterStats.get(ovrId)[13])));
    event.statusResistFlag = Integer.parseInt(monsterStats.get(ovrId)[14]);
  }

  @EventListener public void enemyRewards(final EnemyRewardsEvent event) {
    int enemyId = event.enemyId;
    event.clear();
    if(ultimateBattle) {
      for (int i = 0; i < 86; i++) {
        if(enemyId == Integer.parseInt(ultimateData.get(i)[0])) {
          final String item = monstersRewardsStats.get(enemyId)[27];

          event.xp = Integer.parseInt(ultimateData.get(i)[25]);
          event.gold = Integer.parseInt(ultimateData.get(i)[26]);
          if(!item.startsWith("N")) {
            event.add(new CombatantStruct1a8.ItemDrop(Integer.parseInt(ultimateData.get(i)[28]), item.startsWith("e") ? REGISTRIES.equipment.getEntry(equipmentIdMap.get(Integer.parseInt(item.substring(1, item.length())))).get() : REGISTRIES.items.getEntry(itemIdMap.get(Integer.parseInt(item.substring(1, item.length())))).get()));
          }
          break;
        }
      }
    } else {
      final String item = monstersRewardsStats.get(enemyId)[3];
      event.xp = Integer.parseInt(monstersRewardsStats.get(enemyId)[0]);
      event.gold = Integer.parseInt(monstersRewardsStats.get(enemyId)[1]);
      if(!item.startsWith("N")) {
        event.add(new CombatantStruct1a8.ItemDrop(Integer.parseInt(monstersRewardsStats.get(enemyId)[2]), item.startsWith("e") ? REGISTRIES.equipment.getEntry(equipmentIdMap.get(Integer.parseInt(item.substring(1, item.length())))).get() : REGISTRIES.items.getEntry(itemIdMap.get(Integer.parseInt(item.substring(1, item.length())))).get()));
      }
      if(faustBattle && event.enemyId == 344) {
        event.clear();
        event.xp = 30000;
        event.gold = 250;
        if(Integer.parseInt(GameEngine.CONFIG.getConfig(FAUST_DEFEATED.get())) == 39) {
          event.add(new CombatantStruct1a8.ItemDrop(100, REGISTRIES.equipment.getEntry("dragoon_modifier:e74").get()));
          event.add(new CombatantStruct1a8.ItemDrop(100, REGISTRIES.equipment.getEntry("dragoon_modifier:e89").get()));
        }
      }
    }
  }
  //endregion

  //region Battle
  @EventListener public void battleStarted(final BattleStartedEvent event) {
    if(faustBattle) {
      final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[0];
      final BattleEntity27c bobj = state.innerStruct_00;
      final VitalsStat hp = bobj.stats.getStat(CoreMod.HP_STAT.get());
      hp.setCurrent(25600);
      hp.setMaxRaw(25600);
      bobj.attack_34 = 125;
      bobj.magicAttack_36 = 125;
      bobj.defence_38 = 75;
      bobj.magicDefence_3a = 200;
    }

    burnStacks = 0;
    armorOfLegendTurns = 0;
    legendCasqueTurns = 0;
    dragonBlockStaff = false;
    burnStackMode = false;
    flowerStormOverride = false;
    damageTrackerPrinted = false;
    Arrays.fill(enrageMode, 0);
    Arrays.fill(windMark, 0);
    Arrays.fill(thunderCharge, 0);
    Arrays.fill(elementalAttack, false);
    Arrays.fill(shanaStarChildrenHeal, false);
    Arrays.fill(shanaRapidFireContinue, false);
    Arrays.fill(shanaRapidFire, false);
    Arrays.fill(shanaRapidFireCount, 0);
    Arrays.fill(meruBoost, false);
    Arrays.fill(bonusItemSP, false);
    Arrays.fill(ouroboros, false);
    Arrays.fill(meruBoostTurns, 0);
    Arrays.fill(meruMaxHpSave, 0);
    Arrays.fill(meruMDFSave, 0);
    Arrays.fill(damageTracker[0], 0);
    Arrays.fill(damageTracker[1], 0);
    Arrays.fill(damageTracker[2], 0);
    Arrays.fill(ringOfElements, 0);
    Arrays.fill(ringOfElementsElement, null);
    damageTrackerLog.clear();
    elementArrowsElements.clear();

    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());

    if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      for (int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof PlayerBattleEntity player) {
          if(player.charId_272 == 0) {
            burnStacksMax = player.dlevel_06 == 0 ? 0 : player.dlevel_06 == 1 ? 3 : player.dlevel_06 == 2 ? 6 : player.dlevel_06 == 3 ? 9 : player.dlevel_06 == 7 ? 15 : 12;
          }

          player.equipmentElementalImmunity_22.clear();

          if(player.charId_272 == 7) { //Kongol SPD reduction
            final ActiveStatsa0 stats = Scus94491BpeSegment_800b.stats_800be5f8[player.charId_272];
            player.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(stats.bodySpeed_69 + (int) Math.round(stats.equipmentSpeed_86 / 2d));
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e149")) { //Phantom Shield
            player.defence_38 = (int) Math.round(player.defence_38 * 0.6d);
            player.magicDefence_3a = (int) Math.round(player.magicDefence_3a * 0.6d);
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e150")) { // Dragon Shield
            player.defence_38 = (int) Math.round(player.defence_38 * 0.6d);
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e151")) { // Angel Scarf
            player.magicDefence_3a = (int) Math.round(player.magicDefence_3a * 0.6d);
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e130") && player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().equals("dragoon_modifier:e73")) { //Holy Ahnk + Angel Robe
            player.revive_13a -= 20;
          }

          int crystalItems = 0;
          if(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().equals("dragoon_modifier:e173")) { //Crystal Armor
            crystalItems++;
          }

          if(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().equals("dragoon_modifier:e174")) { //Crystal Hat
            crystalItems++;
          }

          if(player.equipment_11e.get(EquipmentSlot.BOOTS).getRegistryId().equals("dragoon_modifier:e175")) { //Crystal Boots
            crystalItems++;
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e176")) { //Crystal Ring
            crystalItems++;
          }

          if(crystalItems > 3) {
            player.attack_34 += 60;
            player.magicAttack_36 += 60;
            player.defence_38 += 60;
            player.magicDefence_3a += 60;
            player.attackHit_3c += 60;
            player.magicHit_3e += 60;
            player.attackAvoid_40 += 12;
            player.magicAvoid_42 += 12;
            if(player.charId_272 != 7) {
              player.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(player.stats.getStat(CoreMod.SPEED_STAT.get()).get() + 12);
            } else {
              player.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(player.stats.getStat(CoreMod.SPEED_STAT.get()).get() + 6);
            }
            player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(CoreMod.HP_STAT.get()).getMax() * 1.3d));
            player.stats.getStat(CoreMod.MP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(CoreMod.MP_STAT.get()).getMax() * 1.3d));
            player.hpRegen_134 = 10;
            player.mpRegen_136 = 10;
            player.spRegen_138 = 100;
          } else if(crystalItems > 2) {
            player.attack_34 += 30;
            player.magicAttack_36 += 30;
            player.defence_38 += 30;
            player.magicDefence_3a += 30;
            player.attackHit_3c += 30;
            player.magicHit_3e += 30;
            player.attackAvoid_40 += 6;
            player.magicAvoid_42 += 6;
            if(player.charId_272 != 7) {
              player.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(player.stats.getStat(CoreMod.SPEED_STAT.get()).get() + 6);
            } else {
              player.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(player.stats.getStat(CoreMod.SPEED_STAT.get()).get() + 3);
            }
            player.hpRegen_134 = 4;
            player.mpRegen_136 = 4;
            player.spRegen_138 = 40;
          } else if(crystalItems > 1) {
            player.attack_34 += 5;
            player.magicAttack_36 += 5;
            player.defence_38 += 5;
            player.magicDefence_3a += 5;
            player.attackHit_3c += 5;
            player.magicHit_3e += 5;
            player.attackAvoid_40 += 1;
            player.magicAvoid_42 += 1;
            if(player.charId_272 != 7) {
              player.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(player.stats.getStat(CoreMod.SPEED_STAT.get()).get() + 1);
            }
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e177")) { //Ring of Reversal
            int df = player.defence_38;
            int mdf = player.magicDefence_3a;
            player.magicDefence_3a = df;
            player.defence_38 = mdf;
            if(player.defence_38 > player.magicDefence_3a) {
              player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(CoreMod.HP_STAT.get()).getMax() * 1.5d));
            } else {
              player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(CoreMod.HP_STAT.get()).getMax() / 1.5d));
              player.spMultiplier_128 += 35;
            }
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e185")) { //The One Ring
            player.stats.getStat(CoreMod.HP_STAT.get()).setCurrent(1);
            player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw(1);
            player.attackAvoid_40 = 80;
            player.magicAvoid_42 = 80;
          }

          if(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().equals("dragoon_modifier:e187")) { //Divine DG Armor
            player.spPerPhysicalHit_12a += 10;
            player.spPerMagicalHit_12e += 10;
          }

          if(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().equals("dragoon_modifier:e188")) { //Halo of Balance
            player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(CoreMod.HP_STAT.get()).getMax() * 1.3d));
            player.stats.getStat(CoreMod.MP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(CoreMod.MP_STAT.get()).getMax() * 1.3d));
          }

          if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().equals("dragoon_modifier:e189")) { //Firebrand
            player.equipmentAttackElements_1c.add(CoreMod.FIRE_ELEMENT.get());
          }

          if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e190")) { //Super Spirit Ring
            player.spMultiplier_128 = -100;
          }

          if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
            int flowerStormTurns = GameEngine.CONFIG.getConfig(FLOWER_STORM.get());
            int flowerStorm = -1;
            if(player.charId_272 == 1) {
              flowerStorm = 7;
            } else if(player.charId_272 == 5) {
              flowerStorm = 26;
            }
            if(flowerStorm > 0) {
              spellStats_800fa0b8[flowerStorm] = new SpellStats0c(spellStats.get(flowerStorm)[12],
                spellStats.get(flowerStorm)[13].substring(0, spellStats.get(flowerStorm)[13].length() - 1) + GameEngine.CONFIG.getConfig(FLOWER_STORM.get()),
                Integer.parseInt(spellStats.get(flowerStorm)[0]),
                Integer.parseInt(spellStats.get(flowerStorm)[1]),
                Integer.parseInt(spellStats.get(flowerStorm)[2]),
                Integer.parseInt(spellStats.get(flowerStorm)[3]),
                Integer.parseInt(spellStats.get(flowerStorm)[4]),
                Integer.parseInt(spellStats.get(flowerStorm)[5]),
                GameEngine.CONFIG.getConfig(FLOWER_STORM.get()) * 20,
                Integer.parseInt(spellStats.get(flowerStorm)[7]),
                Element.fromFlag(Integer.parseInt(spellStats.get(flowerStorm)[8])),
                Integer.parseInt(spellStats.get(flowerStorm)[9]),
                Integer.parseInt(spellStats.get(flowerStorm)[10]),
                Integer.parseInt(spellStats.get(flowerStorm)[11]));
            }

            if(player.charId_272 == 2 || player.charId_272 == 8) {
              if(player.dlevel_06 >= 2) {
                int moonLight;
                int gatesOfHeaven;
                if(player.charId_272 == 2) {
                  moonLight = 11;
                  gatesOfHeaven = 12;
                } else {
                  moonLight = 66;
                  gatesOfHeaven = 67;
                }

                spellStats_800fa0b8[moonLight] = new SpellStats0c(spellStats.get(moonLight)[12],
                  spellStats.get(moonLight)[13],
                  Integer.parseInt(spellStats.get(moonLight)[0]),
                  Integer.parseInt(spellStats.get(moonLight)[1]),
                  Integer.parseInt(spellStats.get(moonLight)[2]),
                  Integer.parseInt(spellStats.get(moonLight)[3]),
                  Integer.parseInt(spellStats.get(moonLight)[4]),
                  Integer.parseInt(spellStats.get(moonLight)[5]),
                  20,
                  Integer.parseInt(spellStats.get(moonLight)[7]),
                  Element.fromFlag(Integer.parseInt(spellStats.get(moonLight)[8])),
                  Integer.parseInt(spellStats.get(moonLight)[9]),
                  Integer.parseInt(spellStats.get(moonLight)[10]),
                  Integer.parseInt(spellStats.get(moonLight)[11]));
                if(player.dlevel_06 >= 4 && player.stats.getStat(CoreMod.MP_STAT.get()).getMax() >= 120) {
                  spellStats_800fa0b8[gatesOfHeaven] = new SpellStats0c(spellStats.get(gatesOfHeaven)[12],
                    spellStats.get(gatesOfHeaven)[13],
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[0]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[1]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[2]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[3]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[4]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[5]),
                    player.stats.getStat(CoreMod.MP_STAT.get()).getMax() / 3,
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[7]),
                    Element.fromFlag(Integer.parseInt(spellStats.get(gatesOfHeaven)[8])),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[9]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[10]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[11]));
                }
              }
            }
          }
        }

        elementArrowsElements.add(bobj.getElement());
        if(bobj instanceof PlayerBattleEntity && bobj.getElement() == CoreMod.FIRE_ELEMENT.get() && gameState_800babc8.goods_19c[0] << 7 == 1) {
          elementArrowsElements.add(CoreMod.DIVINE_ELEMENT.get());
        }
      }
    }

    if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      GameEngine.EVENTS.postEvent(new HellModeAdjustmentEvent());
    }

    for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
      final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
      final BattleEntity27c bobj = state.innerStruct_00;
      if(bobj instanceof PlayerBattleEntity player) {
        damageTrackerEquips[player.charSlot_276][0] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().split(":")[1].substring(1));
        damageTrackerEquips[player.charSlot_276][1] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().toString().split(":")[1].substring(1));
        damageTrackerEquips[player.charSlot_276][2] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString().split(":")[1].substring(1));
        damageTrackerEquips[player.charSlot_276][3] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.BOOTS).getRegistryId().toString().split(":")[1].substring(1));
        damageTrackerEquips[player.charSlot_276][4] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString().split(":")[1].substring(1));
      }
    }

    if(ultimateBattle) {
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof PlayerBattleEntity player) {
          ultimatePenality[player.charSlot_276][0] = 1;
          ultimatePenality[player.charSlot_276][1] = 1;

          if(player.level_04 > ultimateLevelCap) {
            int levelDifference = player.level_04 - ultimateLevelCap;

            if(ultimateLevelCap == 30) {
              if(Math.round(levelDifference / 10d) == 1) { //Level 40
                ultimatePenality[player.charSlot_276][0] = 1.5;
                ultimatePenality[player.charSlot_276][1] = 1.26;
              } else if(Math.round(levelDifference / 10d) == 2) { //Level 50
                ultimatePenality[player.charSlot_276][0] = 2.6;
                ultimatePenality[player.charSlot_276][1] = 1.53;
              } else if(Math.round(levelDifference / 10d) == 3) { //Level 60
                ultimatePenality[player.charSlot_276][0] = 3.4;
                ultimatePenality[player.charSlot_276][1] = 1.89;
              }
            } else if(ultimateLevelCap == 40) {
              if(Math.round(levelDifference / 10d) == 1) { //Level 50
                ultimatePenality[player.charSlot_276][0] = 1.7;
                ultimatePenality[player.charSlot_276][1] = 1.17;
              } else if(Math.round(levelDifference / 10d) == 2) { //Level 60
                ultimatePenality[player.charSlot_276][0] = 2.2;
                ultimatePenality[player.charSlot_276][1] = 1.35;
              }
            } else if(ultimateLevelCap == 50) {
              if(Math.round(levelDifference / 10d) == 1) { //Level 60
                ultimatePenality[player.charSlot_276][0] = 1.3;
                ultimatePenality[player.charSlot_276][1] = 1.08;
              }
            }
          }

          if(ultimatePenality[player.charSlot_276][0] > 1) {
            int currentMax = player.stats.getStat(CoreMod.HP_STAT.get()).getMaxRaw();
            player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw(Math.round(Math.round((double) currentMax / ultimatePenality[player.charSlot_276][0])));
          }

          ultimateZeroSPStart(player);
        } else if(bobj instanceof MonsterBattleEntity monster) {
          int enemyId = monster.charId_272;
          for (int x = 0; x < 86; x++) {
            if(enemyId == Integer.parseInt(ultimateData.get(x)[0])) {
              monster.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw(Integer.parseInt(ultimateData.get(x)[1]));
              monster.stats.getStat(CoreMod.HP_STAT.get()).setCurrent(Integer.parseInt(ultimateData.get(x)[1]));
              monster.attack_34 = Integer.parseInt(ultimateData.get(x)[3]);
              monster.magicAttack_36 = Integer.parseInt(ultimateData.get(x)[4]);
              monster.stats.getStat(CoreMod.SPEED_STAT.get()).setRaw(Integer.parseInt(ultimateData.get(x)[5]));
              monster.defence_38 = Integer.parseInt(ultimateData.get(x)[6]);
              monster.magicDefence_3a = Integer.parseInt(ultimateData.get(x)[7]);
              monster.attackAvoid_40 = Integer.parseInt(ultimateData.get(x)[8]);
              monster.magicAvoid_42 = Integer.parseInt(ultimateData.get(x)[9]);
              monster.specialEffectFlag_14 = Integer.parseInt(ultimateData.get(x)[10]);
              monster.monsterElement_72 = Element.fromFlag(Integer.parseInt(ultimateData.get(x)[12]));
              monster.displayElement_1c = Element.fromFlag(Integer.parseInt(ultimateData.get(x)[12]));
              monster.equipmentStatusResist_24 = Integer.parseInt(ultimateData.get(x)[13]);
              monster.monsterElementalImmunity_74.clear();
              if(Integer.parseInt(ultimateData.get(x)[13]) > 0)
                monster.monsterElementalImmunity_74.add(Element.fromFlag(Integer.parseInt(ultimateData.get(x)[13])));
              monster.monsterStatusResistFlag_76 = Integer.parseInt(ultimateData.get(x)[14]);
              break;
            }
          }
        }
      }
    }

    updateMonsterHPNames();

    for (int i = 0; i < battleState_8006e398.getMonsterCount(); i++) {
      final MonsterBattleEntity monster = battleState_8006e398.monsterBents_e50[i].innerStruct_00;
      int hp = monster.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
      damageTrackerPreviousHP[monster.charSlot_276] = hp;
    }

    if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      GameEngine.EVENTS.postEvent(new HellModeAdjustmentEvent());
    }
  }

  @EventListener public void battleEntityTurn(final BattleEntityTurnEvent<?> event) {
    selectedItemId = -1;

    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    updateMonsterHPNames();
    updateItemMagicDamage();

    if(event.bent instanceof PlayerBattleEntity player) {
      damageTrackerLog.add(charNames[player.charId_272] + " Turn Started");

      if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
        if(player.isDragoon()) {
          spGained_800bc950[player.charSlot_276] += 100;
        }
        
        if(bonusItemSP[player.charSlot_276]) {
          bonusItemSP[player.charSlot_276] = false;
          if(player.isDragoon()) {
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent() + 50);
            int newSP = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
            if(player.charSlot_276 == 0) {
              battleState_8006e398.dragoonTurns_294[0] = newSP / 100;
            } else if(player.charSlot_276 == 1) {
              battleState_8006e398.dragoonTurns_294[1] = newSP / 100;
            } else if(player.charSlot_276 == 2) {
              battleState_8006e398.dragoonTurns_294[2] = newSP / 100;
            }
          }
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().equals("dragoon_modifier:e166")) { //Spirit Eater
          int sp = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
          if(!player.isDragoon() && sp != player.stats.getStat(CoreMod.SP_STAT.get()).getMax()) {
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(sp - 20);
          }
          spGained_800bc950[player.charSlot_276] += 40;
        }

        if(ouroboros[player.charSlot_276] && !player.isDragoon()) { //Ouroboros
          player.stats.getStat(CoreMod.SPEED_STAT.get()).addMod(new TurnBasedPercentileBuff(-50, 3));
          ouroboros[player.charSlot_276] = false;
        }

        if(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().equals("dragoon_modifier:e184")) { //Ring of Elements
          if(((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == player.element) {
            ringOfElements[player.charSlot_276]++;
            ringOfElementsElement[player.charSlot_276] = ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64;
          } else {
            if(player.element == CoreMod.FIRE_ELEMENT.get() && ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == CoreMod.DIVINE_ELEMENT.get()) {
              ringOfElements[player.charSlot_276]++;
              ringOfElementsElement[player.charSlot_276] = ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64;
            }

            if(((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == null) {
              ringOfElements[player.charSlot_276]--;
            }
          }
        }
      }
    }

    if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      if((difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) && flowerStormOverride) {
        flowerStormOverride = false;
        for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
          final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
          final BattleEntity27c bobj = state.innerStruct_00;
          if(bobj instanceof PlayerBattleEntity player) {
            player.powerDefenceTurns_b9 = GameEngine.CONFIG.getConfig(FLOWER_STORM.get());
            player.powerMagicDefenceTurns_bb = GameEngine.CONFIG.getConfig(FLOWER_STORM.get());
          }
        }
      }

      if(event.bent instanceof PlayerBattleEntity player) {
        currentPlayerSlot = player.charSlot_276;
        if(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().equals("dragoon_modifier:e74")) {
          armorOfLegendTurns += 1;
          if(armorOfLegendTurns <= 40) {
            player.defence_38 += 1;
          }
        }

        if(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().equals("dragoon_modifier:e89")) {
          legendCasqueTurns += 1;
          if(legendCasqueTurns <= 40) {
            player.magicDefence_3a += 1;
          }
        }

        if(player.charId_272 == 0) {
          burnAdded = false;

          if(burnStackMode) {
            burnStacks = 0;
            previousBurnStacks = 0;
            burnStackMode = false;
          }
        }

        if(player.charId_272 == 2 || player.charId_272 == 8) {
          if(shanaStarChildrenHeal[player.charSlot_276] && !player.isDragoon()) {
            shanaStarChildrenHeal[player.charSlot_276] = false;
            player.stats.getStat(CoreMod.HP_STAT.get()).setCurrent(player.stats.getStat(CoreMod.HP_STAT.get()).getMax());
          }

          if(shanaRapidFire[player.charSlot_276]) {
            shanaRapidFire[player.charSlot_276] = false;
            player.dragoonAttack_ac = dragonBlockStaff ? 365 * 8 : 365;
          }
        }

        if(elementalAttack[player.charSlot_276]) {
          player.element = previousElement[player.charSlot_276];
          elementalAttack[player.charSlot_276] = false;
          if(player.charId_272 == 2 || player.charId_272 == 8) {
            player.dragoonAttack_ac = dragonBlockStaff ? 365 * 8 : 365;
          }
        }

        if(player.charId_272 == 6 && meruBoost[player.charSlot_276]) {
          meruBoostTurns[player.charSlot_276] -= 1;
          if(meruBoostTurns[player.charSlot_276] == 0) {
            meruBoost[player.charSlot_276] = false;
            player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw(meruMaxHpSave[player.charSlot_276]);
            player.magicDefence_3a = meruMDFSave[player.charSlot_276];
          }
        }

        if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
          if(player.isDragoon()) {
            spGained_800bc950[player.charSlot_276] += 50;
          }
        }
      }
    }


    if(event.bent instanceof MonsterBattleEntity monster) {
      if(elementalBombTurns[monster.charSlot_276] > 0) {
        elementalBombTurns[monster.charSlot_276] -= 1;

        if(elementalBombTurns[monster.charSlot_276] == 0) {
          monster.displayElement_1c = elementalBombPreviousElement[monster.charSlot_276];
          monster.monsterElement_72 = elementalBombPreviousElement[monster.charSlot_276];
        }
      }
    }
  }

  @EventListener public void attack(final AttackEvent event) {
    if(event.attacker instanceof PlayerBattleEntity) {
      if(event.attackType == AttackType.DRAGOON_MAGIC_STATUS_ITEMS) {
        if(Integer.parseInt(spellStats.get(event.attacker.spellId_4e)[4]) == 0) {
          switch (Integer.parseInt(spellStats.get(event.attacker.spellId_4e)[3])) {
            case 0:
            case 1:
            case 2:
            case 4:
            case 8:
            case 16:
            case 32:
            case 64:
            case 128:
              break;
            default:
              event.damage *= (Integer.parseInt(spellStats.get(event.attacker.spellId_4e)[3]) / 100d);
          }
        }
      }
    }

    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());

    if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
            /*
                ATTACKING PLAYER
             */
      if(event.attacker instanceof PlayerBattleEntity player) {
        if(player.isDragoon() && event.attackType.isPhysical()) {
          if(player.element == ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64) { //Dragoon Space physical boost
            if(player.charId_272 == 7) {
              event.damage *= 1.2;
            } else {
              event.damage *= 1.5;
            }
          } else {
            if(player.element == Element.fromFlag(0x80) && ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == Element.fromFlag(0x8)) { //Divine Dart special physical boost
              if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e189")) { //Firebrand
                event.damage *= 1.1; //TODO this doesn't seem right
              } else {
                event.damage *= 1.5;
              }
            }
          }
        }

        if(event.defender instanceof MonsterBattleEntity) {
          int level = player.level_04;
          if(event.attackType.isPhysical() && (player.charId_272 == 2 || player.charId_272 == 8)) { //Shana AT Boost
            double boost = 1;
            if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e32")) {
              boost = 1.4;
            } else if(level >= 28) {
              boost = 2.15;
            } else if(level >= 20) {
              boost = 1.9;
            } else if(level >= 10) {
              boost = 1.6;
            }
            event.damage = (int) Math.round(event.damage * boost);
          }
        }

        if(player.spellId_4e >= 84) { //Item Spells In Dragoon
          if(player.charId_272 != 4) {
            bonusItemSP[player.charSlot_276] = true;
          }

          if(player.charId_272 == 3) {
            event.damage *= 1.7;
          } else if(player.charId_272 == 5) {
            event.damage *= 1.5;
          } else if(player.charId_272 == 7) {
            event.damage *= 2.2;
          }

          if(dragonBlockStaff) {
            event.damage /= 8;
          }
        }

        if(player.charId_272 == 2 || player.charId_272 == 8) {
          if(player.spellId_4e == 10 || player.spellId_4e == 65) { //Star Children full heal on exit
            shanaStarChildrenHeal[player.charSlot_276] = true;
          }
        }

        if(event.defender instanceof MonsterBattleEntity monster) {
          if(windMark[event.defender.charSlot_276] > 0) { //Wind mark turn value reduction
            monster.turnValue_4c = Math.max(0, monster.turnValue_4c - 10);
            windMark[event.defender.charSlot_276] -= 1;
          }
        }

        if(event.attacker.charId_272 == 0) {
          if(burnStackMode) {
            if(burnStacks == burnStacksMax) {
              if(player.spellId_4e == 0) {
                event.damage *= (1 + (burnStacks * dmgPerBurn)) * (Integer.parseInt(spellStats.get(2)[3]) / Integer.parseInt(spellStats.get(0)[3])) * 1.5;
              } else if(player.spellId_4e == 1) {
                event.damage *= (1 + (burnStacks * dmgPerBurn)) * Integer.parseInt(spellStats.get(3)[3]) / Integer.parseInt(spellStats.get(1)[3]);
              } else if(player.spellId_4e == 2) {
                event.damage *= (1 + (burnStacks * dmgPerBurn)) * 1.5;
              } else {
                event.damage *= 1 + (burnStacks * dmgPerBurn);
              }
            } else {
              event.damage *= 1 + (burnStacks * dmgPerBurn) * 1.5;
            }
          } else {
            if(event.attackType == AttackType.DRAGOON_MAGIC_STATUS_ITEMS && !burnAdded) {
              if(player.spellId_4e == 0 || player.spellId_4e == 84) {
                addBurnStacks(player, burnStackFlameShot);
              } else if(player.spellId_4e == 1) {
                addBurnStacks(player, burnStackExplosion);
              } else if(player.spellId_4e == 2) {
                addBurnStacks(player, burnStackFinalBurst);
              } else if(player.spellId_4e == 3) {
                addBurnStacks(player, burnStackRedEye);
              }
              burnAdded = true;
            } else if(event.attackType == AttackType.PHYSICAL && player.isDragoon()) {
              addBurnStacks(player, burnStackAddition);
              burnAdded = true;
            }
          }
        }

        if(event.attacker.charId_272 == 3) {
          if(player.spellId_4e == 15) {
            for (int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
              final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
              final BattleEntity27c bobj = state.innerStruct_00;
              if(bobj instanceof PlayerBattleEntity) {
                final int playerHealedHP = bobj.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
                final int roseMaxHP = player.stats.getStat(CoreMod.HP_STAT.get()).getMax();
                if(playerHealedHP > 0) {
                  bobj.stats.getStat(CoreMod.HP_STAT.get()).setCurrent((int) Math.min(bobj.stats.getStat(CoreMod.HP_STAT.get()).getMax(), (playerHealedHP + Math.round(roseMaxHP * player.dlevel_06 * 0.0425d))));
                }
              }
            }
          } else if(player.spellId_4e == 19) {
            player.stats.getStat(CoreMod.HP_STAT.get()).setCurrent((int) Math.min(player.stats.getStat(CoreMod.HP_STAT.get()).getMax(), player.stats.getStat(CoreMod.HP_STAT.get()).getCurrent() + event.damage * 0.1d));
          }
        }

        if(event.attacker.charId_272 == 1 || event.attacker.charId_272 == 5) {
          if(windMark[event.defender.charSlot_276] == 0 && event.attackType.isMagical() && player.isDragoon()) { //Add wind marks
            if(player.spellId_4e == 5 || player.spellId_4e == 14 || player.spellId_4e == 91) {
              windMark[event.defender.charSlot_276] = 1;
            } else if(player.spellId_4e == 7 || player.spellId_4e == 18) {
              windMark[event.defender.charSlot_276] = 2;
            } else if(player.spellId_4e == 8) {
              windMark[event.defender.charSlot_276] = 3;
            }
          }
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e167") && event.attackType.isPhysical()) { //Giant Axe
          if(new Random().nextInt(0, 99) < 20) {
            player.guard_54 = 1;
          }
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e168") && event.attackType.isPhysical()) { //Dragon Beater
          final int heal = (int) Math.round(event.damage * 0.01d);
          final int hp = player.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
          final int sp = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
          player.stats.getStat(CoreMod.HP_STAT.get()).setCurrent(hp + Math.min(1000, heal));
          player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(sp + Math.min(100, heal));
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e169") && player.isDragoon()) { //Ouroboros
          final int dragoonTurns = player.charSlot_276 == 0 ? battleState_8006e398.dragoonTurns_294[0] : player.charSlot_276 == 1 ? battleState_8006e398.dragoonTurns_294[1] : battleState_8006e398.dragoonTurns_294[2];
          final int sp = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
          if(player.isDragoon() && dragoonTurns >= 2 && sp >= 200) {
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(sp - 100);
            if(player.charSlot_276 == 0) {
              battleState_8006e398.dragoonTurns_294[0] = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent() / 100;
            } else if(player.charSlot_276 == 1) {
              battleState_8006e398.dragoonTurns_294[1] = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent() / 100;
            } else if(player.charSlot_276 == 2) {
              battleState_8006e398.dragoonTurns_294[2] = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent() / 100;
            }
            event.damage *= 2;
            ouroboros[player.charSlot_276] = true;
          }
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e170")) { //Elemental Arrow
          if(event.defender instanceof MonsterBattleEntity monster && event.attackType.isPhysical()) {
            ArrayList<Element> elementsCalculated = new ArrayList<>();
            for (int i = 0; i < elementArrowsElements.size(); i++) {
              if(elementArrowsElements.get(i) != null) {
                if(!elementsCalculated.contains(elementArrowsElements.get(i))) {
                  elementsCalculated.add(elementArrowsElements.get(i));
                  if(((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 != null) {
                    int damage = ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64.adjustDragoonSpaceDamage(event.attackType, event.damage, elementArrowsElements.get(i));
                    if(damage > event.damage) {
                      event.damage = damage;

                      damage = monster.getElement().adjustAttackingElementalDamage(event.attackType, event.damage, elementArrowsElements.get(i));
                      if(damage != event.damage) {
                        event.damage = damage;
                      }
                    }
                  } else {
                    final int damage = monster.getElement().adjustAttackingElementalDamage(event.attackType, event.damage, elementArrowsElements.get(i));
                    if(damage > event.damage) {
                      event.damage = damage;
                    }
                  }
                }
              }
            }

            if(new Random().nextInt(0, 99) < 40 && gameState_800babc8.items_2e9.size() < CONFIG.getConfig(CoreMod.INVENTORY_SIZE_CONFIG.get())) {
              Scus94491BpeSegment_8002.giveItem(REGISTRIES.items.getEntry("dragoon_modifier:i9").get());
            }
          }

          if(player.itemId_52 > 0) {
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent() + 100);
          }
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e171")) { //Magic Hammer
          if(event.attackType.isPhysical()) {
            event.damage = 0;
          }
          player.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(player.stats.getStat(CoreMod.MP_STAT.get()).getCurrent() + 8);
        }

        if(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().equals("dragoon_modifier:e172")) { //Overcharge Glove
          if(event.defender instanceof MonsterBattleEntity monster) {
            if(monster.getElement() == CoreMod.THUNDER_ELEMENT.get()) {
              event.damage *= 3;
            }
          }
        }

        for (int i = 0; i < 3; i++) {
          if(ringOfElements[i] > 0 && ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == null) { //Ring of Elements
            if(event.defender instanceof MonsterBattleEntity monster) {
              if(event.attackType.isPhysical()) {
                for (Element e : player.equipmentAttackElements_1c) {
                  final int damage = ringOfElementsElement[i].adjustDragoonSpaceDamage(event.attackType, event.damage, e);
                  if(damage != event.damage) {
                    event.damage = damage;
                  }
                }
              } else {
                try {
                  final int damage = event.attacker.spell_94.element_08.adjustDragoonSpaceDamage(event.attackType, event.damage, ringOfElementsElement[i]);
                  if(damage != event.damage) {
                    event.damage = damage;
                  }
                } catch (Exception ignored) {}

                try {
                  final int damage = event.attacker.item_d4.element_01.adjustDragoonSpaceDamage(event.attackType, event.damage, ringOfElementsElement[i]);
                  if(damage != event.damage) {
                    event.damage = damage;
                  }
                } catch (Exception ignored) {}
              }
            }
          }
        }

        if(event.defender instanceof MonsterBattleEntity monster) { //Haschel in party thunder charge
          try {
            if(event.attacker.spell_94.element_08 == CoreMod.THUNDER_ELEMENT.get() && new Random().nextBoolean()) {
              thunderCharge[monster.charSlot_276] = Math.min(10, thunderCharge[monster.charSlot_276] + 1);
            }
          } catch (Exception ignored) {}

          try {
            if(event.attacker.item_d4.element_01 == CoreMod.THUNDER_ELEMENT.get() && new Random().nextBoolean()) {
              thunderCharge[monster.charSlot_276] = Math.min(10, thunderCharge[monster.charSlot_276] + 1);
            }
          } catch (Exception ignored) {}

          if(event.attackType.isPhysical() && player.equipmentAttackElements_1c.contains(CoreMod.THUNDER_ELEMENT.get()) && new Random().nextBoolean()) {
            thunderCharge[monster.charSlot_276] = Math.min(10, thunderCharge[monster.charSlot_276] + 1);
          }
        }

        if(player.charSlot_276 == 4) { //Haschel thunder charge on physical and spark net boost on max stacks and thunder element
          if(event.defender instanceof MonsterBattleEntity monster) {
            if(player.dlevel_06 > 0) {
              if(event.attackType.isPhysical() && new Random().nextBoolean()) {
                thunderCharge[monster.charSlot_276] = Math.min(10, thunderCharge[monster.charSlot_276] + 1);
              } else {
                if(player.isDragoon() && player.spellId_4e == 86) {
                  if(thunderCharge[monster.charSlot_276] == 10) {
                    thunderCharge[monster.charSlot_276] = 0;
                    event.damage *= monster.getElement() == CoreMod.THUNDER_ELEMENT.get() ? 8.8 : 2.93333;
                  }
                }
              }
            }
          }
        }

        if(event.defender instanceof PlayerBattleEntity defender) { //If Meru's in Wingly Boost Mode all healing is 0
          if(meruBoost[defender.charSlot_276]) {
            try {
              if(Integer.parseInt(spellStats.get(event.attacker.spellId_4e)[4]) > 0) {
                event.damage = 0;
              }
            } catch (Exception ignored) {}

            try {
              if(Integer.parseInt(itemStats.get(event.attacker.itemId_52)[11]) == 128) {
                event.damage = 0;
              }
            } catch (Exception ignored) {}
          }
        }

        if(bonusItemSP[player.charSlot_276]) {
          player.itemId_52 = 0;
        }
      }

            /*
                DEFENDING MONSTER
             */

      if(event.defender instanceof PlayerBattleEntity defender) {
        if(defender.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString().equals("dragoon_modifier:e183")) { //Ring of Shielding
          final int hp = defender.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
          if((hp - event.damage) <= 0 && new Random().nextInt(0, 99) < 35) {
            for (int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
              final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
              final BattleEntity27c bobj = state.innerStruct_00;
              if(bobj == defender) {
                battleState_8006e398.specialEffect_00[i].shieldsSigStoneCharmTurns_1c = 5;
              }
            }
          }
        }

        if(defender.charId_272 == 6) { //If Meru dies in Wingly Boost turn it off
          final int hp = defender.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
          if(meruBoost[defender.charSlot_276] && hp - event.damage <= 0) {
            meruBoostTurns[defender.charSlot_276] = 0;
            meruBoost[defender.charSlot_276] = false;
            defender.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw(meruMaxHpSave[defender.charSlot_276]);
            defender.magicDefence_3a = meruMDFSave[defender.charSlot_276];
          }
        }

        if(ringOfElements[defender.charSlot_276] > 0) {
          final int hp = defender.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
          if(event.damage <= 0) {
            ringOfElements[defender.charSlot_276] = 0;
          }
        }

        final int level = defender.level_04;

        if(event.attackType.isPhysical()) { //DF Boost
          if(defender.charId_272 == 2 || defender.charId_272 == 8) { //Shana
            if(level >= 30) {
              event.damage = (int) Math.round(event.damage / 1.12d);
            }
          }

          if(defender.charId_272 == 3 && level >= 30) { //Rose
            event.damage = (int) Math.round(event.damage / 1.11d);
          }

          if(defender.charId_272 == 6 && level >= 30) { //Meru
            event.damage = (int) Math.round(event.damage / 1.26d);
          }
        }

        if(event.attackType.isMagical()) {
          Element attackElement = null;
          String equipID = defender.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString();
          final int armorEquipped = Integer.parseInt(equipID.split(":")[1].substring(1));

          try {
            attackElement = event.attacker.item_d4.element_01;
          } catch (Exception ignored) {}

          if(attackElement == null) {
            attackElement = event.attacker.spell_94.element_08;
          }

          //Divine Dragon Armor 15% elemental reduction instead of half
          if(attackElement == CoreMod.FIRE_ELEMENT.get() && armorEquipped == 51) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          } else if(attackElement == CoreMod.WIND_ELEMENT.get() && armorEquipped == 52) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          } else if(attackElement == CoreMod.EARTH_ELEMENT.get() && armorEquipped == 56) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          } else if(attackElement == CoreMod.THUNDER_ELEMENT.get() && armorEquipped == 61) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          } else if(attackElement == CoreMod.LIGHT_ELEMENT.get() && armorEquipped == 67) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          } else if(attackElement == CoreMod.DARK_ELEMENT.get() && armorEquipped == 68) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          } else if(attackElement == CoreMod.WATER_ELEMENT.get() && armorEquipped == 69) {
            event.damage = (int) Math.round(event.damage / 1.15d);;
          }
        }
      }
    }

    if(ultimateBattle) {
      if(event.attacker instanceof PlayerBattleEntity player && event.defender instanceof MonsterBattleEntity) {
        if(ultimatePenality[player.charSlot_276][1] > 1) { //Damage penalty for over leveled ultiamte boss
          event.damage /= ultimatePenality[player.charSlot_276][1];
        }
      }

      if(event.attacker instanceof MonsterBattleEntity && event.defender instanceof PlayerBattleEntity player) {
        if(ultimatePenality[player.charSlot_276][1] > 1) { //Damage penalty for over leveled ultiamte boss
          event.damage *= ultimatePenality[player.charSlot_276][1];
        }
      }
    }

        /*if(event.attacker instanceof MonsterBattleEntity monster && event.defender instanceof PlayerBattleEntity player) {
            try {
                System.out.println("-------------------------------");
                if(event.attackType.isPhysical()) {
                    System.out.println("[DRAMODTEST] ID:  " + monster.spellId_4e);
                    System.out.println("[DRAMODTEST] DMG: " + spellStats_800fa0b8[monster.spellId_4e].multi_04);
                } else {
                    System.out.println("[DRAMODTEST] ID:  " + monster.spellId_4e);
                    System.out.println("[DRAMODTEST] DMG: " + spellStats_800fa0b8[monster.spellId_4e].multi_04);
                    System.out.println("[DRAMODTEST] IID: " + monster.itemId_52);
                    System.out.println("[DRAMODTEST] ITM: " + monster.item_d4.damage_05);
                }
            } catch (Exception ignored) {}
        }*/

    if(ultimateBattle) { //Ultimate Boss effects per attack
      if(event.attacker instanceof MonsterBattleEntity monster) {
        ultimateGuardBreak((PlayerBattleEntity) event.defender, monster, event);
        ultimateMPAttack((PlayerBattleEntity) event.defender, monster, event);
      }
    }

    updateEnrageMode(event);
    updateElementalBomb(event);
    updateDamageTracker(event);
  }

  @EventListener public void attackSpGainEvent(final AttackSpGainEvent event) {
    final PlayerBattleEntity bent = event.bent;

    if(bent.charId_272 == 2 || bent.charId_272 == 8) {
      event.sp = Integer.parseInt(shanaSpGain.get(0)[bent.dlevel_06 - 1]);
    }
  }

  public void addBurnStacks(PlayerBattleEntity dart, int stacks) {
    if(!burnStackMode) {
      previousBurnStacks = burnStacks;
      int dlv = dart.dlevel_06;
      burnStacksMax = dlv== 0 ? 0 : dlv == 1 ? 3 : dlv == 2 ? 6 : dlv == 3 ? 9 : dlv == 7 ? 15 : 12;
      burnStacks = Math.min(burnStacksMax, burnStacks + stacks);

      if(burnStacks >= 4 && previousBurnStacks < 4) {
        dart.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(dart.stats.getStat(CoreMod.MP_STAT.get()).getCurrent() + 10);
      } else if(burnStacks >= 8 && previousBurnStacks < 8) {
        dart.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(dart.stats.getStat(CoreMod.MP_STAT.get()).getCurrent() + 20);
      } else if(burnStacks >= 12 && previousBurnStacks < 12) {
        dart.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(dart.stats.getStat(CoreMod.MP_STAT.get()).getCurrent() + 30);
      }
    }
  }

  public void dramodBurnStacks(int spellId) {
    if(spellId >= 0 && spellId <= 3) {
      if(burnStackMode && burnStacks > 0) {
        int damage = Integer.parseInt(spellStats.get(spellId)[3]);
        String newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", (1 + (burnStacks * dmgPerBurn))));

        if(burnStacks == burnStacksMax) {
          if(spellId == 0) {
            damage *= (1 + (burnStacks * dmgPerBurn)) * (Integer.parseInt(spellStats.get(2)[3]) / Integer.parseInt(spellStats.get(0)[3])) * 1.5;
            newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", ((1 + (burnStacks * dmgPerBurn)) * (Integer.parseInt(spellStats.get(2)[3]) / Integer.parseInt(spellStats.get(0)[3])) * 1.5)));
          } else if(spellId == 1)  {
            damage *= (1 + (burnStacks * dmgPerBurn)) * Integer.parseInt(spellStats.get(3)[3]) / Integer.parseInt(spellStats.get(1)[3]);
            newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", (1 + (burnStacks * dmgPerBurn)) * Integer.parseInt(spellStats.get(3)[3]) / Integer.parseInt(spellStats.get(1)[3])));
          } else if(spellId == 2)  {
            damage *= (1 + (burnStacks * dmgPerBurn)) * 1.5;
            newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", (1 + (burnStacks * dmgPerBurn)) * 1.5));
          } else {
            damage *= 1 + (burnStacks * dmgPerBurn);
          }
        } else {
          damage *= 1 + (burnStacks * dmgPerBurn) * 1.5;
        }

        spellStats_800fa0b8[spellId] = new SpellStats0c(
          spellStats.get(spellId)[12],
          newDescription,
          Integer.parseInt(spellStats.get(spellId)[0]),
          Integer.parseInt(spellStats.get(spellId)[1]),
          Integer.parseInt(spellStats.get(spellId)[2]),
          damage,
          Integer.parseInt(spellStats.get(spellId)[4]),
          Integer.parseInt(spellStats.get(spellId)[5]),
          burnStacks == burnStacksMax ? 0 : Integer.parseInt(spellStats.get(spellId)[6]),
          Integer.parseInt(spellStats.get(spellId)[7]),
          Element.fromFlag(Integer.parseInt(spellStats.get(spellId)[8])),
          Integer.parseInt(spellStats.get(spellId)[9]),
          Integer.parseInt(spellStats.get(spellId)[10]),
          Integer.parseInt(spellStats.get(spellId)[11])
        );
      } else {
        spellStats_800fa0b8[spellId] = new SpellStats0c(
          spellStats.get(spellId)[12],
          spellStats.get(spellId)[13],
          Integer.parseInt(spellStats.get(spellId)[0]),
          Integer.parseInt(spellStats.get(spellId)[1]),
          Integer.parseInt(spellStats.get(spellId)[2]),
          Integer.parseInt(spellStats.get(spellId)[3]),
          Integer.parseInt(spellStats.get(spellId)[4]),
          Integer.parseInt(spellStats.get(spellId)[5]),
          Integer.parseInt(spellStats.get(spellId)[6]),
          Integer.parseInt(spellStats.get(spellId)[7]),
          Element.fromFlag(Integer.parseInt(spellStats.get(spellId)[8])),
          Integer.parseInt(spellStats.get(spellId)[9]),
          Integer.parseInt(spellStats.get(spellId)[10]),
          Integer.parseInt(spellStats.get(spellId)[11])
        );
      }
    }
  }

  @EventListener public void spellStats(final SpellStatsEvent spell) {
    int spellId = spell.spellId;

    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());

    if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      dramodBurnStacks(spellId);
    }
  }

  @EventListener public void dragonBlockStaffOn(final DragonBlockStaffOnEvent event) {
    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());
    if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      dragonBlockStaff = true;
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof PlayerBattleEntity player) {
          player.dragoonAttack_ac = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[3]) * 8;
          player.dragoonMagic_ae = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[4]) * 8;
          player.dragoonDefence_b0 = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[5]) * 8;
          player.dragoonMagicDefence_b2 = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[6]) * 8;
        }
      }
    }
  }

  @EventListener public void dragonBlockStaffOff(final DragonBlockStaffOffEvent event) {
    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());
    if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      dragonBlockStaff = false;
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof PlayerBattleEntity player) {
          player.dragoonAttack_ac = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[3]);
          player.dragoonMagic_ae = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[4]);
          player.dragoonDefence_b0 = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[5]);
          player.dragoonMagicDefence_b2 = Integer.parseInt(dragoonStatsTable.get(player.charId_272 * (maxDragoonLevel + 1) + player.dlevel_06)[6]);
        }
      }
    }
  }

  @EventListener public void repeatItemReturn(final RepeatItemReturnEvent event) {
    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());

    if(difficulty.equals("Japan Demo")) {
      event.returnItem = event.itemId == 250; //TODO in SC this should be registryID
    }
  }

  public void updateEnrageMode(final AttackEvent event) {
    if(GameEngine.CONFIG.getConfig(ENRAGE_MODE.get()) == EnrageMode.ON) {
      for (int i = 0; i < battleState_8006e398.getMonsterCount(); i++) {
        final MonsterBattleEntity monster = battleState_8006e398.monsterBents_e50[i].innerStruct_00;
        int hp = monster.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
        int maxHp = monster.stats.getStat(CoreMod.HP_STAT.get()).getMax();
        if(hp <= maxHp / 2 && enrageMode[i] == 0) {
          monster.attack_34 = (int) Math.round(monster.attack_34 * 1.1d);
          monster.magicAttack_36 = (int) Math.round(monster.magicAttack_36 * 1.1d);
          monster.defence_38 = (int) Math.round(monster.defence_38 * 1.1d);
          monster.magicDefence_3a = (int) Math.round(monster.magicDefence_3a * 1.1d);
          enrageMode[i] = 1;
        }
        if(hp <= maxHp / 4 && enrageMode[i] == 1) {
          monster.attack_34 = (int) Math.round(monster.attack_34 * 1.136365d);
          monster.magicAttack_36 = (int) Math.round(monster.magicAttack_36 * 1.136365d);
          monster.defence_38 = (int) Math.round(monster.defence_38 * 1.136365d);
          monster.magicDefence_3a = (int) Math.round(monster.magicDefence_3a * 1.136365d);
          enrageMode[i] = 2;
        }
      }
    }
  }

  public void updateItemMagicDamage() {
    if(GameEngine.CONFIG.getConfig(DAMAGE_TRACKER.get()) == DamageTracker.ON) {
      for (int i = 0; i < battleState_8006e398.getMonsterCount(); i++) {
        final MonsterBattleEntity monster = battleState_8006e398.monsterBents_e50[i].innerStruct_00;
        int hp = monster.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
        if(hp < damageTrackerPreviousHP[monster.charSlot_276]) {
          int difference = damageTrackerPreviousHP[monster.charSlot_276] - hp;
          damageTracker[damageTrackerPreviousCharacter][damageTrackerPreviousAttackType] += difference;
          damageTrackerLog.add(charNames[damageTrackerPreviousCharacterID] + " - Multiplier - " + difference);
          damageTrackerPreviousHP[monster.charSlot_276] = hp;
        }
      }
    }
  }

  public void updateElementalBomb(final AttackEvent event) { //TODO item registries
    if(GameEngine.CONFIG.getConfig(ELEMENTAL_BOMB.get()) == ElementalBomb.ON) {
      if(event.attacker instanceof PlayerBattleEntity player) {
        try {
          if(player.itemId_52 >= 49 && player.itemId_52 != 57 && event.defender instanceof MonsterBattleEntity monster) {
            //for (int i = 0; i < monsterCount_800c6768.get(); i++) {
            if(elementalBombTurns[monster.charSlot_276] == 0) {
              Element swapTo;
              if(player.itemId_52 == 50) { //Burning Wave
                swapTo = Element.fromFlag(0x80);
              } else if(player.itemId_52 == 51) { //Frozen Jet
                swapTo = Element.fromFlag(0x1);
              } else if(player.itemId_52 == 52) { //Down Burst
                swapTo = Element.fromFlag(0x40);
              } else if(player.itemId_52 == 53) { //Gravity Grabber
                swapTo = Element.fromFlag(0x2);
              } else if(player.itemId_52 == 54) { //Spectral Flash
                swapTo = Element.fromFlag(0x20);
              } else if(player.itemId_52 == 55) { //Night Raid
                swapTo = Element.fromFlag(0x4);
              } else if(player.itemId_52 == 56) { //Flash Hall
                swapTo = Element.fromFlag(0x10);
              } else { //Psyche Bomb
                swapTo = Element.fromFlag(0x8);
              }
              elementalBombPreviousElement[monster.charSlot_276] = monster.getElement();
              elementalBombTurns[monster.charSlot_276] = 5;
              monster.monsterElement_72 = swapTo;
              monster.displayElement_1c = swapTo;
            }
          }
          //}
        } catch (Exception ignored) {}
      }
    }
  }

  @EventListener public void shanaItemSpGain(AttackEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
      if(event.attacker instanceof PlayerBattleEntity player && event.defender instanceof MonsterBattleEntity monster) {
        if((player.charId_272 == 2 || player.charId_272 == 8) && player.itemId_52 > 0) {
          final int sp = player.getStat(BattleEntityStat.CURRENT_SP);
          spGained_800bc950[player.charSlot_276] += 50;
          player.setStat(BattleEntityStat.CURRENT_SP, sp + 50);
          player.itemId_52 = 0;
        }
      }
    }
  }

  public void updateDamageTracker(final AttackEvent attack) {
    if(GameEngine.CONFIG.getConfig(DAMAGE_TRACKER.get()) == DamageTracker.ON) {
      if(attack.attacker instanceof PlayerBattleEntity player && attack.defender instanceof MonsterBattleEntity monster) {
        if(player.isDragoon()) {
          if(attack.attackType.isPhysical()) {
            damageTrackerPreviousAttackType = 0;
            damageTracker[player.charSlot_276][0] += attack.damage;
            damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - D.Physical - " + attack.damage);
          } else {
            damageTrackerPreviousAttackType = 1;
            damageTracker[player.charSlot_276][1] += attack.damage;
            damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - D.Magical - " + attack.damage);
          }
        } else {
          if(attack.attackType.isPhysical()) {
            damageTrackerPreviousAttackType = 2;
            damageTracker[player.charSlot_276][2] += attack.damage;
            damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - Physical - " + attack.damage);
          } else {
            damageTrackerPreviousAttackType = 3;
            damageTracker[player.charSlot_276][3] += attack.damage;
            damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - Magical - " + attack.damage);
          }
        }

        int hp = monster.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
        if(attack.damage > hp && hp > 0 && hp != damageTrackerPreviousHP[monster.charSlot_276]) {
          damageTracker[player.charSlot_276][4] = attack.damage - hp;
        }

        damageTrackerPreviousCharacter = player.charSlot_276;
        damageTrackerPreviousCharacterID = player.charId_272;
        damageTrackerPreviousHP[monster.charSlot_276] = hp - attack.damage;
      }
    }
  }

  public void ultimateZeroSPStart(final PlayerBattleEntity player) {
    int encounterId = encounterId_800bb0f8;

    if(encounterId == 413 || encounterId == 415 || encounterId == 403) {
      player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(0);
    }
  }

  public void updateMonsterHPNames() {
    if(GameEngine.CONFIG.getConfig(MONSTER_HP_NAMES.get()) == MonsterHPNames.ON) {
      for (int i = 0; i < 10; i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        if(state != null) {
          final BattleEntity27c bobj = state.innerStruct_00;
          if(bobj instanceof MonsterBattleEntity) {
            int hp = bobj.stats.getStat(CoreMod.HP_STAT.get()).getCurrent();
            ((Battle)currentEngineState_8004dd04).currentEnemyNames_800c69d0[bobj.charSlot_276] = String.valueOf(hp);
          }
        }
      }
    }
  }

  @EventListener public void statDisplay(StatDisplayEvent event) {
    if(event.player.charId_272 == 0 && burnStacksMax > 0) {
      final MV transforms = new MV();
      final float burn =  burnStacks == 0 ? 0.0f : (float) burnStacks / burnStacksMax;
      transforms.transfer.set(event.charSlot * 94 + 16, 226.0, 124.0f);
      transforms.scaling(92.0f, 12.0f, 1.0f);
      RENDERER
        .queueOrthoModel(RENDERER.opaqueQuad, transforms)
        .screenspaceOffset(0.0f, 0.0f)
        .monochrome(0.0f);

      transforms.transfer.set(event.charSlot * 94 + 16, 226.0, 120.0f);
      transforms.scaling(92.0f * burn, 12.0f, 1.0f);
      RENDERER
        .queueOrthoModel(RENDERER.opaqueQuad, transforms)
        .screenspaceOffset(0.0f, 0.0f)
        .colour(1.0f, 0.0f, 0.0f);

      Scus94491BpeSegment_8002.renderText(String.valueOf(burnStacks), event.charSlot * 94 + 16, 226.0f, TextColour.WHITE, 0);
    }
  }

  @EventListener public void selectedTarget(final SingleMonsterTargetEvent event) {
    for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
      final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
      final BattleEntity27c bent = state.innerStruct_00;
      if(bent instanceof PlayerBattleEntity player) {
        if(player.charId_272 == 1 || player.charId_272 == 5) {
          final MV transforms = new MV();
          final float wind =  windMark[event.monster.charSlot_276] == 0 ? 0.0f : (float) windMark[event.monster.charSlot_276] / 3;
          transforms.transfer.set(player.charSlot_276 * 94 + 16, 226.0, 124.0f);
          transforms.scaling(92.0f, 12.0f, 1.0f);
          RENDERER
            .queueOrthoModel(RENDERER.opaqueQuad, transforms)
            .screenspaceOffset(0.0f, 0.0f)
            .monochrome(0.0f);

          transforms.transfer.set(player.charSlot_276 * 94 + 16, 226.0, 120.0f);
          transforms.scaling(92.0f * wind, 12.0f, 1.0f);
          RENDERER
            .queueOrthoModel(RENDERER.opaqueQuad, transforms)
            .screenspaceOffset(0.0f, 0.0f)
            .colour(0.0f, 1.0f, 0.0f);

          Scus94491BpeSegment_8002.renderText(String.valueOf(windMark[event.monster.charSlot_276]), player.charSlot_276 * 94 + 16, 226.0f, TextColour.WHITE, 0);
        } else if(player.charId_272 == 4) {
          final MV transforms = new MV();
          final float thunder = thunderCharge[event.monster.charSlot_276] == 0 ? 0.0f : (float) thunderCharge[event.monster.charSlot_276] / 10;
          transforms.transfer.set(player.charSlot_276 * 94 + 16, 226.0, 124.0f);
          transforms.scaling(92.0f, 12.0f, 1.0f);
          RENDERER
            .queueOrthoModel(RENDERER.opaqueQuad, transforms)
            .screenspaceOffset(0.0f, 0.0f)
            .monochrome(0.0f);

          transforms.transfer.set(player.charSlot_276 * 94 + 16, 226.0, 120.0f);
          transforms.scaling(92.0f * thunder, 12.0f, 1.0f);
          RENDERER
            .queueOrthoModel(RENDERER.opaqueQuad, transforms)
            .screenspaceOffset(0.0f, 0.0f)
            .colour(0.63f, 0.0f, 1.0f);

          Scus94491BpeSegment_8002.renderText(String.valueOf(thunderCharge[event.monster.charSlot_276]), player.charSlot_276 * 94 + 16, 226.0f, TextColour.WHITE, 0);
        }
      }
    }
  }

  @EventListener public void battleEnded(final BattleEndedEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    updateItemMagicDamage();

    if(faustBattle) {
      faustBattle = false;
      try {
        GameEngine.CONFIG.setConfig(FAUST_DEFEATED.get(), String.valueOf(Integer.parseInt(GameEngine.CONFIG.getConfig(FAUST_DEFEATED.get())) + 1));
      } catch (NumberFormatException ex) {
        GameEngine.CONFIG.setConfig(FAUST_DEFEATED.get(), String.valueOf(1));
      }
    }

    if(ultimateBattle) {
      ultimateBattle = false;

      int ultimateBossesDefeated = Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get()));
      int ultimateBossSelected = GameEngine.CONFIG.getConfig(ULTIMATE_BOSS.get()) - 1;
      int mapId = submapCut_80052c30;

      if(mapId >= 393 && mapId <= 394) {
        if(ultimateBossSelected > 2 && ultimateBossesDefeated > 2) {
          ultimateBossSelected = 2;
        } else {
          if(ultimateBossSelected > ultimateBossesDefeated) {
            ultimateBossSelected = ultimateBossesDefeated;
          }
        }
      } else if(mapId >= 395 && mapId <= 397) {
        if(ultimateBossSelected > 7 && ultimateBossesDefeated > 7) {
          ultimateBossSelected = 7;
        } else {
          if(ultimateBossSelected > ultimateBossesDefeated) {
            ultimateBossSelected = ultimateBossesDefeated;
          }
        }
      } else if(mapId >= 398 && mapId <= 400) {
        if(ultimateBossSelected > 21 && ultimateBossesDefeated > 21) {
          ultimateBossSelected = 21;
        } else {
          if(ultimateBossSelected > ultimateBossesDefeated) {
            ultimateBossSelected = ultimateBossesDefeated;
          }
        }
      }

      if(ultimateBossesDefeated == ultimateBossSelected) {
        GameEngine.CONFIG.setConfig(ULTIMATE_BOSS_DEFEATED.get(), String.valueOf(Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get())) + 1));
      }

      if(Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get())) == 3) {
        GameEngine.CONFIG.setConfig(CoreMod.INVENTORY_SIZE_CONFIG.get(), 36);
      } else if(Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get())) == 8) {
        GameEngine.CONFIG.setConfig(CoreMod.INVENTORY_SIZE_CONFIG.get(), 40);
      }
    }

    if(GameEngine.CONFIG.getConfig(DAMAGE_TRACKER.get()) == DamageTracker.ON && !damageTrackerPrinted && gameState_800babc8.charIds_88[0] >= 0 && gameState_800babc8.charIds_88[1] >= 0 && gameState_800babc8.charIds_88[2] >= 0) {
      try {
        double total = IntStream.of(damageTracker[0]).sum() + IntStream.of(damageTracker[1]).sum() + IntStream.of(damageTracker[2]).sum();
        PrintWriter pw = new PrintWriter("./mods/Damage Tracker/" + new SimpleDateFormat("yyyy-MMdd--hh-mm-ss").format(new Date()) + " - E" + encounterId_800bb0f8 + ".txt");
        pw.printf("======================================================================%n");
        pw.printf("=                           Damage Tracker                           =%n");
        pw.printf("======================================================================%n");
        pw.printf("| %-20s | %-20s | %-20s |%n", charNames[gameState_800babc8.charIds_88[0]], charNames[gameState_800babc8.charIds_88[1]], charNames[gameState_800babc8.charIds_88[2]]);
        pw.printf("----------------------------------------------------------------------%n");
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "D.Physical", damageTracker[0][0], "D.Physical", damageTracker[1][0], "D.Physical", damageTracker[2][0]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "D.Magical", damageTracker[0][1], "D.Magical",  damageTracker[1][1],"D.Magical",  damageTracker[2][1]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "Physical", damageTracker[0][2], "Physical",  damageTracker[1][2],"Physical",  damageTracker[2][2]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "Magical", damageTracker[0][3], "Magical",  damageTracker[1][3],"Magical",  damageTracker[2][3]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "Total", IntStream.of(damageTracker[0]).sum(), "Total",  IntStream.of(damageTracker[1]).sum(), "Total",  IntStream.of(damageTracker[2]).sum());
        pw.printf("----------------------------------------------------------------------%n");
        pw.printf("%-13s %.2f%%%n", charNames[gameState_800babc8.charIds_88[0]], (IntStream.of(damageTracker[0]).sum() - damageTracker[0][4] * 2) / total * 100);
        pw.printf("%-13s %.2f%%%n", charNames[gameState_800babc8.charIds_88[1]], (IntStream.of(damageTracker[1]).sum() - damageTracker[1][4] * 2) / total * 100);
        pw.printf("%-13s %.2f%%%n", charNames[gameState_800babc8.charIds_88[2]], (IntStream.of(damageTracker[2]).sum() - damageTracker[2][4] * 2) / total * 100);
        pw.printf("Grand Total   " + total + "%n");
        pw.printf("Encounter     " + encounterId_800bb0f8 + "%n%n");
        pw.printf("===========================================================================================================%n");
        pw.printf("=                                                Equipment                                                =%n");
        pw.printf("===========================================================================================================%n");
        pw.printf("| Name     | Weapon           | Helmet           | Armor            | Shoes            | Accessory        |%n");
        pw.printf("-----------------------------------------------------------------------------------------------------------%n");
        for (int i = 0; i < damageTrackerEquips.length; i++) {
          pw.printf("| %-8s | %-16s | %-16s | %-16s | %-16s | %-16s |%n", charNames[gameState_800babc8.charIds_88[i]], equipStats.get(damageTrackerEquips[i][0])[29], equipStats.get(damageTrackerEquips[i][1])[29], equipStats.get(damageTrackerEquips[i][2])[29], equipStats.get(damageTrackerEquips[i][3])[29], equipStats.get(damageTrackerEquips[i][4])[29]);
        }
        pw.printf("===========================================================================================================%n%n");
        for (String s : damageTrackerLog) {
          pw.printf(s + "%n");
        }
        pw.flush();
        pw.close();
        damageTrackerPrinted = true;
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
  //endregion

  //region Menu Events
  @EventListener public void xpToLevel(final XpToLevelEvent event) {
    //event.xp = Integer.parseInt(xpNextStats.get(event.charId * (maxCharacterLevel + 1) + event.level)[0]);
  }

  @EventListener public void shopType(final ShopTypeEvent event) {
    event.shopType = shopItems.get(event.shopId)[0].substring(0, 1).equals("e") ? 0 : 1;
  }

  @EventListener public void shopEquipment(final ShopEquipmentEvent event) {
    event.equipment.clear();

    for(int i = 0; i < 16; i++) {
      String id = shopItems.get(event.shopId)[i];
      if(!id.startsWith("N")) {
        final Equipment equipment = REGISTRIES.equipment.getEntry(equipmentIdMap.get(Integer.parseInt(id.substring(1, id.length())))).get();
        event.equipment.add(new ShopScreen.ShopEntry<>(equipment, equipment.getPrice() * 2));
      }
    }
  }

  @EventListener public void shopItem(final ShopItemEvent event) {
    event.items.clear();
    for(int i = 0; i < 16; i++) {
      String id = shopItems.get(event.shopId)[i];
      if(!id.startsWith("N")) {
        final Item item = REGISTRIES.items.getEntry(itemIdMap.get(Integer.parseInt(id.substring(1, id.length())))).get();
        event.items.add(new ShopScreen.ShopEntry<>(item, item.getPrice() * 2));
      }
    }

    /* TODO hero competition shop
    if(event.shopId == 40) {
      if(event.itemId == 211) {
        event.price = 300;
      } else if(event.itemId == 221) {
        event.price = 600;
      } else if(event.itemId == 233) {
        event.price = 900;
      }
    } else if(event.shopId == 41) {
      event.price = 1000;
    }
     */
  }

  /*@EventListener public void shopSellPrice(final ShopSellPriceEvent shopItem) {
    shopItem.price = Integer.parseInt(shopPrices.get(shopItem.itemId)[0]);
  }*/

  //endregion

  //region Ultimate
  public void ultimateGuardBreak(final PlayerBattleEntity player, final MonsterBattleEntity monster, final AttackEvent attack) {
    int encounterId = encounterId_800bb0f8;

    if(encounterId == 415) {
      if(!attack.attackType.isPhysical()) {
        if(monster.spellId_4e == 117) {
          player.guard_54 = 0;
        }
      }
    }
  }


  public void ultimateMPAttack(final PlayerBattleEntity player, final MonsterBattleEntity monster, final AttackEvent attack) {
    int encounterId = encounterId_800bb0f8;

    if(attack.damage > 0) {
      if(encounterId == 415) {
        if(attack.attackType.isPhysical()) {
          if(monster.spellId_4e == 33) {
            player.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(Math.max(0, player.stats.getStat(CoreMod.MP_STAT.get()).getCurrent() - 10));
          }
        }
      }
    }
  }
  //endregion

  //region Hotkey
  @EventListener public void inputPressed(final InputPressedEvent event) {
    hotkey.add(event.inputAction);
    dramodHotkeys();
  }

  @EventListener public void inputReleased(final InputReleasedEvent event) {
    hotkey.remove(event.inputAction);
  }

  public void dramodHotkeys() {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());

    if(engineState_8004dd20 == EngineStateEnum.COMBAT_06) { // Combat
      if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1) && hotkey.contains(InputAction.DPAD_UP)) { //Exit Dragoon Slot 1
        if(battleState_8006e398.dragoonTurns_294[0] > 0) {
          battleState_8006e398.dragoonTurns_294[0] = 1;
        }
      } else if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1) && hotkey.contains(InputAction.DPAD_RIGHT)) { //Exit Dragoon Slot 2
        if(battleState_8006e398.dragoonTurns_294[1] > 0) {
          battleState_8006e398.dragoonTurns_294[1] = 1;
        }
      } else if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1) && hotkey.contains(InputAction.DPAD_LEFT)) { //Exit Dragoon Slot 3
        if(battleState_8006e398.dragoonTurns_294[2] > 0) {
          battleState_8006e398.dragoonTurns_294[2] = 1;
        }
      }

      if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
        if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_WEST)) { //Burn Stacks Mode
          if(burnStacks > 0) {
            burnStackMode = !burnStackMode;
          }
        } else if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && hotkey.contains(InputAction.DPAD_UP)) { //Dragoon Guard Slot 1
          PlayerBattleEntity player = battleState_8006e398.playerBents_e40[0].innerStruct_00;
          int dragoonTurns = battleState_8006e398.dragoonTurns_294[0];
          int sp = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
          if(player.isDragoon() && player.dlevel_06 >= 6 && dragoonTurns > 1 && sp >= 100) {
            battleState_8006e398.dragoonTurns_294[0] -= 1;
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(sp - 100);
            player.guard_54 = 1;
          }
        } else if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && hotkey.contains(InputAction.DPAD_RIGHT)) { //Dragoon Guard Slot 2
          PlayerBattleEntity player = battleState_8006e398.playerBents_e40[1].innerStruct_00;
          int dragoonTurns = battleState_8006e398.dragoonTurns_294[1];
          int sp = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
          if(player.isDragoon() && player.dlevel_06 >= 6 && dragoonTurns > 1 && sp >= 100) {
            battleState_8006e398.dragoonTurns_294[1] -= 1;
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(sp - 100);
            player.guard_54 = 1;
          }
        } else if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && hotkey.contains(InputAction.DPAD_LEFT)) { //Dragoon Guard Slot 3
          PlayerBattleEntity player = battleState_8006e398.playerBents_e40[2].innerStruct_00;
          int dragoonTurns = battleState_8006e398.dragoonTurns_294[2];
          int sp = player.stats.getStat(CoreMod.SP_STAT.get()).getCurrent();
          if(player.isDragoon() && player.dlevel_06 >= 6 && dragoonTurns > 1 && sp >= 100) {
            battleState_8006e398.dragoonTurns_294[2] -= 1;
            player.stats.getStat(CoreMod.SP_STAT.get()).setCurrent(sp - 100);
            player.guard_54 = 1;
          }
        } else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_1)) { // Shana Rapid fire
          for(int i = 0; i < 0x48; i++) {
            try {
              final ScriptState<?> state = scriptStatePtrArr_800bc1c0[i];
              if((state.name.contains("Char ID 2") || state.name.contains("Char Id 8"))) {
                for(int x = 0; x < battleState_8006e398.getAllBentCount(); x++) {
                  final ScriptState<? extends BattleEntity27c> playerstate = battleState_8006e398.allBents_e0c[x];
                  final BattleEntity27c bobj = playerstate.innerStruct_00;
                  if(bobj instanceof PlayerBattleEntity player) {
                    if(player.isDragoon() && shanaRapidFireContinue[player.charSlot_276]) {
                      if(scriptStatePtrArr_800bc1c0[i].offset_18 == 0x1d2) {
                        scriptStatePtrArr_800bc1c0[i].offset_18 = 0x2050; //TODO not this lol
                        shanaRapidFireCount[player.charSlot_276]++;
                        if(shanaRapidFireCount[player.charSlot_276] == 2) {
                          shanaRapidFireContinue[player.charSlot_276] = false;
                        }
                      }
                      break;
                    }
                  }
                }
              }
            } catch (Exception ignored) {}
          }
        } else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1)) { //Shana Rapid Fire Activator
          for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
            final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
            final BattleEntity27c bobj = state.innerStruct_00;
            if(bobj instanceof PlayerBattleEntity player) {
              if((player.charId_272 == 2 || player.charId_272 == 8) && player.charSlot_276 == currentPlayerSlot && !shanaRapidFire[player.charSlot_276] && player.isDragoon() && player.dlevel_06 >= 6) {
                int mp = player.stats.getStat(CoreMod.MP_STAT.get()).getCurrent();
                if(mp >= 20) {
                  player.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(mp - 20);
                  shanaRapidFire[player.charSlot_276] = true;
                  shanaRapidFireContinue[player.charSlot_276] = true;
                  shanaRapidFireCount[player.charSlot_276] = 0;
                  player.dragoonAttack_ac = dragonBlockStaff ? 165 * 8 : 165;
                }
              }
            }
          }
                /*} else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_2)) { //Shana Light Element Arrow
                    for(int i = 0; i < allBobjCount_800c66d0.get(); i++) {
                        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
                        final BattleEntity27c bobj = state.innerStruct_00;
                        if(bobj instanceof PlayerBattleEntity) {
                            PlayerBattleEntity player = (PlayerBattleEntity) bobj;
                            if((player.charId_272 == 2 || player.charId_272 == 8) && player.charSlot_276 == currentPlayerSlot && player.isDragoon() && player.dlevel_06 >= 7) {
                                int mp = player.stats.getStat(CoreMod.MP_STAT.get()).getCurrent();
                                if(mp >= 100) {
                                    player.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(mp - 100);
                                    previousElement[player.charSlot_276] = player.element;
                                    elementalAttack[player.charSlot_276] = true;
                                    player.element = Element.fromFlag(32);
                                    player.dragoonAttack_ac = dragonBlockStaff ? 550 * 8 : 550;
                                }
                            }
                        }
                    }*/
        } else if(hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_2)) { //Meru Boost
          for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
            final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
            final BattleEntity27c bobj = state.innerStruct_00;
            if(bobj instanceof PlayerBattleEntity) {
              PlayerBattleEntity player = (PlayerBattleEntity) bobj;
              if(player.charId_272 == 6 && player.charSlot_276 == currentPlayerSlot && player.isDragoon() && player.dlevel_06 >= 7) {
                int mp = player.stats.getStat(CoreMod.MP_STAT.get()).getCurrent();
                if(mp >= 100) {
                  int maxHP = player.stats.getStat(CoreMod.HP_STAT.get()).getMax();
                  player.stats.getStat(CoreMod.MP_STAT.get()).setCurrent(mp - 100);
                  meruBoost[player.charSlot_276] = true;
                  meruBoostTurns[player.charSlot_276] = 5;
                  meruMDFSave[player.charSlot_276] = player.magicDefence_3a;
                  meruMaxHpSave[player.charSlot_276] = maxHP;
                  player.stats.getStat(CoreMod.HP_STAT.get()).setMaxRaw(maxHP * 3);
                  player.stats.getStat(CoreMod.HP_STAT.get()).setCurrent(maxHP * 3);
                }
              }
            }
          }
        }
      }
    } else {
      if(hotkey.contains(InputAction.BUTTON_CENTER_1) && hotkey.contains(InputAction.BUTTON_THUMB_1)) { //Add Shana
        gameState_800babc8.charData_32c[2].partyFlags_04 = gameState_800babc8.charData_32c[2].partyFlags_04 == 0 ? 3 : 0;
      } else if(hotkey.contains(InputAction.BUTTON_CENTER_1) && hotkey.contains(InputAction.BUTTON_THUMB_2)) { //Add Lavitz
        gameState_800babc8.charData_32c[1].partyFlags_04 = gameState_800babc8.charData_32c[1].partyFlags_04 == 0 ? 3 : 0;
      } else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_1)) { //Add Dragoons Start
        int mapId = submapCut_80052c30;
        if(mapId == 10) {
          gameState_800babc8.goods_19c[0] ^= 1 << 0;
          gameState_800babc8.goods_19c[0] ^= 1 << 1;
          gameState_800babc8.goods_19c[0] ^= 1 << 2;
          gameState_800babc8.goods_19c[0] ^= 1 << 3;
          gameState_800babc8.goods_19c[0] ^= 1 << 4;
          gameState_800babc8.goods_19c[0] ^= 1 << 5;
          gameState_800babc8.goods_19c[0] ^= 1 << 6;
        }
      } else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1)) { //Solo/All Character Start
        int mapId = submapCut_80052c30;
        if(mapId == 10) {
          for (int i = 0; i < 9; i++) {
            gameState_800babc8.charData_32c[i].partyFlags_04 = 3;
            gameState_800babc8.charData_32c[i].dlevel_13 = 1;
            gameState_800babc8.charData_32c[i].level_12 = 1;
            gameState_800babc8.charData_32c[i].xp_00 = 0;
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("dragoon_modifier:e0").get());
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("dragoon_modifier:e76").get());
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("dragoon_modifier:e46").get());
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("dragoon_modifier:e93").get());
          }
          gameState_800babc8.goods_19c[0] ^= 1 << 0;
          gameState_800babc8.goods_19c[0] ^= 1 << 1;
          gameState_800babc8.goods_19c[0] ^= 1 << 2;
          gameState_800babc8.goods_19c[0] ^= 1 << 3;
          gameState_800babc8.goods_19c[0] ^= 1 << 4;
          gameState_800babc8.goods_19c[0] ^= 1 << 5;
          gameState_800babc8.goods_19c[0] ^= 1 << 6;
        } else if(mapId == 232) { //Add Dart Dragoon Back
          gameState_800babc8.goods_19c[0] ^= 1 << 0;
        } else if(mapId == 424 || mapId == 736) { //Divine Dragoon Swap
          if((difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) && Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get())) >= 34) {
            gameState_800babc8.goods_19c[0] ^= 1 << 7;
            if(mapId == 736) {
              gameState_800babc8.goods_19c[0] |= 1 << 0;
            }
          } else {
            gameState_800babc8.goods_19c[0] ^= 1 << 7;
            if(mapId == 736) {
              gameState_800babc8.goods_19c[0] |= 1 << 0;
            }
          }
        } else if(mapId == 729) { //Warp out of Moon
          submapCut_80052c30 = 527;
          ((SMap)currentEngineState_8004dd04).smapLoadingStage_800cb430 = SubmapState.CHANGE_SUBMAP_4;
        } else if(mapId == 526 || mapId == 527) { // TODO: Story flag check here // Warp to Moon
          submapCut_80052c30 = 730;
          ((SMap)currentEngineState_8004dd04).smapLoadingStage_800cb430 = SubmapState.CHANGE_SUBMAP_4;
        } else if(mapId == 732) { //Faust Battle
          encounterId_800bb0f8 = 420;

          if(engineState_8004dd20 == EngineStateEnum.SUBMAP_05) {
            battleStage_800bb0f4 = 78;
            ((SMap)currentEngineState_8004dd04).mapTransition(-1, 0);
          } else if(engineState_8004dd20 == EngineStateEnum.WORLD_MAP_08) {
            battleStage_800bb0f4 = 78;
            gameState_800babc8.directionalPathIndex_4de = ((WMap)currentEngineState_8004dd04).mapState_800c6798.directionalPathIndex_12;
            gameState_800babc8.pathIndex_4d8 = ((WMap)currentEngineState_8004dd04).mapState_800c6798.pathIndex_14;
            gameState_800babc8.dotIndex_4da = ((WMap)currentEngineState_8004dd04).mapState_800c6798.dotIndex_16;
            gameState_800babc8.dotOffset_4dc = ((WMap)currentEngineState_8004dd04).mapState_800c6798.dotOffset_18;
            gameState_800babc8.facing_4dd = ((WMap)currentEngineState_8004dd04).mapState_800c6798.facing_1c;
            ((WMap)currentEngineState_8004dd04).wmapState_800bb10c = WmapState.TRANSITION_TO_BATTLE_8;
          }

          faustBattle = true;
        }
      } else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_THUMB_2)) { //Add all party members back
        for (int i = 0; i < 9; i++) {
          gameState_800babc8.charData_32c[i].partyFlags_04 = 3;
        }
      } else if(hotkey.contains(InputAction.BUTTON_SOUTH) && hotkey.contains(InputAction.BUTTON_CENTER_2)) { //???
        gameState_800babc8.charData_32c[8].partyFlags_04 = 0;
      } else if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_EAST)) { //Level Up Party
        int highestInPartyEXP = 0;
        boolean maxedSwapEXP = false;
        for (int i = 0; i < 9; i++) {
          if(gameState_800babc8.charData_32c[i].partyFlags_04 > 0 && gameState_800babc8.charData_32c[i].xp_00 > highestInPartyEXP) {
            highestInPartyEXP = gameState_800babc8.charData_32c[i].xp_00;
          }
        }

        if(difficulty.equals("Hard Mode") || difficulty.equals("Us + Hard Bosses")) {
          if(highestInPartyEXP > 80000) {
            maxedSwapEXP = true;
          }
        }

        if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
          if(highestInPartyEXP > 160000) {
            maxedSwapEXP = true;
          }
        }

        if(!maxedSwapEXP) {
          for (int i = 0; i < 9; i++) {
            if(gameState_800babc8.charData_32c[i].partyFlags_04 > 0) {
              while (highestInPartyEXP > getXpToNextLevel(i)) {
                gameState_800babc8.charData_32c[i].level_12++;
              }
            }
          }
        }
      } else if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_WEST)) {
        if(!swappedEXP) {
          swappedEXP = true;
          System.out.println("[Dragoon Modifier] Preparing Switch EXP...");
          System.arraycopy(gameState_800babc8.charIds_88, 0, swapEXPParty, 0, 3);
        } else {
          swappedEXP = false;
          int slot1 = -1;
          int slot2 = -1;
          for (int i = 0; i < 3; i++) {
            if(swapEXPParty[i] != gameState_800babc8.charIds_88[i]) {
              slot1 = i;
            }
          }

          for (int i = 0; i < 3; i++) {
            if(swapEXPParty[slot1] == gameState_800babc8.charIds_88[i]) {
              slot2 = i;
              int char1 = gameState_800babc8.charIds_88[slot1];
              int char2 = gameState_800babc8.charIds_88[slot2];
              int slot1EXP = gameState_800babc8.charData_32c[char1].xp_00;
              int slot2EXP = gameState_800babc8.charData_32c[char2].xp_00;
              boolean disableSwap = false;

              if(difficulty.equals("Hard Mode") || difficulty.equals("Us + Hard Bosses")) {
                if(slot1EXP > 80000 || slot2EXP > 80000) {
                  disableSwap = true;
                }
              }

              if(difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
                if(slot1EXP > 160000 || slot2EXP > 160000) {
                  disableSwap = true;
                }
              }

              if(!disableSwap) {
                gameState_800babc8.charData_32c[char1].xp_00 = slot2EXP;
                gameState_800babc8.charData_32c[char2].xp_00 = slot1EXP;
              }
            }
          }

          if(slot1 >= 0 && slot2 >= 0) {
            System.out.println("[Dragoon Modifier] EXP Switched.");
          } else {
            System.out.println("[Dragoon Modifier] Switch EXP character removed from party.");
          }
        }
      } else if(hotkey.contains(InputAction.BUTTON_EAST) && hotkey.contains(InputAction.BUTTON_CENTER_2)) {
        if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
          int mapId = submapCut_80052c30;
          if(mapId >= 393 && mapId <= 405) {
            if(gameState_800babc8.chapterIndex_98 == 3) {
              int ultimateBossesDefeated = Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get()));
              int ultimateBossSelected = GameEngine.CONFIG.getConfig(ULTIMATE_BOSS.get()) - 1;

              if(mapId >= 393 && mapId <= 394) {
                if(ultimateBossSelected > 2 && ultimateBossesDefeated > 2) {
                  ultimateBossSelected = 2;
                } else {
                  if(ultimateBossSelected > ultimateBossesDefeated) {
                    ultimateBossSelected = ultimateBossesDefeated;
                  }
                }
                ultimateLevelCap = 30;
              } else if(mapId >= 395 && mapId <= 397) {
                if(ultimateBossSelected > 7 && ultimateBossesDefeated > 7) {
                  ultimateBossSelected = 7;
                } else {
                  if(ultimateBossSelected > ultimateBossesDefeated) {
                    ultimateBossSelected = ultimateBossesDefeated;
                  }
                }
                ultimateLevelCap = 40;
              } else if(mapId >= 398 && mapId <= 400) {
                if(ultimateBossSelected > 21 && ultimateBossesDefeated > 21) {
                  ultimateBossSelected = 21;
                } else {
                  if(ultimateBossSelected > ultimateBossesDefeated) {
                    ultimateBossSelected = ultimateBossesDefeated;
                  }
                }
                ultimateLevelCap = 50;
              }


              /*if(ultimateBossSelected >= 0) {
                ultimateBattle = true;

                encounterId_800bb0f8 = ultimateEncounter[ultimateBossSelected][0];

                if(engineState_8004dd20 == EngineStateEnum.SUBMAP_05) {
                  battleStage_800bb0f4 = ultimateEncounter[ultimateBossSelected][1];
                  ((SMap)currentEngineState_8004dd04).mapTransition(-1, 0);
                } else if(engineState_8004dd20 == EngineStateEnum.WORLD_MAP_08) {
                  battleStage_800bb0f4 = ultimateEncounter[ultimateBossSelected][1];

                  gameState_800babc8.directionalPathIndex_4de = ((WMap)currentEngineState_8004dd04).mapState_800c6798.directionalPathIndex_12;
                  gameState_800babc8.pathIndex_4d8 = ((WMap)currentEngineState_8004dd04).mapState_800c6798.pathIndex_14;
                  gameState_800babc8.dotIndex_4da = ((WMap)currentEngineState_8004dd04).mapState_800c6798.dotIndex_16;
                  gameState_800babc8.dotOffset_4dc = ((WMap)currentEngineState_8004dd04).mapState_800c6798.dotOffset_18;
                  gameState_800babc8.facing_4dd = ((WMap)currentEngineState_8004dd04).mapState_800c6798.facing_1c;
                  ((WMap)currentEngineState_8004dd04).wmapState_800bb10c = WmapState.TRANSITION_TO_BATTLE_8;
                }
              }*/
            }
          }
        }
      } else if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_1)) {
        if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
          if(gameState_800babc8.chapterIndex_98 == 3) {
            Scus94491BpeSegment_8007.shopId_8007a3b4 = 42;
            Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
            Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
          }
        }
      } else if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1)) {
        if(difficulty.equals("Hard Mode") || difficulty.equals("US + Hard Bosses") || difficulty.equals("Hell Mode") || difficulty.equals("Hard + Hell Bosses")) {
          if(gameState_800babc8.chapterIndex_98 == 3) {
            Scus94491BpeSegment_8007.shopId_8007a3b4 = 43;
            Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
            Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
          }
        }
      } else if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_2)) {
        if(gameState_800babc8.chapterIndex_98 >= 1) {
          Scus94491BpeSegment_8007.shopId_8007a3b4 = 40;
          Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
          Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
        }
      } else if(hotkey.contains(InputAction.BUTTON_NORTH) && hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2)) {
        Scus94491BpeSegment_8007.shopId_8007a3b4 = 41;
        Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
        Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
      }
    }
  }
  //endregion
}