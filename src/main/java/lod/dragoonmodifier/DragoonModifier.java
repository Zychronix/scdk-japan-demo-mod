package lod.dragoonmodifier;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import legend.core.GameEngine;
import legend.core.gte.MV;
import legend.game.EngineStateEnum;
import legend.game.Scus94491BpeSegment_8002;
import legend.game.Scus94491BpeSegment_8007;
import legend.game.Scus94491BpeSegment_800b;
import legend.game.characters.Addition04;
import legend.game.characters.Element;
import legend.game.characters.ElementSet;
import legend.game.characters.UnaryStatModConfig;
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
import legend.game.modding.events.characters.AdditionUnlockEvent;
import legend.game.modding.events.characters.XpToLevelEvent;
import legend.game.modding.events.config.ConfigLoadedEvent;
import legend.game.modding.events.gamestate.NewGameEvent;
import legend.game.modding.events.input.InputPressedEvent;
import legend.game.modding.events.input.InputReleasedEvent;
import legend.game.modding.events.inventory.EquipmentStatsEvent;
import legend.game.modding.events.inventory.GiveEquipmentEvent;
import legend.game.modding.events.inventory.GiveItemEvent;
import legend.game.modding.events.inventory.RepeatItemReturnEvent;
import legend.game.modding.events.inventory.ShopEquipmentEvent;
import legend.game.modding.events.inventory.ShopItemEvent;
import legend.game.modding.events.inventory.ShopTypeEvent;
import legend.game.modding.events.submap.SubmapWarpEvent;
import legend.game.saves.ConfigEntry;
import legend.game.saves.ConfigRegistryEvent;
import legend.game.scripting.ScriptState;
import legend.game.submap.SMap;
import legend.game.submap.SubmapObject210;
import legend.game.submap.SubmapState;
import legend.game.types.ActiveStatsa0;
import legend.game.types.EquipmentSlot;
import legend.game.types.InventoryMenuState;
import legend.game.types.LevelStuff08;
import legend.game.types.MagicStuff08;
import legend.game.types.SpellStats0c;
import legend.game.wmap.WMap;
import legend.game.wmap.WmapState;
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
import org.apache.commons.lang3.ArrayUtils;
import org.legendofdragoon.modloader.Mod;
import org.legendofdragoon.modloader.events.EventListener;
import org.legendofdragoon.modloader.registries.DuplicateRegistryIdException;
import org.legendofdragoon.modloader.registries.Registrar;
import org.legendofdragoon.modloader.registries.RegistryDelegate;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.io.File;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import static legend.core.GameEngine.CONFIG;
import static legend.core.GameEngine.REGISTRIES;
import static legend.core.GameEngine.RENDERER;
import static legend.game.SItem.getXpToNextLevel;
import static legend.game.Scus94491BpeSegment_8004.currentEngineState_8004dd04;
import static legend.game.Scus94491BpeSegment_8004.engineState_8004dd20;
import static legend.game.Scus94491BpeSegment_8005.submapCut_80052c30;
import static legend.game.Scus94491BpeSegment_8006.battleState_8006e398;
import static legend.game.Scus94491BpeSegment_800b.battleStage_800bb0f4;
import static legend.game.Scus94491BpeSegment_800b.encounterId_800bb0f8;
import static legend.game.Scus94491BpeSegment_800b.fullScreenEffect_800bb140;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.Scus94491BpeSegment_800b.livingCharCount_800bc97c;
import static legend.game.Scus94491BpeSegment_800b.livingCharIds_800bc968;
import static legend.game.Scus94491BpeSegment_800b.scriptStatePtrArr_800bc1c0;
import static legend.game.Scus94491BpeSegment_800b.spGained_800bc950;
import static legend.game.combat.Battle.spellStats_800fa0b8;
import static legend.lodmod.LodMod.DARK_ELEMENT;
import static legend.lodmod.LodMod.DIVINE_ELEMENT;
import static legend.lodmod.LodMod.EARTH_ELEMENT;
import static legend.lodmod.LodMod.FIRE_ELEMENT;
import static legend.lodmod.LodMod.HP_STAT;
import static legend.lodmod.LodMod.LIGHT_ELEMENT;
import static legend.lodmod.LodMod.MP_STAT;
import static legend.lodmod.LodMod.SPEED_STAT;
import static legend.lodmod.LodMod.SP_STAT;
import static legend.lodmod.LodMod.THUNDER_ELEMENT;
import static legend.lodmod.LodMod.WATER_ELEMENT;
import static legend.lodmod.LodMod.WIND_ELEMENT;

@Mod(id = DragoonModifier.MOD_ID)
public class DragoonModifier {
  public static final String MOD_ID = "dragoon_modifier";
  public static boolean DEBUG_MODE = true;
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
  public static final List<String[]> levelCaps = new ArrayList<>();
  public static final List<String[]> spBarColours = new ArrayList<>();
  public static final List<String[]> shanaSpGain = new ArrayList<>();
  public static final List<String[]> ultimateData = new ArrayList<>();
  public static final Map<RegistryId, Equipment> registryEquipment = new HashMap<>();
  public static final Map<RegistryId, Item> registryItems = new HashMap<>();

  public static int maxCharacterLevel = 60;
  public static int maxDragoonLevel = 5;
  public static int maxAdditionLevel = 5;
  public static int additionsPerLevel = 20;
  public int currentPlayerSlot;
  public boolean dragonBlockStaff;
  public int[] enrageMode = new int[10];
  public Element[] previousElement = new Element[3];
  public int[][] damageTrackerEquips = new int[3][5];
  public int[][] damageTracker = new int[3][5];
  public int[] damageTrackerPreviousHP = new int[10];
  public int damageTrackerPreviousCharacter;
  public int damageTrackerPreviousCharacterID;
  public int damageTrackerPreviousAttackType;
  public ArrayList<String> damageTrackerLog = new ArrayList<>();
  public boolean damageTrackerPrinted;
  public boolean[] elementalAttack = new boolean[3];
  public int[] windMark = new int[10];
  public int[] thunderCharge = new int[10];
  public boolean flowerStormOverride;
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
  public boolean swappedEXP;
  public int[] swapEXPParty = new int[3];
  public int[][] ultimateEncounter = {{487, 10}, {386, 3}, {414, 8},
    {461, 21}, {412, 16}, {413, 70}, {387, 5}, {415, 12},
    {449, 68}, {402, 23}, {403, 29}, {417, 31}, {418, 41}, {448, 68}, {416, 38}, {422, 42}, {423, 47}, {432, 69}, {430, 67}, {433, 56}, {431, 54}, {447, 68}
  };
  public boolean ultimateBattle;
  public int ultimateLevelCap = 30;
  public double[][] ultimatePenality = new double[3][2];
  public boolean[] bonusItemSP = new boolean[3];
  public boolean[] ouroboros = new boolean[3];
  public ArrayList<Element> elementArrowsElements = new ArrayList<>();
  public int[] ringOfElements = new int[3];
  public Element[] ringOfElementsElement = new Element[3];

  public static int selectedItemId = -1;
  public static int selectedFakeItemId;
  //public static ItemStats0c selectedItemStats = null;
  public static int lastSelectedMenuType;

  public Set<InputAction> hotkey = new HashSet<>();

  public boolean burnStackMode;
  public int burnStacks;
  public int previousBurnStacks;
  public double dmgPerBurn = 0.1;
  public int burnStacksMax;
  public double maxBurnAddition = 1;
  public final int burnStackFlameShot = 1;
  public final int burnStackExplosion = 2;
  public final int burnStackFinalBurst = 3;
  public final int burnStackRedEye = 4;
  public final int burnStackAddition = 1;
  public boolean burnAdded;
  public boolean faustBattle;
  public int armorOfLegendTurns;
  public int legendCasqueTurns;
  public int[] protectionShield = new int[3];
  public boolean[] spiritBottle = new boolean[3];
  public boolean[] speedBottle = new boolean[3];
  public boolean[] healingBottle = new boolean[3];
  public boolean[] sunBottle = new boolean[3];

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

  public final int[] bossEncounters = {
    384, //Commander
    386, //Fruegel I
    414, //Urobolus
    385, //Sandora Elite
    388, //Kongol I
    408, //Virage I
    415, //Fire Bird
    393, //Greham + Feyrbrand
    412, //Drake the Bandit
    413, //Jiango
    387, //Fruegel II
    461, //Sandora Elite II
    389, //Kongol II
    390, //Emperor Doel
    402, //Mappi
    409, //Virage II
    403, //Gehrich + Mappi
    396, //Lenus
    417, //Ghost Commander
    397, //Lenus + Regole
    418, //Kamuy
    410, //S Virage
    416, //Grand Jewel
    394, //Divine Dragon
    422, //Windigo
    392, //Lloyd
    423, //Polter Set
    398, //Damia
    399, //Syuveil
    400, //Belzac
    401, //Kanzas
    420, //Magician Faust
    432, //Last Kraken
    430, //Executioners
    449, //Spirit (Feyrbrand)
    448, //Spirit (Regole)
    447, //Spirit (Divine Dragon)
    431, //Zackwell
    433, //Imago
    411, //S Virage II
    442, //Zieg
    443 //Melbu Fraahma
  };

  //region Startup
  public DragoonModifier() {
    GameEngine.EVENTS.register(this);
  }

  public RegistryId id(final String entryId) {
    return new RegistryId(MOD_ID, entryId);
  }
  public RegistryId idCore(final String entryId) {
    return new RegistryId("lod", entryId);
  }

  public void print(final String text) { if(DEBUG_MODE) System.out.println("[DRAGOON MODIFIER] " + text); }

  @EventListener public void configRegistry(final ConfigRegistryEvent event) {
    this.print("Config Registry Event");
    DRAMOD_CONFIG_REGISTRAR.registryEvent(event);
    this.loadAllCsvs(GameEngine.CONFIG.getConfig(DIFFICULTY.get()));
  }

  @EventListener public void configLoaded(final ConfigLoadedEvent event) {
    if(event.storageLocation == DIFFICULTY.get().storageLocation) {
      this.print("Config Loaded Event" + equipStats.size());
      this.loadAllCsvs(event.configCollection.getConfig(DIFFICULTY.get()));
    }
  }

  @EventListener public void difficultyChanged(final DifficultyChangedEvent event) {
    this.loadAllCsvs(GameEngine.CONFIG.getConfig(DIFFICULTY.get()));
  }

  public List<String[]> loadCSV(final String path) {
    try (final FileReader fr = new FileReader(path, StandardCharsets.UTF_8);
         final CSVReader csv = new CSVReader(fr)) {
      final List<String[]> list = csv.readAll();
      list.removeFirst();
      return list;
    } catch (final IOException | CsvException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadCsvIntoList(final String difficulty, final List<String[]> list, final String file) {
    list.clear();
    list.addAll(this.loadCSV("./mods/csvstat/" + difficulty + '/' + file));
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
    this.loadCsvIntoList(difficulty, levelCaps, "scdk-level-caps.csv");
    this.loadCsvIntoList(difficulty, spBarColours, "scdk-sp-bar-colours.csv");
    this.loadCsvIntoList(difficulty, shanaSpGain, "scdk-shana-sp-gain.csv");
    this.loadCsvIntoList("Ultimate", ultimateData, "scdk-ultimate.csv");
    maxCharacterLevel = Integer.parseInt(levelCaps.getFirst()[0]);
    maxDragoonLevel = Integer.parseInt(levelCaps.getFirst()[1]);
    maxAdditionLevel = Integer.parseInt(levelCaps.getFirst()[2]);
    additionsPerLevel = Integer.parseInt(levelCaps.getFirst()[3]);

    this.print("Loaded using directory: " + difficulty + equipStats.size());
    this.configSwapped();
  }

  @EventListener public void newGame(final NewGameEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      event.gameState.gold_94 = 200;
    } else {
      event.gameState.gold_94 = 20;
    }
  }

  @EventListener public void submapWarp(final SubmapWarpEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    if(submapCut_80052c30 == 676 && gameState_800babc8.charData_32c[0].level_12 == 1) {
      if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
        event.gameState.gold_94 = 200;
        event.gameState.items_2e9.add(REGISTRIES.items.getEntry("lod:spark_net").get());
        event.gameState.items_2e9.add(REGISTRIES.items.getEntry("lod:healing_potion").get());
        event.gameState.items_2e9.add(REGISTRIES.items.getEntry("lod:healing_potion").get());
      } else {
        event.gameState.gold_94 = 20;
      }
    } else if(submapCut_80052c30 == 10) {
      if(("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) && gameState_800babc8.charData_32c[0].level_12 == 1) {
        gameState_800babc8.goods_19c[0] ^= 1;
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
    new Thread(() -> {
      while(engineState_8004dd20 == EngineStateEnum.PRELOAD_00) {
        try {
          this.print("Waiting for preload state to exit...");
          Thread.sleep(500);
        } catch(final InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      CoreMod.MAX_CHARACTER_LEVEL = maxCharacterLevel;
      CoreMod.MAX_DRAGOON_LEVEL = maxDragoonLevel;
      CoreMod.MAX_ADDITION_LEVEL = maxAdditionLevel;
      CoreMod.ADDITIONS_PER_LEVEL = additionsPerLevel;

      for(int i = 0; i < 9; i++) {
        CoreMod.CHARACTER_DATA[i].xpTable = new int[maxCharacterLevel + 1];
        CoreMod.CHARACTER_DATA[i].statsTable = new LevelStuff08[maxCharacterLevel + 1];
        CoreMod.CHARACTER_DATA[i].dxpTable = new int[CoreMod.MAX_DRAGOON_LEVEL + 1];
        CoreMod.CHARACTER_DATA[i].dragoonStatsTable = new MagicStuff08[CoreMod.MAX_DRAGOON_LEVEL + 1];
        CoreMod.CHARACTER_DATA[i].additions = new ArrayList<>();
        CoreMod.CHARACTER_DATA[i].additionsMultiplier = new ArrayList<>();
        CoreMod.CHARACTER_DATA[i].dragoonAddition = new ArrayList<>();
      }

      for(int i = 0; i < 9; i++) {
        for(int x = 0; x < maxCharacterLevel + 1; x++) {
          CoreMod.CHARACTER_DATA[i].xpTable[x] = Integer.parseInt(xpNextStats.get((maxCharacterLevel + 1) * i + x)[0]);
          CoreMod.CHARACTER_DATA[i].statsTable[x] = new LevelStuff08(Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[5]), Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[6]),
            Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[0]), Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[1]),
            Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[2]), Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[3]),
            Integer.parseInt(characterStatsTable.get((maxCharacterLevel + 1) * i + x)[4]));
        }
      }

      for(int i = 0; i < 9; i++) {
        CoreMod.CHARACTER_DATA[i].spBarColours = new int[maxDragoonLevel + 2][6];
        for(int x = 0; x < maxDragoonLevel + 1; x++) {
          CoreMod.CHARACTER_DATA[i].dxpTable[x] = Integer.parseInt(dxpNextStats.get(i)[x]);
        }
        for(int x = 0; x < maxDragoonLevel + 1; x++) {
          final int spellIndex = Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[1]);
          CoreMod.CHARACTER_DATA[i].dragoonStatsTable[x] = new MagicStuff08(Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[0]), spellIndex == 255 ? (byte)-1 : (byte)spellIndex,
            Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[2]), Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[3]),
            Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[4]), Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[5]),
            Integer.parseInt(dragoonStatsTable.get((maxDragoonLevel + 1) * i + x)[6]));
        }
      }

      for(int i = 0; i < 9; i++) {
        for(int x = 0; x < maxDragoonLevel + 2; x++) {
          final int top = Integer.decode(spBarColours.get(i * 2)[x].replace("#", "0x"));
          final int btm = Integer.decode(spBarColours.get(2 + 1)[x].replace("#", "0x"));

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

      this.loadCharacterAdditions(0, 0, 7);
      this.loadCharacterAdditions(1, 8, 13);
      this.loadCharacterAdditions(3, 14, 18);
      this.loadCharacterAdditions(7, 19, 22);
      this.loadCharacterAdditions(6, 23, 28);
      this.loadCharacterAdditions(4, 29, 35);
      this.loadCharacterAdditions(5, 36, 41);
      this.loadAdditionMultiplier(0, 0, 7);
      this.loadAdditionMultiplier(1, 8, 13);
      this.loadAdditionMultiplier(3, 14, 18);
      this.loadAdditionMultiplier(7, 19, 22);
      this.loadAdditionMultiplier(6, 23, 28);
      this.loadAdditionMultiplier(4, 29, 35);
      this.loadAdditionMultiplier(5, 36, 41);
      this.loadDragoonAddition(0, 7, false);
      this.loadDragoonAddition(1, 13, false);
      this.loadDragoonAddition(3, 18, false);
      this.loadDragoonAddition(7, 22, false);
      this.loadDragoonAddition(6, 28, false);
      this.loadDragoonAddition(4, 35, false);
      this.loadDragoonAddition(5, 41, false);
      this.loadDragoonAddition(0, 42, true);
      this.loadShanaAdditions();
      this.loadAdditionMultiplier(2, 7, 8);
      this.loadAdditionMultiplier(8, 7, 8);

      this.print("MOD loaded. Config swapped." + equipStats.size());
    }).start();
  }

  public Equipment getEquipFromRegistry(final RegistryId id) {
    for(final var entry : registryEquipment.entrySet()) {
      if(entry.getKey().toString().equals(id.toString())) {
        return entry.getValue();
      }
    }
    return null;
  }

  public Item getItemFromRegistry(final RegistryId id) {
    for(final var entry : registryItems.entrySet()) {
      if(entry.getKey().toString().equals(id.toString())) {
        return entry.getValue();
      }
    }
    return null;
  }
  //endregion

  //region Additions
  public void loadCharacterAdditions(final int charIndex, final int additionStart, final int additionEnd) {
    CoreMod.CHARACTER_DATA[charIndex].additions = new ArrayList<>();

    for(int i = additionStart; i < additionEnd; i++) {
      final AdditionHitProperties10[] hits = new AdditionHitProperties10[8];

      for(int x = 0; x < 8; x++) {
        final int panDistance = Short.parseShort(additionStats.get(i * 8 + x)[8]);
        hits[x] = new AdditionHitProperties10(
          Short.parseShort(additionStats.get(i * 8 + x)[0]),
          Short.parseShort(additionStats.get(i * 8 + x)[1]),
          Short.parseShort(additionStats.get(i * 8 + x)[2]),
          Short.parseShort(additionStats.get(i * 8 + x)[3]),
          Short.parseShort(additionStats.get(i * 8 + x)[4]),
          Short.parseShort(additionStats.get(i * 8 + x)[5]),
          Short.parseShort(additionStats.get(i * 8 + x)[6]),
          Short.parseShort(additionStats.get(i * 8 + x)[7]),
          panDistance > 127 ? panDistance - 255 : panDistance,
          Short.parseShort(additionStats.get(i * 8 + x)[9]),
          Short.parseShort(additionStats.get(i * 8 + x)[10]),
          Short.parseShort(additionStats.get(i * 8 + x)[11]),
          Short.parseShort(additionStats.get(i * 8 + x)[12]),
          Short.parseShort(additionStats.get(i * 8 + x)[13]),
          Short.parseShort(additionStats.get(i * 8 + x)[14]),
          Short.parseShort(additionStats.get(i * 8 + x)[15])
        );
      }

      CoreMod.CHARACTER_DATA[charIndex].additions.add(new AdditionHits80(hits));
    }
  }

  public void loadAdditionMultiplier(final int charIndex, final int additionStart, final int additionEnd) {
    CoreMod.CHARACTER_DATA[charIndex].additionsMultiplier = new ArrayList<>();

    for(int i = additionStart; i < additionEnd; i++) {
      final Addition04[] multipliers = new Addition04[maxAdditionLevel + 1];

      for(int x = 0; x < maxAdditionLevel + 1; x++) {
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
      final int panDistance = Short.parseShort(additionStats.get(x * 8 + x)[8]);
      hits[x] = new AdditionHitProperties10(
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[0]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[1]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[2]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[3]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[4]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[5]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[6]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[7]),
        panDistance > 127 ? panDistance - 255 : panDistance,
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[9]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[10]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[11]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[12]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[13]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[14]),
        Short.parseShort(additionStats.get(dragoonIndex * 8 + x)[15])
      );
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
      hits[x] = new AdditionHitProperties10(
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0
      );
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
  @EventListener public void equipmentRegistry(final EquipmentRegistryEvent event) {
    this.print("Equipment Registry Event");
    registryEquipment.clear();

    final File[] baseDirectory = new File("./mods/csvstat").listFiles();
    if(baseDirectory != null) {
      for(final File file : baseDirectory) {
        if(file.isDirectory() && !"Ultimate".equals(file.getName())) {
          try (final FileReader fr = new FileReader(file.getAbsolutePath() + "/scdk-equip-stats.csv", StandardCharsets.UTF_8);
               final CSVReader csv = new CSVReader(fr)) {
            final List<String[]> list = csv.readAll();
            list.removeFirst();

            for(final String[] equip : list) {
              //this.print("Registering: " + file.getName() + '/' + equip[44]);
              final ElementSet elementalResistance = new ElementSet();
              final ElementSet elementalImmunity = new ElementSet();

              elementalResistance.add(Element.fromFlag(Integer.parseInt(equip[6])));
              elementalImmunity.add(Element.fromFlag(Integer.parseInt(equip[7])));

              final Equipment dmEquip = new Equipment(
                Integer.parseInt(equip[41]), //Price
                Integer.parseInt(equip[0]), //Flags
                Integer.parseInt(equip[1]), //type
                Integer.parseInt(equip[2]), //_02
                Integer.parseInt(equip[3]), //equipable
                Element.fromFlag(Integer.parseInt(equip[4])), //Element
                Integer.parseInt(equip[5]), //_05
                elementalResistance, //elementalResistance
                elementalImmunity, //elementalImmunity
                Integer.parseInt(equip[8]), //Status Resist
                Integer.parseInt(equip[9]), //_09
                Integer.parseInt(equip[10]), //AT
                Integer.parseInt(equip[11]), //mpPerPhysicalHit
                Integer.parseInt(equip[12]), //spPerPhysicalHit
                Integer.parseInt(equip[13]), //mpPerMagicalHit
                Integer.parseInt(equip[14]), //spPerMagicalHit
                Integer.parseInt(equip[15]), //hpMultiplier
                Integer.parseInt(equip[16]), //mpMultiplier
                Integer.parseInt(equip[17]), //spMultiplier
                Boolean.parseBoolean(equip[18]), //magicalResistance
                Boolean.parseBoolean(equip[19]), //physicalResistance
                Boolean.parseBoolean(equip[20]), //magicalImmunity
                Boolean.parseBoolean(equip[21]), //physicalImmunity
                Integer.parseInt(equip[22]), //revive
                Integer.parseInt(equip[23]), //hpRegen
                Integer.parseInt(equip[24]), //mpRegen
                Integer.parseInt(equip[25]), //spRegen
                Integer.parseInt(equip[26]), //escapeBonus
                Integer.parseInt(equip[27]), //icon
                Integer.parseInt(equip[28]), //spd
                Integer.parseInt(equip[29]), //atkHi
                Integer.parseInt(equip[30]), //matk
                Integer.parseInt(equip[31]), //def
                Integer.parseInt(equip[32]), //mdef
                Integer.parseInt(equip[33]), //aHit
                Integer.parseInt(equip[34]), //mHit
                Integer.parseInt(equip[35]), //aAv
                Integer.parseInt(equip[36]), //mAv
                Integer.parseInt(equip[37]), //onStatusChance
                Integer.parseInt(equip[38]), //_19
                Integer.parseInt(equip[39]), //_1a
                Integer.parseInt(equip[40]) //On Hit Status
              );


              if(equip[44].startsWith("lod")) {
                if(file.getName().equals(GameEngine.CONFIG.getConfig(DIFFICULTY.get()))) {
                  if(equip[44].split(":")[1].length() >= 3) {
                    registryEquipment.put(this.idCore(equip[44].split(":")[1]), dmEquip);
                  }
                }
              } else {
                if(file.getName().equals(GameEngine.CONFIG.getConfig(DIFFICULTY.get()))) {
                  if(equip[44].split(":")[1].length() >= 3) {
                    registryEquipment.put(this.id(equip[44].split(":")[1]), dmEquip);
                  }
                }

                if(event != null && equip[44].split(":")[1].length() >= 3) {
                  try {
                    event.register(this.id(equip[44].split(":")[1]), dmEquip);
                  } catch(final DuplicateRegistryIdException ignored) {}
                }
              }
            }

            this.print("Registered " + registryEquipment.size() + " equips.");
          } catch (final IOException | CsvException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    for(final var entry : registryEquipment.entrySet()) {
      print("Equip Registry: " + entry.getKey());
    }
  }

  @EventListener public void itemRegistry(final ItemRegistryEvent event) {
    this.print("Item Registry Event");
    registryItems.clear();

    final File[] baseDirectory = new File("./mods/csvstat").listFiles();
    if(baseDirectory != null) {
      for(final File file : baseDirectory) {
        if(file.isDirectory() && !"Ultimate".equals(file.getName())) {
          try (final FileReader fr = new FileReader(file.getAbsolutePath() + "/scdk-thrown-item-stats.csv", StandardCharsets.UTF_8);
               final CSVReader csv = new CSVReader(fr)) {
            final List<String[]> list = csv.readAll();
            list.removeFirst();

            for(final String[] item : list) {
              //this.print("Registering: " + file.getName() + '/' + item[32]);
              final int target = Integer.parseInt(item[0]);
              final Set<Item.TargetType> targets = EnumSet.noneOf(Item.TargetType.class);
              final Set<Item.UsageLocation> usage = EnumSet.noneOf(Item.UsageLocation.class);

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
                Integer.parseInt(item[26]),
                targets,
                usage,
                Element.fromFlag(Integer.parseInt(item[1])),
                Integer.parseInt(item[2]),
                Integer.parseInt(item[3]),
                Integer.parseInt(item[4]),
                Integer.parseInt(item[5]),
                Integer.parseInt(item[6]),
                Integer.parseInt(item[7]),
                Integer.parseInt(item[8]),
                Integer.parseInt(item[9]),
                Integer.parseInt(item[10]),
                Boolean.parseBoolean(item[11]),
                Boolean.parseBoolean(item[12]),
                Integer.parseInt(item[13]),
                Integer.parseInt(item[14]),
                Integer.parseInt(item[15]),
                Integer.parseInt(item[16]),
                Integer.parseInt(item[17]),
                Integer.parseInt(item[18]),
                Integer.parseInt(item[19]),
                Integer.parseInt(item[20]),
                Integer.parseInt(item[21]),
                Integer.parseInt(item[22]),
                Integer.parseInt(item[23]),
                Integer.parseInt(item[24]),
                Boolean.parseBoolean(item[25])
              );

              if(item[32].startsWith("lod")) {
                if(file.getName().equals(GameEngine.CONFIG.getConfig(DIFFICULTY.get()))) {
                  if(item[32].split(":")[1].length() >= 3) {
                    registryItems.put(this.idCore(item[32].split(":")[1]), dmItem);
                  }
                }
              } else {
                if(file.getName().equals(GameEngine.CONFIG.getConfig(DIFFICULTY.get()))) {
                  if(item[32].split(":")[1].length() >= 3) {
                    registryItems.put(this.id(item[32].split(":")[1]), dmItem);
                  }
                }

                if(event != null && item[32].split(":")[1].length() >= 3) {
                  try {
                    event.register(this.id(item[32].split(":")[1]), dmItem);
                  } catch(final DuplicateRegistryIdException ignored) {}
                }
              }
            }

            this.print("Registered " + registryItems.size() + " items.");
          } catch (final IOException | CsvException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    for(final var entry : registryItems.entrySet()) {
      this.print("Item Registry: " + entry.getKey());
    }
  }

  @EventListener public void spellRegistry(final SpellRegistryEvent event) {
    for(int i = 0; i < spellStats.size(); i++) {
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
    /*event.override = true; // TODO: Make sure this works for lod and dra_mod
    event.item = REGISTRIES.items.getEntry("dragoon_modifier:i" + LodMod.idItemMap.get(event.item.getRegistryId())).get();*/
  }

  @EventListener public void takeItem(final GiveEquipmentEvent event) {
    /*event.override = true; // TODO: Make sure this works for lod and dra_mod
    event.equip = REGISTRIES.equipment.getEntry("dragoon_modifier:e" + LodMod.idEquipmentMap.get(event.equip.getRegistryId())).get(); */
  }

  @EventListener public void equipStats(final EquipmentStatsEvent event) {
    final Equipment update = this.getEquipFromRegistry(event.equipment.getRegistryId());
    if(update != null) {
      event.flags_00 = update.flags_00;
      event.slot = update.slot;
      event._02 = update._02;
      event.equipableFlags_03 = update.equipableFlags_03;
      event.attackElement_04 = update.attackElement_04;
      event._05 = update._05;
      event.elementalResistance_06 = update.elementalResistance_06;
      event.elementalImmunity_07 = update.elementalImmunity_07;
      event.statusResist_08 = update.statusResist_08;
      event._09 = update._09;
      event.attack1_0a = update.attack1_0a;
      event.mpPerPhysicalHit = update.mpPerPhysicalHit;
      event.spPerPhysicalHit = update.spPerPhysicalHit;
      event.mpPerMagicalHit = update.mpPerMagicalHit;
      event.spPerMagicalHit = update.spPerMagicalHit;
      event.hpMultiplier = update.hpMultiplier;
      event.mpMultiplier = update.mpMultiplier;
      event.spMultiplier = update.spMultiplier;
      event.magicalResistance = update.magicalResistance;
      event.physicalResistance = update.physicalResistance;
      event.magicalImmunity = update.magicalImmunity;
      event.physicalImmunity = update.physicalImmunity;
      event.revive = update.revive;
      event.hpRegen = update.hpRegen;
      event.mpRegen = update.mpRegen;
      event.spRegen = update.spRegen;
      event.escapeBonus = update.escapeBonus;
      event.icon_0e = update.icon_0e;
      event.speed_0f = update.speed_0f;
      event.attack2_10 = update.attack2_10;
      event.magicAttack_11 = update.magicAttack_11;
      event.defence_12 = update.defence_12;
      event.magicDefence_13 = update.magicDefence_13;
      event.attackHit_14 = update.attackHit_14;
      event.magicHit_15 = update.magicHit_15;
      event.attackAvoid_16 = update.attackAvoid_16;
      event.magicAvoid_17 = update.magicAvoid_17;
      event.onHitStatusChance_18 = update.onHitStatusChance_18;
      event._19 = update._19;
      event._1a = update._1a;
      event.onHitStatus_1b = update.onHitStatus_1b;
    } else {
      print("NULL EQUIPMENT FOUND DOES NOT EXIST IN DRAMOD REGISTRY: " + event.equipment.getRegistryId());
    }
  }
  //endregion

  //region Inventory Battle
  @EventListener public void itemId(final ItemIdEvent event) {
    /*if(event.registryId.toString().contains("dragoon_modifier")) { // TODO: Custom items test for later
      final String item = event.registryId.toString().split(":")[1];
      event.itemId = Integer.parseInt(item.substring(1));
      final int fakeItemId = Integer.parseInt(itemStats.get(event.itemId)[17]);
      this.print("Item ID: " + event.itemId + '/' + event.registryId + " Fake ID: " + fakeItemId);
      selectedItemId = event.itemId;
      selectedFakeItemId = fakeItemId;
    } else {
      selectedItemId = -1;
    }*/
  }

  @EventListener public void battleDescription(final BattleDescriptionEvent event) {
    this.print("Description: " + event.textType + '/' + event.textIndex);
    if(event.textType == 4) {
      if(selectedItemId > -1) {
        event.string = itemStats.get(selectedItemId)[15];
      }
    }
    lastSelectedMenuType = event.textType;
  }

/*
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
*/

  @EventListener public void selectedItem(final SelectedItemEvent event) {
    final String item = event.item.getRegistryId().toString().split(":")[1];
    final int fakeItemId = Integer.parseInt(itemStats.get(event.itemId)[17]);
    event.itemId = (short) fakeItemId;
  }

  @EventListener public void spellItemDeff(final SpellItemDeffEvent event) {
    if(selectedItemId != -1 && lastSelectedMenuType == 4) {
      final int deffScriptId = Integer.parseInt(itemStats.get(selectedItemId)[16]);
      if(deffScriptId != -1) {
        event.scriptId = deffScriptId;
        event.s0 = 0;
      }
    }

    this.print("Item/Spell DEFF: " + event.scriptId + '+' + event.s0);
  }

  @EventListener public void dragoonDeff(final DragoonDeffEvent event) {
    this.print("Dragoon DEFF: " + event.scriptId);
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
          for(int i = 0; i < 80; i++) {
            try {
              fullScreenEffect_800bb140.type_00 = 0;
              Thread.sleep(125);
            } catch (final InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        }).start();
        break;
      case 4208: //Blossom Storm
      case 4234: //Rose Storm
        final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
        if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
          this.flowerStormOverride = true;
        }
        break;
    }
  }
  //endregion

  //region Battle Monster
  @EventListener public void monsterStats(final MonsterStatsEvent event) {
    final int ovrId = event.enemyId;
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
    final int enemyId = event.enemyId;
    event.clear();
    if(this.ultimateBattle) {
      for(int i = 0; i < 86; i++) {
        if(enemyId == Integer.parseInt(ultimateData.get(i)[0])) {
          final String item = monstersRewardsStats.get(enemyId)[27];

          event.xp = Integer.parseInt(ultimateData.get(i)[25]);
          event.gold = Integer.parseInt(ultimateData.get(i)[26]);
          if(!item.startsWith("lod:_") && !item.startsWith("lod:None")) {
            try {
              event.add(new CombatantStruct1a8.ItemDrop(Integer.parseInt(monstersRewardsStats.get(enemyId)[2]), REGISTRIES.equipment.getEntry(item).get()));
            } catch(final Exception ignored) {}

            try {
              event.add(new CombatantStruct1a8.ItemDrop(Integer.parseInt(monstersRewardsStats.get(enemyId)[2]), REGISTRIES.items.getEntry(item).get()));
            } catch(final Exception ignored) {}
          }
          break;
        }
      }
    } else {
      final String item = monstersRewardsStats.get(enemyId)[3];
      final int exp = Integer.parseInt(monstersRewardsStats.get(enemyId)[0]);
      event.xp = ArrayUtils.contains(this.bossEncounters, encounterId_800bb0f8) ? (int) (exp * 1.5) : exp;
      event.gold = Integer.parseInt(monstersRewardsStats.get(enemyId)[1]);
      if(!item.startsWith("lod:_") && !item.startsWith("lod:None")) {
        try {
          event.add(new CombatantStruct1a8.ItemDrop(Integer.parseInt(monstersRewardsStats.get(enemyId)[2]), REGISTRIES.equipment.getEntry(item).get()));
        } catch(final Exception ignored) {}

        try {
          event.add(new CombatantStruct1a8.ItemDrop(Integer.parseInt(monstersRewardsStats.get(enemyId)[2]), REGISTRIES.items.getEntry(item).get()));
        } catch(final Exception ignored) {}
      }
      if(this.faustBattle && event.enemyId == 344) {
        event.clear();
        event.xp = 30000;
        event.gold = 250;
        if(Integer.parseInt(GameEngine.CONFIG.getConfig(FAUST_DEFEATED.get())) == 39) {
          event.add(new CombatantStruct1a8.ItemDrop(100, REGISTRIES.equipment.getEntry("lod:armor_of_legend").get()));
          event.add(new CombatantStruct1a8.ItemDrop(100, REGISTRIES.equipment.getEntry("lod:legend_casque").get()));
        }
      }
    }
  }
  //endregion

  //region Battle
  @EventListener public void battleStarted(final BattleStartedEvent event) {
    if(this.faustBattle) {
      final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[0];
      final BattleEntity27c bobj = state.innerStruct_00;
      final VitalsStat hp = bobj.stats.getStat(HP_STAT.get());
      hp.setCurrent(25600);
      hp.setMaxRaw(25600);
      bobj.attack_34 = 125;
      bobj.magicAttack_36 = 125;
      bobj.defence_38 = 75;
      bobj.magicDefence_3a = 200;
    }

    this.burnStacks = 0;
    this.armorOfLegendTurns = 0;
    this.legendCasqueTurns = 0;
    this.dragonBlockStaff = false;
    this.burnStackMode = false;
    this.flowerStormOverride = false;
    this.damageTrackerPrinted = false;
    Arrays.fill(this.enrageMode, 0);
    Arrays.fill(this.windMark, 0);
    Arrays.fill(this.thunderCharge, 0);
    Arrays.fill(this.elementalAttack, false);
    Arrays.fill(this.shanaStarChildrenHeal, false);
    Arrays.fill(this.shanaRapidFireContinue, false);
    Arrays.fill(this.shanaRapidFire, false);
    Arrays.fill(this.shanaRapidFireCount, 0);
    Arrays.fill(this.meruBoost, false);
    Arrays.fill(this.bonusItemSP, false);
    Arrays.fill(this.ouroboros, false);
    Arrays.fill(this.meruBoostTurns, 0);
    Arrays.fill(this.meruMaxHpSave, 0);
    Arrays.fill(this.meruMDFSave, 0);
    Arrays.fill(this.damageTracker[0], 0);
    Arrays.fill(this.damageTracker[1], 0);
    Arrays.fill(this.damageTracker[2], 0);
    Arrays.fill(this.ringOfElements, 0);
    Arrays.fill(this.ringOfElementsElement, null);
    Arrays.fill(this.protectionShield, 0);
    Arrays.fill(this.spiritBottle, false);
    Arrays.fill(this.speedBottle, false);
    Arrays.fill(this.healingBottle, false);
    Arrays.fill(this.sunBottle, false);
    this.damageTrackerLog.clear();
    this.elementArrowsElements.clear();

    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());

    if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof final PlayerBattleEntity player) {
          if(player.charId_272 == 0) {
            this.burnStacksMax = player.dlevel_06 == 0 ? 0 : player.dlevel_06 == 1 ? 3 : player.dlevel_06 == 2 ? 6 : player.dlevel_06 == 3 ? 9 : player.dlevel_06 == 7 ? 15 : 12;
          }

          player.equipmentElementalImmunity_22.clear();

          if(player.charId_272 == 7) { //Kongol SPD reduction
            final ActiveStatsa0 stats = Scus94491BpeSegment_800b.stats_800be5f8[player.charId_272];
            player.stats.getStat(SPEED_STAT.get()).setRaw(stats.bodySpeed_69 + (int) Math.round(stats.equipmentSpeed_86 / 2d));
          }

          if("lod:phantom_shield".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
            player.defence_38 = (int) Math.round(player.defence_38 * 0.6d);
            player.magicDefence_3a = (int) Math.round(player.magicDefence_3a * 0.6d);
          }

          if("lod:dragon_shield".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
            player.defence_38 = (int) Math.round(player.defence_38 * 0.6d);
          }

          if("lod:angel_scarf".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
            player.magicDefence_3a = (int) Math.round(player.magicDefence_3a * 0.6d);
          }

          if("lod:holy_ankh".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString()) && "lod:angel_robe".equals(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString())) {
            player.revive_13a -= 20;
          }

          int crystalItems = 0;
          if("dragoon_modifier:crystal_armor".equals(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString())) {
            crystalItems++;
          }

          if("dragoon_modifier:crystal_hat".equals(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().toString())) {
            crystalItems++;
          }

          if("dragoon_modifier:crystal_boots".equals(player.equipment_11e.get(EquipmentSlot.BOOTS).getRegistryId().toString())) {
            crystalItems++;
          }

          if("dragoon_modifier:crystal_ring".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
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
              player.stats.getStat(SPEED_STAT.get()).setRaw(player.stats.getStat(SPEED_STAT.get()).get() + 12);
            } else {
              player.stats.getStat(SPEED_STAT.get()).setRaw(player.stats.getStat(SPEED_STAT.get()).get() + 6);
            }
            player.stats.getStat(HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(HP_STAT.get()).getMax() * 1.3d));
            player.stats.getStat(MP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(MP_STAT.get()).getMax() * 1.3d));
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
              player.stats.getStat(SPEED_STAT.get()).setRaw(player.stats.getStat(SPEED_STAT.get()).get() + 6);
            } else {
              player.stats.getStat(SPEED_STAT.get()).setRaw(player.stats.getStat(SPEED_STAT.get()).get() + 3);
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
              player.stats.getStat(SPEED_STAT.get()).setRaw(player.stats.getStat(SPEED_STAT.get()).get() + 1);
            }
          }

          if("dragoon_modifier:ring_of_reversal".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
            final int df = player.defence_38;
            final int mdf = player.magicDefence_3a;
            player.magicDefence_3a = df;
            player.defence_38 = mdf;
            if(player.defence_38 > player.magicDefence_3a) {
              player.stats.getStat(HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(HP_STAT.get()).getMax() * 1.5d));
            } else {
              player.stats.getStat(HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(HP_STAT.get()).getMax() / 1.5d));
              player.spMultiplier_128 += 35;
            }
          }

          if("dragoon_modifier:the_one_ring".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
            player.stats.getStat(HP_STAT.get()).setCurrent(1);
            player.stats.getStat(HP_STAT.get()).setMaxRaw(1);
            player.attackAvoid_40 = 80;
            player.magicAvoid_42 = 80;
          }

          if("dragoon_modifier:divine_dg_armor".equals(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString())) {
            player.spPerPhysicalHit_12a += 10;
            player.spPerMagicalHit_12e += 10;
          }

          if("dragoon_modifier:halo_of_balance".equals(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().toString())) {
            player.stats.getStat(HP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(HP_STAT.get()).getMax() * 1.3d));
            player.stats.getStat(MP_STAT.get()).setMaxRaw((int) Math.round(player.stats.getStat(MP_STAT.get()).getMax() * 1.3d));
          }

          if("dragoon_modifier:firebrand".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) {
            player.equipmentAttackElements_1c.clear();
            player.equipmentAttackElements_1c.add(DIVINE_ELEMENT.get());
            player.equipmentAttackElements_1c.add(FIRE_ELEMENT.get());
          }

          if("dragoon_modifier:super_spirit_ring".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
            player.spMultiplier_128 = -100;
          }

          if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
            final int flowerStormTurns = GameEngine.CONFIG.getConfig(FLOWER_STORM.get());
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

            if("dragoon_modifier:protection_shield".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
              this.protectionShield[player.charSlot_276] += player.stats.getStat(HP_STAT.get()).getMax() / 2;
            }

            if("dragoon_modifier:protection_shoes".equals(player.equipment_11e.get(EquipmentSlot.BOOTS).getRegistryId().toString())) {
              this.protectionShield[player.charSlot_276] += player.stats.getStat(HP_STAT.get()).getMax() / 4;
            }

            if("dragoon_modifier:protection_helmet".equals(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().toString())) {
              this.protectionShield[player.charSlot_276] += player.stats.getStat(HP_STAT.get()).getMax() / 4;
            }

            if("dragoon_modifier:protection_armor".equals(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString())) {
              this.protectionShield[player.charSlot_276] += player.stats.getStat(HP_STAT.get()).getMax();
            }

            if("dragoon_modifier:spirit_bottle".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
              this.spiritBottle[player.charSlot_276] = true;
            }

            if("dragoon_modifier:speed_bottle".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
              this.speedBottle[player.charSlot_276] = true;
            }

            if("dragoon_modifier:healing_bottle".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
              this.healingBottle[player.charSlot_276] = true;
            }

            if("dragoon_modifier:sun_bottle".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
              this.sunBottle[player.charSlot_276] = true;
            }

            if(player.charId_272 == 2 || player.charId_272 == 8) {
              if(player.dlevel_06 >= 2) {
                final int moonLight;
                final int gatesOfHeaven;
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
                if(player.dlevel_06 >= 4 && player.stats.getStat(MP_STAT.get()).getMax() >= 120) {
                  spellStats_800fa0b8[gatesOfHeaven] = new SpellStats0c(spellStats.get(gatesOfHeaven)[12],
                    spellStats.get(gatesOfHeaven)[13],
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[0]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[1]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[2]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[3]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[4]),
                    Integer.parseInt(spellStats.get(gatesOfHeaven)[5]),
                    player.stats.getStat(MP_STAT.get()).getMax() / 3,
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

        this.elementArrowsElements.add(bobj.getElement());
        if(bobj instanceof PlayerBattleEntity && bobj.getElement() == FIRE_ELEMENT.get() && gameState_800babc8.goods_19c[0] << 7 == 1) {
          this.elementArrowsElements.add(DIVINE_ELEMENT.get());
        }
      }
    }

    if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      GameEngine.EVENTS.postEvent(new HellModeAdjustmentEvent());
    }

    for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
      final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
      final BattleEntity27c bobj = state.innerStruct_00;
      if(bobj instanceof final PlayerBattleEntity player) { //TODO damage tracker registries
        /*this.damageTrackerEquips[player.charSlot_276][0] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString().split(":")[1]);
        this.damageTrackerEquips[player.charSlot_276][1] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().toString().split(":")[1]);
        this.damageTrackerEquips[player.charSlot_276][2] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString().split(":")[1]);
        this.damageTrackerEquips[player.charSlot_276][3] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.BOOTS).getRegistryId().toString().split(":")[1]);
        this.damageTrackerEquips[player.charSlot_276][4] = Integer.parseInt(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString().split(":")[1]);*/
      }
    }

    if(this.ultimateBattle) {
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof final PlayerBattleEntity player) {
          this.ultimatePenality[player.charSlot_276][0] = 1;
          this.ultimatePenality[player.charSlot_276][1] = 1;

          if(player.level_04 > this.ultimateLevelCap) {
            final int levelDifference = player.level_04 - this.ultimateLevelCap;

            if(this.ultimateLevelCap == 30) {
              if(Math.round(levelDifference / 10d) == 1) { //Level 40
                this.ultimatePenality[player.charSlot_276][0] = 1.5;
                this.ultimatePenality[player.charSlot_276][1] = 1.26;
              } else if(Math.round(levelDifference / 10d) == 2) { //Level 50
                this.ultimatePenality[player.charSlot_276][0] = 2.6;
                this.ultimatePenality[player.charSlot_276][1] = 1.53;
              } else if(Math.round(levelDifference / 10d) == 3) { //Level 60
                this.ultimatePenality[player.charSlot_276][0] = 3.4;
                this.ultimatePenality[player.charSlot_276][1] = 1.89;
              }
            } else if(this.ultimateLevelCap == 40) {
              if(Math.round(levelDifference / 10d) == 1) { //Level 50
                this.ultimatePenality[player.charSlot_276][0] = 1.7;
                this.ultimatePenality[player.charSlot_276][1] = 1.17;
              } else if(Math.round(levelDifference / 10d) == 2) { //Level 60
                this.ultimatePenality[player.charSlot_276][0] = 2.2;
                this.ultimatePenality[player.charSlot_276][1] = 1.35;
              }
            } else if(this.ultimateLevelCap == 50) {
              if(Math.round(levelDifference / 10d) == 1) { //Level 60
                this.ultimatePenality[player.charSlot_276][0] = 1.3;
                this.ultimatePenality[player.charSlot_276][1] = 1.08;
              }
            }
          }

          if(this.ultimatePenality[player.charSlot_276][0] > 1) {
            final int currentMax = player.stats.getStat(HP_STAT.get()).getMaxRaw();
            player.stats.getStat(HP_STAT.get()).setMaxRaw(Math.round(Math.round((double) currentMax / this.ultimatePenality[player.charSlot_276][0])));
          }

          this.ultimateZeroSPStart(player);
        } else if(bobj instanceof final MonsterBattleEntity monster) {
          final int enemyId = monster.charId_272;
          for(int x = 0; x < 86; x++) {
            if(enemyId == Integer.parseInt(ultimateData.get(x)[0])) {
              monster.stats.getStat(HP_STAT.get()).setMaxRaw(Integer.parseInt(ultimateData.get(x)[1]));
              monster.stats.getStat(HP_STAT.get()).setCurrent(Integer.parseInt(ultimateData.get(x)[1]));
              monster.attack_34 = Integer.parseInt(ultimateData.get(x)[3]);
              monster.magicAttack_36 = Integer.parseInt(ultimateData.get(x)[4]);
              monster.stats.getStat(SPEED_STAT.get()).setRaw(Integer.parseInt(ultimateData.get(x)[5]));
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

    this.updateMonsterHPNames();

    for(int i = 0; i < battleState_8006e398.getMonsterCount(); i++) {
      final MonsterBattleEntity monster = battleState_8006e398.monsterBents_e50[i].innerStruct_00;
      final int hp = monster.stats.getStat(HP_STAT.get()).getCurrent();
      this.damageTrackerPreviousHP[monster.charSlot_276] = hp;
    }

    if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      GameEngine.EVENTS.postEvent(new HellModeAdjustmentEvent());
    }
  }

  @EventListener public void battleEntityTurn(final BattleEntityTurnEvent<?> event) {
    selectedItemId = -1;

    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    this.updateMonsterHPNames();
    this.updateItemMagicDamage();

    if(event.bent instanceof final PlayerBattleEntity player) {
      this.damageTrackerLog.add(charNames[player.charId_272] + " Turn Started");
      //selectedItemStats = null; TODO

      if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
        if(player.isDragoon()) {
          spGained_800bc950[player.charSlot_276] += 100;
        }

        if(this.bonusItemSP[player.charSlot_276]) {
          this.bonusItemSP[player.charSlot_276] = false;
          if(player.isDragoon()) {
            player.stats.getStat(SP_STAT.get()).setCurrent(player.stats.getStat(SP_STAT.get()).getCurrent() + 50);
            final int newSP = player.stats.getStat(SP_STAT.get()).getCurrent();
            if(player.charSlot_276 == 0) {
              battleState_8006e398.dragoonTurnsRemaining_294[0] = newSP / 100;
            } else if(player.charSlot_276 == 1) {
              battleState_8006e398.dragoonTurnsRemaining_294[1] = newSP / 100;
            } else if(player.charSlot_276 == 2) {
              battleState_8006e398.dragoonTurnsRemaining_294[2] = newSP / 100;
            }
          }
        }

        if("dragoon_modifier:spirit_eater".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) {
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          if(!player.isDragoon() && sp != player.stats.getStat(SP_STAT.get()).getMax()) {
            player.stats.getStat(SP_STAT.get()).setCurrent(sp - 20);
          }
          spGained_800bc950[player.charSlot_276] += 40;
        }

        if(this.ouroboros[player.charSlot_276] && !player.isDragoon()) { //Ouroboros
          player.stats.getStat(SPEED_STAT.get()).addMod(LodMod.id("speed_down"), LodMod.UNARY_STAT_MOD_TYPE.get().make(new UnaryStatModConfig().percent(-50).turns(3)));
          this.ouroboros[player.charSlot_276] = false;
        }

        if("dragoon_modifier:ring_of_elements".equals(player.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) {
          if(((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == player.element) {
            this.ringOfElements[player.charSlot_276]++;
            this.ringOfElementsElement[player.charSlot_276] = ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64;
          } else {
            if(player.element == FIRE_ELEMENT.get() && ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == DIVINE_ELEMENT.get()) {
              this.ringOfElements[player.charSlot_276]++;
              this.ringOfElementsElement[player.charSlot_276] = ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64;
            }

            if(((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == null) {
              this.ringOfElements[player.charSlot_276]--;
            }
          }
        }
      }
    }

    if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      if(("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) && this.flowerStormOverride) {
        this.flowerStormOverride = false;
        for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
          final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
          final BattleEntity27c bobj = state.innerStruct_00;
          if(bobj instanceof final PlayerBattleEntity player) {
            player.powerDefenceTurns_b9 = GameEngine.CONFIG.getConfig(FLOWER_STORM.get());
            player.powerMagicDefenceTurns_bb = GameEngine.CONFIG.getConfig(FLOWER_STORM.get());
          }
        }
      }

      if(event.bent instanceof final PlayerBattleEntity player) {
        this.currentPlayerSlot = player.charSlot_276;
        if("lod:armor_of_legend".equals(player.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString())) {
          this.armorOfLegendTurns += 1;
          if(this.armorOfLegendTurns <= 40) {
            player.defence_38 += 1;
          }
        }

        if("lod:legend_casque".equals(player.equipment_11e.get(EquipmentSlot.HELMET).getRegistryId().toString())) {
          this.legendCasqueTurns += 1;
          if(this.legendCasqueTurns <= 40) {
            player.magicDefence_3a += 1;
          }
        }

        if(player.charId_272 == 0) {
          this.burnAdded = false;

          if(this.burnStackMode) {
            this.burnStacks = 0;
            this.previousBurnStacks = 0;
            this.burnStackMode = false;
          }
        }

        if(player.charId_272 == 2 || player.charId_272 == 8) {
          if(this.shanaStarChildrenHeal[player.charSlot_276] && !player.isDragoon()) {
            this.shanaStarChildrenHeal[player.charSlot_276] = false;
            player.stats.getStat(HP_STAT.get()).setCurrent(player.stats.getStat(HP_STAT.get()).getMax());
          }

          if(this.shanaRapidFire[player.charSlot_276]) {
            this.shanaRapidFire[player.charSlot_276] = false;
            player.dragoonAttack_ac = this.dragonBlockStaff ? 365 * 8 : 365;
          }
        }

        if(this.elementalAttack[player.charSlot_276]) {
          player.element = this.previousElement[player.charSlot_276];
          this.elementalAttack[player.charSlot_276] = false;
          if(player.charId_272 == 2 || player.charId_272 == 8) {
            player.dragoonAttack_ac = this.dragonBlockStaff ? 365 * 8 : 365;
          }
        }

        if(player.charId_272 == 6 && this.meruBoost[player.charSlot_276]) {
          this.meruBoostTurns[player.charSlot_276] -= 1;
          if(this.meruBoostTurns[player.charSlot_276] == 0) {
            this.meruBoost[player.charSlot_276] = false;
            player.stats.getStat(HP_STAT.get()).setMaxRaw(this.meruMaxHpSave[player.charSlot_276]);
            player.magicDefence_3a = this.meruMDFSave[player.charSlot_276];
          }
        }

        if(this.spiritBottle[player.charSlot_276]) {
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          player.stats.getStat(SP_STAT.get()).setCurrent(sp + 80);
          spGained_800bc950[player.charSlot_276] += 80;
          this.spiritBottle[player.charSlot_276] = false;
        }

        if(this.speedBottle[player.charSlot_276]) {
          player.turnValue_4c += 255;
          this.speedBottle[player.charSlot_276] = false;
        }

        if(this.healingBottle[player.charSlot_276]) {
          final int hp = player.stats.getStat(HP_STAT.get()).getCurrent();
          if(hp <= player.stats.getStat(HP_STAT.get()).getMax() * 0.25) {
            player.stats.getStat(HP_STAT.get()).setCurrent(hp + (int) Math.round(player.stats.getStat(HP_STAT.get()).getMax() * 0.7));
            this.healingBottle[player.charSlot_276] = false;
          }
        }

        if(this.sunBottle[player.charSlot_276]) {
          final int mp = player.stats.getStat(MP_STAT.get()).getCurrent();
          this.print("MP: " + mp + " / " + (player.stats.getStat(MP_STAT.get()).getMax() - 20));
          if(mp <= player.stats.getStat(MP_STAT.get()).getMax() - 20) {
            player.stats.getStat(MP_STAT.get()).setCurrent(mp + 20);
            this.sunBottle[player.charSlot_276] = false;
          }
        }

        if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
          if(player.isDragoon()) {
            spGained_800bc950[player.charSlot_276] += 50;
          }
        }
      }
    }


    if(event.bent instanceof final MonsterBattleEntity monster) {
      if(this.elementalBombTurns[monster.charSlot_276] > 0) {
        this.elementalBombTurns[monster.charSlot_276] -= 1;

        if(this.elementalBombTurns[monster.charSlot_276] == 0) {
          monster.displayElement_1c = this.elementalBombPreviousElement[monster.charSlot_276];
          monster.monsterElement_72 = this.elementalBombPreviousElement[monster.charSlot_276];
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
              event.damage *= (int)(Integer.parseInt(spellStats.get(event.attacker.spellId_4e)[3]) / 100d);
          }
        }
      }
    }

    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());

    if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
            /*
                ATTACKING PLAYER
             */
      if(event.attacker instanceof final PlayerBattleEntity player) {
        if(player.isDragoon() && event.attackType.isPhysical()) {
          if(player.element == ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64) { //Dragoon Space physical boost
            if(player.charId_272 == 7) {
              event.damage *= 1.2;
            } else {
              event.damage *= 1.5;
            }
          } else {
            if(player.element == Element.fromFlag(0x80) && ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == Element.fromFlag(0x8)) { //Divine Dart special physical boost
              if("dragoon_modifier:firebrand".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) {
                event.damage *= 1.1; //TODO this doesn't seem right
              } else {
                event.damage *= 1.5;
              }
            }
          }
        }

        if(event.defender instanceof MonsterBattleEntity) {
          final int level = player.level_04;
          if(event.attackType.isPhysical() && (player.charId_272 == 2 || player.charId_272 == 8)) { //Shana AT Boost
            double boost = 1;
            if("lod:detonate_arrow".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) {
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
            this.bonusItemSP[player.charSlot_276] = true;
          }

          if(player.charId_272 == 3) {
            event.damage *= 1.7;
          } else if(player.charId_272 == 5) {
            event.damage *= 1.5;
          } else if(player.charId_272 == 7) {
            event.damage *= 2.2;
          }

          if(this.dragonBlockStaff) {
            event.damage /= 8;
          }
        }

        if(player.charId_272 == 2 || player.charId_272 == 8) {
          if(player.spellId_4e == 10 || player.spellId_4e == 65) { //Star Children full heal on exit
            this.shanaStarChildrenHeal[player.charSlot_276] = true;
          }
        }

        if(event.defender instanceof final MonsterBattleEntity monster) {
          if(this.windMark[event.defender.charSlot_276] > 0) { //Wind mark turn value reduction
            monster.turnValue_4c = Math.max(0, monster.turnValue_4c - 10);
            this.windMark[event.defender.charSlot_276] -= 1;
          }
        }

        if(event.attacker.charId_272 == 0) {
          if(this.burnStackMode) {
            if(this.burnStacks == this.burnStacksMax) {
              if(player.spellId_4e == 0) {
                event.damage *= (1 + (this.burnStacks * this.dmgPerBurn)) * (Integer.parseInt(spellStats.get(2)[3]) / Integer.parseInt(spellStats.get(0)[3])) * 1.5;
              } else if(player.spellId_4e == 1) {
                event.damage *= (1 + (this.burnStacks * this.dmgPerBurn)) * Integer.parseInt(spellStats.get(3)[3]) / Integer.parseInt(spellStats.get(1)[3]);
              } else if(player.spellId_4e == 2) {
                event.damage *= (1 + (this.burnStacks * this.dmgPerBurn)) * 1.5;
              } else {
                event.damage *= 1 + (this.burnStacks * this.dmgPerBurn);
              }
            } else {
              event.damage *= 1 + (this.burnStacks * this.dmgPerBurn) * 1.5;
            }
          } else {
            if(event.attackType == AttackType.DRAGOON_MAGIC_STATUS_ITEMS && !this.burnAdded) {
              if(player.spellId_4e == 0 || player.spellId_4e == 84) {
                this.addBurnStacks(player, this.burnStackFlameShot);
              } else if(player.spellId_4e == 1) {
                this.addBurnStacks(player, this.burnStackExplosion);
              } else if(player.spellId_4e == 2) {
                this.addBurnStacks(player, this.burnStackFinalBurst);
              } else if(player.spellId_4e == 3) {
                this.addBurnStacks(player, this.burnStackRedEye);
              }
              this.burnAdded = true;
            } else if(event.attackType == AttackType.PHYSICAL && player.isDragoon()) {
              this.addBurnStacks(player, this.burnStackAddition);
              this.burnAdded = true;
            }
          }
        }

        if(event.attacker.charId_272 == 3) {
          if(player.spellId_4e == 15) {
            for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
              final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
              final BattleEntity27c bobj = state.innerStruct_00;
              if(bobj instanceof PlayerBattleEntity) {
                final int playerHealedHP = bobj.stats.getStat(HP_STAT.get()).getCurrent();
                final int roseMaxHP = player.stats.getStat(HP_STAT.get()).getMax();
                if(playerHealedHP > 0) {
                  bobj.stats.getStat(HP_STAT.get()).setCurrent((int) Math.min(bobj.stats.getStat(HP_STAT.get()).getMax(), (playerHealedHP + Math.round(roseMaxHP * player.dlevel_06 * 0.0425d))));
                }
              }
            }
          } else if(player.spellId_4e == 19) {
            player.stats.getStat(HP_STAT.get()).setCurrent((int) Math.min(player.stats.getStat(HP_STAT.get()).getMax(), player.stats.getStat(HP_STAT.get()).getCurrent() + event.damage * 0.1d));
          }
        }

        if(event.attacker.charId_272 == 1 || event.attacker.charId_272 == 5) {
          if(this.windMark[event.defender.charSlot_276] == 0 && event.attackType.isMagical() && player.isDragoon()) { //Add wind marks
            if(player.spellId_4e == 5 || player.spellId_4e == 14 || player.spellId_4e == 91) {
              this.windMark[event.defender.charSlot_276] = 1;
            } else if(player.spellId_4e == 7 || player.spellId_4e == 18) {
              this.windMark[event.defender.charSlot_276] = 2;
            } else if(player.spellId_4e == 8) {
              this.windMark[event.defender.charSlot_276] = 3;
            }
          }
        }

        if("dragoon_modifier:giant_axe".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString()) && event.attackType.isPhysical()) {
          if(new Random().nextInt(0, 99) < 20) {
            player.guard_54 = 1;
          }
        }

        if("dragoon_modifier:dragon_beater".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString()) && event.attackType.isPhysical()) {
          final int heal = (int) Math.round(event.damage * 0.01d);
          final int hp = player.stats.getStat(HP_STAT.get()).getCurrent();
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          player.stats.getStat(HP_STAT.get()).setCurrent(hp + Math.min(1000, heal));
          player.stats.getStat(SP_STAT.get()).setCurrent(sp + Math.min(100, heal));
        }

        if("dragoon_modifier:ouroboros".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString()) && player.isDragoon()) {
          final int dragoonTurns = player.charSlot_276 == 0 ? battleState_8006e398.dragoonTurnsRemaining_294[0] : player.charSlot_276 == 1 ? battleState_8006e398.dragoonTurnsRemaining_294[1] : battleState_8006e398.dragoonTurnsRemaining_294[2];
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          if(player.isDragoon() && dragoonTurns >= 2 && sp >= 200) {
            player.stats.getStat(SP_STAT.get()).setCurrent(sp - 100);
            if(player.charSlot_276 == 0) {
              battleState_8006e398.dragoonTurnsRemaining_294[0] = player.stats.getStat(SP_STAT.get()).getCurrent() / 100;
            } else if(player.charSlot_276 == 1) {
              battleState_8006e398.dragoonTurnsRemaining_294[1] = player.stats.getStat(SP_STAT.get()).getCurrent() / 100;
            } else if(player.charSlot_276 == 2) {
              battleState_8006e398.dragoonTurnsRemaining_294[2] = player.stats.getStat(SP_STAT.get()).getCurrent() / 100;
            }
            event.damage *= 2;
            this.ouroboros[player.charSlot_276] = true;
          }
        }

        if("dragoon_modifier:elemental_arrow".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) { //Elemental Arrow
          if(event.defender instanceof final MonsterBattleEntity monster && event.attackType.isPhysical()) {
            final ArrayList<Element> elementsCalculated = new ArrayList<>();
            for(final Element elementArrowsElement : this.elementArrowsElements) {
              if(elementArrowsElement != null) {
                if(!elementsCalculated.contains(elementArrowsElement)) {
                  elementsCalculated.add(elementArrowsElement);
                  if(((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 != null) {
                    int damage = ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64.adjustDragoonSpaceDamage(event.attackType, event.damage, elementArrowsElement);
                    if(damage > event.damage) {
                      event.damage = damage;

                      damage = monster.getElement().adjustAttackingElementalDamage(event.attackType, event.damage, elementArrowsElement);
                      if(damage != event.damage) {
                        event.damage = damage;
                      }
                    }
                  } else {
                    final int damage = monster.getElement().adjustAttackingElementalDamage(event.attackType, event.damage, elementArrowsElement);
                    if(damage > event.damage) {
                      event.damage = damage;
                    }
                  }
                }
              }
            }

            if(new Random().nextInt(0, 99) < 40 && gameState_800babc8.items_2e9.size() < CONFIG.getConfig(CoreMod.INVENTORY_SIZE_CONFIG.get())) {
              Scus94491BpeSegment_8002.giveItem(REGISTRIES.items.getEntry("lod:trans_light").get());
            }
          }

          if(player.item_d4 != null) {
            player.stats.getStat(SP_STAT.get()).setCurrent(player.stats.getStat(SP_STAT.get()).getCurrent() + 100);
          }
        }

        if("dragoon_modifier:magic_hammer".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) {
          if(event.attackType.isPhysical()) {
            event.damage = 0;
          }
          player.stats.getStat(MP_STAT.get()).setCurrent(player.stats.getStat(MP_STAT.get()).getCurrent() + 8);
        }

        if("dragoon_modifier:overcharge_glove".equals(player.equipment_11e.get(EquipmentSlot.WEAPON).getRegistryId().toString())) {
          if(event.defender instanceof final MonsterBattleEntity monster) {
            if(monster.getElement() == THUNDER_ELEMENT.get()) {
              event.damage *= 3;
            }
          }
        }

        for(int i = 0; i < 3; i++) {
          if(this.ringOfElements[i] > 0 && ((Battle)currentEngineState_8004dd04).dragoonSpaceElement_800c6b64 == null) { //Ring of Elements
            if(event.defender instanceof final MonsterBattleEntity monster) {
              if(event.attackType.isPhysical()) {
                for(final Element e : player.equipmentAttackElements_1c) {
                  final int damage = this.ringOfElementsElement[i].adjustDragoonSpaceDamage(event.attackType, event.damage, e);
                  if(damage != event.damage) {
                    event.damage = damage;
                  }
                }
              } else {
                try {
                  final int damage = event.attacker.spell_94.element_08.adjustDragoonSpaceDamage(event.attackType, event.damage, this.ringOfElementsElement[i]);
                  if(damage != event.damage) {
                    event.damage = damage;
                  }
                } catch (final Exception ignored) {}

                try {
                  final int damage = event.attacker.item_d4.getAttackElement().adjustDragoonSpaceDamage(event.attackType, event.damage, this.ringOfElementsElement[i]);
                  if(damage != event.damage) {
                    event.damage = damage;
                  }
                } catch (final Exception ignored) {}
              }
            }
          }
        }

        if(event.defender instanceof final MonsterBattleEntity monster) { //Haschel in party thunder charge
          try {
            if(event.attacker.spell_94.element_08 == THUNDER_ELEMENT.get() && new Random().nextBoolean()) {
              this.thunderCharge[monster.charSlot_276] = Math.min(10, this.thunderCharge[monster.charSlot_276] + 1);
            }
          } catch (final Exception ignored) {}

          try {
            if(event.attacker.item_d4.getAttackElement() == THUNDER_ELEMENT.get() && new Random().nextBoolean()) {
              this.thunderCharge[monster.charSlot_276] = Math.min(10, this.thunderCharge[monster.charSlot_276] + 1);
            }
          } catch (final Exception ignored) {}

          if(event.attackType.isPhysical() && player.equipmentAttackElements_1c.contains(THUNDER_ELEMENT.get()) && new Random().nextBoolean()) {
            this.thunderCharge[monster.charSlot_276] = Math.min(10, this.thunderCharge[monster.charSlot_276] + 1);
          }
        }

        if(player.charId_272 == 4) { //Haschel thunder charge on physical and spark net boost on max stacks and thunder element
          if(event.defender instanceof final MonsterBattleEntity monster) {
            if(player.dlevel_06 > 0) {
              if(event.attackType.isPhysical() && new Random().nextBoolean()) {
                this.thunderCharge[monster.charSlot_276] = Math.min(10, this.thunderCharge[monster.charSlot_276] + 1);
              } else {
                if(player.isDragoon() && player.spellId_4e == 86) {
                  if(this.thunderCharge[monster.charSlot_276] == 10) {
                    this.thunderCharge[monster.charSlot_276] = 0;
                    event.damage *= monster.getElement() == THUNDER_ELEMENT.get() ? 8.8 : 2.93333;
                  }
                }
              }
            }
          }
        }

        if(event.defender instanceof final PlayerBattleEntity defender) { //If Meru's in Wingly Boost Mode all healing is 0
          if(this.meruBoost[defender.charSlot_276]) {
            try {
              if(Integer.parseInt(spellStats.get(event.attacker.spellId_4e)[4]) > 0) {
                event.damage = 0;
              }
            } catch (final Exception ignored) {}

            try {
              /*if(Integer.parseInt(itemStats.get(event.attacker.item_d4.getRegistryId())[11]) == 128) { //TODO Meru Boost with registry
                event.damage = 0;
              }*/
            } catch (final Exception ignored) {}
          }
        }

        if(this.bonusItemSP[player.charSlot_276]) {
          player.item_d4 = null;
        }
      }

            /*
                DEFENDING PLAYER
             */

      if(event.defender instanceof final PlayerBattleEntity defender) {
        if("dragoon_modifier:ring_of_shielding".equals(defender.equipment_11e.get(EquipmentSlot.ACCESSORY).getRegistryId().toString())) { //Ring of Shielding
          final int hp = defender.stats.getStat(HP_STAT.get()).getCurrent();
          if((hp - event.damage) <= 0 && new Random().nextInt(0, 99) < 35) {
            //defender.stats.getStat(null).addMod(LodMod.id("material_shield"), LodMod.UNARY_STAT_MOD_TYPE.get().make(new UnaryStatModConfig().percent(100).turns(5)));
            //defender.stats.getStat(SPEED_STAT.get()).addMod(LodMod.id("magic_shield"), LodMod.UNARY_STAT_MOD_TYPE.get().make(new UnaryStatModConfig().percent(100).turns(5)));
          }
        }

        if(defender.charId_272 == 6) { //If Meru dies in Wingly Boost turn it off
          final int hp = defender.stats.getStat(HP_STAT.get()).getCurrent();
          if(this.meruBoost[defender.charSlot_276] && hp - event.damage <= 0) {
            this.meruBoostTurns[defender.charSlot_276] = 0;
            this.meruBoost[defender.charSlot_276] = false;
            defender.stats.getStat(HP_STAT.get()).setMaxRaw(this.meruMaxHpSave[defender.charSlot_276]);
            defender.magicDefence_3a = this.meruMDFSave[defender.charSlot_276];
          }
        }

        if(this.ringOfElements[defender.charSlot_276] > 0) {
          final int hp = defender.stats.getStat(HP_STAT.get()).getCurrent();
          if(event.damage <= 0) {
            this.ringOfElements[defender.charSlot_276] = 0;
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
          final String equipID = defender.equipment_11e.get(EquipmentSlot.ARMOUR).getRegistryId().toString();
          final int armorEquipped = Integer.parseInt(equipID.split(":")[1].substring(1));

          try {
            attackElement = event.attacker.item_d4.getAttackElement();
          } catch (final Exception ignored) {}

          if(attackElement == null) {
            attackElement = event.attacker.spell_94.element_08;
          }

          //Divine Dragon Armor 15% elemental reduction instead of half
          if(attackElement == FIRE_ELEMENT.get() && armorEquipped == 51) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          } else if(attackElement == WIND_ELEMENT.get() && armorEquipped == 52) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          } else if(attackElement == EARTH_ELEMENT.get() && armorEquipped == 56) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          } else if(attackElement == THUNDER_ELEMENT.get() && armorEquipped == 61) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          } else if(attackElement == LIGHT_ELEMENT.get() && armorEquipped == 67) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          } else if(attackElement == DARK_ELEMENT.get() && armorEquipped == 68) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          } else if(attackElement == WATER_ELEMENT.get() && armorEquipped == 69) {
            event.damage = (int) Math.round(event.damage / 1.15d);
          }
        }

        this.print("PROTECTION SHIELD: " + this.protectionShield[defender.charSlot_276]);
        if(this.protectionShield[defender.charSlot_276] > 0) {

          if(event.damage <= this.protectionShield[defender.charSlot_276]) {
            this.protectionShield[defender.charSlot_276] -= event.damage;
            event.damage = 0;
          } else {
            event.damage -= this.protectionShield[defender.charSlot_276];
            this.protectionShield[defender.charSlot_276] = 0;
          }
        }
        this.print("PROTECTION SHIELD: " + this.protectionShield[defender.charSlot_276]);
      }
    }

    if(this.ultimateBattle) {
      if(event.attacker instanceof final PlayerBattleEntity player && event.defender instanceof MonsterBattleEntity) {
        if(this.ultimatePenality[player.charSlot_276][1] > 1) { //Damage penalty for over leveled ultiamte boss
          event.damage /= this.ultimatePenality[player.charSlot_276][1];
        }
      }

      if(event.attacker instanceof MonsterBattleEntity && event.defender instanceof final PlayerBattleEntity player) {
        if(this.ultimatePenality[player.charSlot_276][1] > 1) { //Damage penalty for over leveled ultiamte boss
          event.damage *= this.ultimatePenality[player.charSlot_276][1];
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

    if(this.ultimateBattle) { //Ultimate Boss effects per attack
      if(event.attacker instanceof final MonsterBattleEntity monster) {
        this.ultimateGuardBreak((PlayerBattleEntity) event.defender, monster, event);
        this.ultimateMPAttack((PlayerBattleEntity) event.defender, monster, event);
      }
    }

    this.updateEnrageMode(event);
    this.updateElementalBomb(event);
    this.updateDamageTracker(event);
  }

  @EventListener public void attackSpGainEvent(final AttackSpGainEvent event) {
    final PlayerBattleEntity bent = event.bent;

    if(bent.charId_272 == 2 || bent.charId_272 == 8) {
      event.sp = Integer.parseInt(shanaSpGain.getFirst()[bent.dlevel_06 - 1]);
    }
  }

  public void addBurnStacks(final PlayerBattleEntity dart, final int stacks) {
    if(!this.burnStackMode) {
      this.previousBurnStacks = this.burnStacks;
      final int dlv = dart.dlevel_06;
      this.burnStacksMax = dlv== 0 ? 0 : dlv == 1 ? 3 : dlv == 2 ? 6 : dlv == 3 ? 9 : dlv == 7 ? 15 : 12;
      this.burnStacks = Math.min(this.burnStacksMax, this.burnStacks + stacks);

      if(this.burnStacks >= 4 && this.previousBurnStacks < 4) {
        dart.stats.getStat(MP_STAT.get()).setCurrent(dart.stats.getStat(MP_STAT.get()).getCurrent() + 10);
      } else if(this.burnStacks >= 8 && this.previousBurnStacks < 8) {
        dart.stats.getStat(MP_STAT.get()).setCurrent(dart.stats.getStat(MP_STAT.get()).getCurrent() + 20);
      } else if(this.burnStacks >= 12 && this.previousBurnStacks < 12) {
        dart.stats.getStat(MP_STAT.get()).setCurrent(dart.stats.getStat(MP_STAT.get()).getCurrent() + 30);
      }
    }
  }

  public void dramodBurnStacks(final int spellId) {
    if(spellId >= 0 && spellId <= 3) {
      if(this.burnStackMode && this.burnStacks > 0) {
        int damage = Integer.parseInt(spellStats.get(spellId)[3]);
        String newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", (1 + (this.burnStacks * this.dmgPerBurn))));

        if(this.burnStacks == this.burnStacksMax) {
          if(spellId == 0) {
            damage *= (1 + (this.burnStacks * this.dmgPerBurn)) * (Integer.parseInt(spellStats.get(2)[3]) / Integer.parseInt(spellStats.get(0)[3])) * 1.5;
            newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", ((1 + (this.burnStacks * this.dmgPerBurn)) * (Integer.parseInt(spellStats.get(2)[3]) / Integer.parseInt(spellStats.get(0)[3])) * 1.5)));
          } else if(spellId == 1)  {
            damage *= (1 + (this.burnStacks * this.dmgPerBurn)) * Integer.parseInt(spellStats.get(3)[3]) / Integer.parseInt(spellStats.get(1)[3]);
            newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", (1 + (this.burnStacks * this.dmgPerBurn)) * Integer.parseInt(spellStats.get(3)[3]) / Integer.parseInt(spellStats.get(1)[3])));
          } else if(spellId == 2)  {
            damage *= (1 + (this.burnStacks * this.dmgPerBurn)) * 1.5;
            newDescription = spellStats.get(spellId)[13].replace("1.00", String.format("%.2f", (1 + (this.burnStacks * this.dmgPerBurn)) * 1.5));
          } else {
            damage *= 1 + (this.burnStacks * this.dmgPerBurn);
          }
        } else {
          damage *= 1 + (this.burnStacks * this.dmgPerBurn) * 1.5;
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
          this.burnStacks == this.burnStacksMax ? 0 : Integer.parseInt(spellStats.get(spellId)[6]),
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
    final int spellId = spell.spellId;

    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());

    if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      this.dramodBurnStacks(spellId);
    }
  }

  @EventListener public void dragonBlockStaffOn(final DragonBlockStaffOnEvent event) {
    final String difficulty = CONFIG.getConfig(DIFFICULTY.get());
    if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      this.dragonBlockStaff = true;
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof final PlayerBattleEntity player) {
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
    if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      this.dragonBlockStaff = false;
      for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        final BattleEntity27c bobj = state.innerStruct_00;
        if(bobj instanceof final PlayerBattleEntity player) {
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

    if("Japan Demo".equals(difficulty)) {
      event.returnItem = event.item == LodItems.PSYCHE_BOMB_X.get();
    }
  }

  public void updateEnrageMode(final AttackEvent event) {
    if(GameEngine.CONFIG.getConfig(ENRAGE_MODE.get()) == EnrageMode.ON) {
      for(int i = 0; i < battleState_8006e398.getMonsterCount(); i++) {
        final MonsterBattleEntity monster = battleState_8006e398.monsterBents_e50[i].innerStruct_00;
        final int hp = monster.stats.getStat(HP_STAT.get()).getCurrent();
        final int maxHp = monster.stats.getStat(HP_STAT.get()).getMax();
        if(hp <= maxHp / 2 && this.enrageMode[i] == 0) {
          monster.attack_34 = (int) Math.round(monster.attack_34 * 1.1d);
          monster.magicAttack_36 = (int) Math.round(monster.magicAttack_36 * 1.1d);
          monster.defence_38 = (int) Math.round(monster.defence_38 * 1.1d);
          monster.magicDefence_3a = (int) Math.round(monster.magicDefence_3a * 1.1d);
          this.enrageMode[i] = 1;
        }
        if(hp <= maxHp / 4 && this.enrageMode[i] == 1) {
          monster.attack_34 = (int) Math.round(monster.attack_34 * 1.136365d);
          monster.magicAttack_36 = (int) Math.round(monster.magicAttack_36 * 1.136365d);
          monster.defence_38 = (int) Math.round(monster.defence_38 * 1.136365d);
          monster.magicDefence_3a = (int) Math.round(monster.magicDefence_3a * 1.136365d);
          this.enrageMode[i] = 2;
        }
      }
    }
  }

  public void updateItemMagicDamage() {
    if(GameEngine.CONFIG.getConfig(DAMAGE_TRACKER.get()) == DamageTracker.ON) {
      for(int i = 0; i < battleState_8006e398.getMonsterCount(); i++) {
        final MonsterBattleEntity monster = battleState_8006e398.monsterBents_e50[i].innerStruct_00;
        final int hp = monster.stats.getStat(HP_STAT.get()).getCurrent();
        if(hp < this.damageTrackerPreviousHP[monster.charSlot_276]) {
          final int difference = this.damageTrackerPreviousHP[monster.charSlot_276] - hp;
          this.damageTracker[this.damageTrackerPreviousCharacter][this.damageTrackerPreviousAttackType] += difference;
          this.damageTrackerLog.add(charNames[this.damageTrackerPreviousCharacterID] + " - Multiplier - " + difference);
          this.damageTrackerPreviousHP[monster.charSlot_276] = hp;
        }
      }
    }
  }

  private boolean isAttackItem(final Item item) {
    return item == LodItems.BURNING_WAVE.get() || item == LodItems.FROZEN_JET.get() || item == LodItems.DOWN_BURST.get() || item == LodItems.GRAVITY_GRABBER.get() || item == LodItems.SPECTRAL_FLASH.get() || item == LodItems.NIGHT_RAID.get() || item == LodItems.FLASH_HALL.get() || item == LodItems.PSYCHE_BOMB.get() || item == LodItems.PSYCHE_BOMB_X.get();
  }

  public void updateElementalBomb(final AttackEvent event) { //TODO item registries
    if(GameEngine.CONFIG.getConfig(ELEMENTAL_BOMB.get()) == ElementalBomb.ON) {
      if(event.attacker instanceof final PlayerBattleEntity player) {
        try {
          if(this.isAttackItem(player.item_d4) && event.defender instanceof final MonsterBattleEntity monster) {
            //for(int i = 0; i < monsterCount_800c6768.get(); i++) {
            if(this.elementalBombTurns[monster.charSlot_276] == 0) {
              final Element swapTo = player.item_d4.getAttackElement();
              this.elementalBombPreviousElement[monster.charSlot_276] = monster.getElement();
              this.elementalBombTurns[monster.charSlot_276] = 5;
              monster.monsterElement_72 = swapTo;
              monster.displayElement_1c = swapTo;
            }
          }
          //}
        } catch (final Exception ignored) {}
      }
    }
  }

  @EventListener public void shanaItemSpGain(final AttackEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
      if(event.attacker instanceof final PlayerBattleEntity player && event.defender instanceof final MonsterBattleEntity monster) {
        if((player.charId_272 == 2 || player.charId_272 == 8) && player.item_d4 != null) {
          final int sp = player.getStat(BattleEntityStat.CURRENT_SP);
          spGained_800bc950[player.charSlot_276] += 50;
          player.setStat(BattleEntityStat.CURRENT_SP, sp + 50);
          player.item_d4 = null;
        }
      }
    }
  }

  public void updateDamageTracker(final AttackEvent attack) {
    if(GameEngine.CONFIG.getConfig(DAMAGE_TRACKER.get()) == DamageTracker.ON) {
      if(attack.attacker instanceof final PlayerBattleEntity player && attack.defender instanceof final MonsterBattleEntity monster) {
        if(player.isDragoon()) {
          if(attack.attackType.isPhysical()) {
            this.damageTrackerPreviousAttackType = 0;
            this.damageTracker[player.charSlot_276][0] += attack.damage;
            this.damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - D.Physical - " + attack.damage);
          } else {
            this.damageTrackerPreviousAttackType = 1;
            this.damageTracker[player.charSlot_276][1] += attack.damage;
            this.damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - D.Magical - " + attack.damage);
          }
        } else {
          if(attack.attackType.isPhysical()) {
            this.damageTrackerPreviousAttackType = 2;
            this.damageTracker[player.charSlot_276][2] += attack.damage;
            this.damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - Physical - " + attack.damage);
          } else {
            this.damageTrackerPreviousAttackType = 3;
            this.damageTracker[player.charSlot_276][3] += attack.damage;
            this.damageTrackerLog.add(charNames[gameState_800babc8.charIds_88[player.charSlot_276]] + " - Magical - " + attack.damage);
          }
        }

        final int hp = monster.stats.getStat(HP_STAT.get()).getCurrent();
        if(attack.damage > hp && hp > 0 && hp != this.damageTrackerPreviousHP[monster.charSlot_276]) {
          this.damageTracker[player.charSlot_276][4] = attack.damage - hp;
        }

        this.damageTrackerPreviousCharacter = player.charSlot_276;
        this.damageTrackerPreviousCharacterID = player.charId_272;
        this.damageTrackerPreviousHP[monster.charSlot_276] = hp - attack.damage;
      }
    }
  }

  public void ultimateZeroSPStart(final PlayerBattleEntity player) {
    final int encounterId = encounterId_800bb0f8;

    if(encounterId == 413 || encounterId == 415 || encounterId == 403) {
      player.stats.getStat(SP_STAT.get()).setCurrent(0);
    }
  }

  public void updateMonsterHPNames() {
    if(GameEngine.CONFIG.getConfig(MONSTER_HP_NAMES.get()) == MonsterHPNames.ON) {
      for(int i = 0; i < 10; i++) {
        final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
        if(state != null) {
          final BattleEntity27c bobj = state.innerStruct_00;
          if(bobj instanceof MonsterBattleEntity) {
            final int hp = bobj.stats.getStat(HP_STAT.get()).getCurrent();
            ((Battle)currentEngineState_8004dd04).currentEnemyNames_800c69d0[bobj.charSlot_276] = String.valueOf(hp);
          }
        }
      }
    }
  }

  @EventListener public void statDisplay(final StatDisplayEvent event) {
    if(event.player.charId_272 == 0 && this.burnStacksMax > 0) {
      final MV transforms = new MV();
      final float burn = this.burnStacks == 0 ? 0.0f : (float)this.burnStacks / this.burnStacksMax;
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

      Scus94491BpeSegment_8002.renderText(String.valueOf(this.burnStacks), event.charSlot * 94 + 16, 226.0f, TextColour.WHITE, 0);
    }
  }

  @EventListener public void selectedTarget(final SingleMonsterTargetEvent event) {
    for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
      final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
      final BattleEntity27c bent = state.innerStruct_00;
      if(bent instanceof final PlayerBattleEntity player) {
        if(player.charId_272 == 1 || player.charId_272 == 5) {
          final MV transforms = new MV();
          final float wind = this.windMark[event.monster.charSlot_276] == 0 ? 0.0f : (float)this.windMark[event.monster.charSlot_276] / 3;
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

          Scus94491BpeSegment_8002.renderText(String.valueOf(this.windMark[event.monster.charSlot_276]), player.charSlot_276 * 94 + 16, 226.0f, TextColour.WHITE, 0);
        } else if(player.charId_272 == 4) {
          final MV transforms = new MV();
          final float thunder = this.thunderCharge[event.monster.charSlot_276] == 0 ? 0.0f : (float)this.thunderCharge[event.monster.charSlot_276] / 10;
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

          Scus94491BpeSegment_8002.renderText(String.valueOf(this.thunderCharge[event.monster.charSlot_276]), player.charSlot_276 * 94 + 16, 226.0f, TextColour.WHITE, 0);
        }
      }
    }
  }

  @EventListener public void battleEnded(final BattleEndedEvent event) {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());
    this.updateItemMagicDamage();

    if(ArrayUtils.contains(this.bossEncounters, encounterId_800bb0f8)) {
      livingCharCount_800bc97c = 3;
      System.arraycopy(gameState_800babc8.charIds_88, 0, livingCharIds_800bc968, 0, 3);
    }

    if(this.faustBattle) {
      this.faustBattle = false;
      try {
        GameEngine.CONFIG.setConfig(FAUST_DEFEATED.get(), String.valueOf(Integer.parseInt(GameEngine.CONFIG.getConfig(FAUST_DEFEATED.get())) + 1));
      } catch (final NumberFormatException ex) {
        GameEngine.CONFIG.setConfig(FAUST_DEFEATED.get(), String.valueOf(1));
      }
    }

    if(this.ultimateBattle) {
      this.ultimateBattle = false;

      final int ultimateBossesDefeated = Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get()));
      int ultimateBossSelected = GameEngine.CONFIG.getConfig(ULTIMATE_BOSS.get()) - 1;
      final int mapId = submapCut_80052c30;

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

    if(GameEngine.CONFIG.getConfig(DAMAGE_TRACKER.get()) == DamageTracker.ON && !this.damageTrackerPrinted && gameState_800babc8.charIds_88[0] >= 0 && gameState_800babc8.charIds_88[1] >= 0 && gameState_800babc8.charIds_88[2] >= 0) {
      try {
        final double total = IntStream.of(this.damageTracker[0]).sum() + IntStream.of(this.damageTracker[1]).sum() + IntStream.of(this.damageTracker[2]).sum();
        final PrintWriter pw = new PrintWriter("./mods/Damage Tracker/" + new SimpleDateFormat("yyyy-MMdd--hh-mm-ss").format(new Date()) + " - E" + encounterId_800bb0f8 + ".txt");
        pw.printf("======================================================================%n");
        pw.printf("=                           Damage Tracker                           =%n");
        pw.printf("======================================================================%n");
        pw.printf("| %-20s | %-20s | %-20s |%n", charNames[gameState_800babc8.charIds_88[0]], charNames[gameState_800babc8.charIds_88[1]], charNames[gameState_800babc8.charIds_88[2]]);
        pw.printf("----------------------------------------------------------------------%n");
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "D.Physical", this.damageTracker[0][0], "D.Physical", this.damageTracker[1][0], "D.Physical", this.damageTracker[2][0]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "D.Magical", this.damageTracker[0][1], "D.Magical", this.damageTracker[1][1],"D.Magical", this.damageTracker[2][1]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "Physical", this.damageTracker[0][2], "Physical", this.damageTracker[1][2],"Physical", this.damageTracker[2][2]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "Magical", this.damageTracker[0][3], "Magical", this.damageTracker[1][3],"Magical", this.damageTracker[2][3]);
        pw.printf("| %-10s %-9s | %-10s %-9s | %-10s %-9s |%n", "Total", IntStream.of(this.damageTracker[0]).sum(), "Total",  IntStream.of(this.damageTracker[1]).sum(), "Total",  IntStream.of(this.damageTracker[2]).sum());
        pw.printf("----------------------------------------------------------------------%n");
        pw.printf("%-13s %.2f%%%n", charNames[gameState_800babc8.charIds_88[0]], (IntStream.of(this.damageTracker[0]).sum() - this.damageTracker[0][4] * 2) / total * 100);
        pw.printf("%-13s %.2f%%%n", charNames[gameState_800babc8.charIds_88[1]], (IntStream.of(this.damageTracker[1]).sum() - this.damageTracker[1][4] * 2) / total * 100);
        pw.printf("%-13s %.2f%%%n", charNames[gameState_800babc8.charIds_88[2]], (IntStream.of(this.damageTracker[2]).sum() - this.damageTracker[2][4] * 2) / total * 100);
        pw.printf("Grand Total   " + total + "%n");
        pw.printf("Encounter     " + encounterId_800bb0f8 + "%n%n");
        pw.printf("===========================================================================================================%n");
        pw.printf("=                                                Equipment                                                =%n");
        pw.printf("===========================================================================================================%n");
        pw.printf("| Name     | Weapon           | Helmet           | Armor            | Shoes            | Accessory        |%n");
        pw.printf("-----------------------------------------------------------------------------------------------------------%n");
        for(int i = 0; i < this.damageTrackerEquips.length; i++) { //TODO damage tracker registries
          //pw.printf("| %-8s | %-16s | %-16s | %-16s | %-16s | %-16s |%n", charNames[gameState_800babc8.charIds_88[i]], equipStats.get(this.damageTrackerEquips[i][0])[29], equipStats.get(this.damageTrackerEquips[i][1])[29], equipStats.get(this.damageTrackerEquips[i][2])[29], equipStats.get(this.damageTrackerEquips[i][3])[29], equipStats.get(this.damageTrackerEquips[i][4])[29]);
        }
        pw.printf("===========================================================================================================%n%n");
        for(final String s : this.damageTrackerLog) {
          pw.printf(s + "%n");
        }
        pw.flush();
        pw.close();
        this.damageTrackerPrinted = true;
      } catch (final FileNotFoundException e) {
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
    event.shopType = "e".equals(shopItems.get(event.shopId)[0].substring(0, 1)) ? 0 : 1;
  }

  @EventListener public void shopEquipment(final ShopEquipmentEvent event) {
    event.equipment.clear();

    for(int i = 0; i < 16; i++) {
      final String id = shopItems.get(event.shopId)[i];
      if(!id.startsWith("N")) {
        //TODO shopEvent
        //final Equipment equipment = REGISTRIES.equipment.getEntry(equipmentIdMap.get(Integer.parseInt(id.substring(1)))).get();
        //event.equipment.add(new ShopScreen.ShopEntry<>(equipment, equipment.getPrice() * 2));
      }
    }
  }

  @EventListener public void shopItem(final ShopItemEvent event) {
    /*event.items.clear();
    for(int i = 0; i < 16; i++) {
      final String id = shopItems.get(event.shopId)[i];
      if(!id.startsWith("N")) {
        final Item item = REGISTRIES.items.getEntry(itemIdMap.get(Integer.parseInt(id.substring(1)))).get();

        if(event.shopId == 40) {
          final int price = item.getPrice() * 2;
          /*if(item.getName().equals("Spirit Potion")) { //TODO
            price = 300;
          } else if(item.getName().equals("Total Vanishing")) {
            price = 600;
          } else if(item.getName().equals("Healing Rain")) {
            price = 900;
          }*/
          /*event.items.add(new ShopScreen.ShopEntry<>(item, price));
        } else if(event.shopId == 41) {
          event.items.add(new ShopScreen.ShopEntry<>(item, 1000));
        } else {
          event.items.add(new ShopScreen.ShopEntry<>(item, item.getPrice() * 2));
        }
      }
    }*/
  }

  /*@EventListener public void shopSellPrice(final ShopSellPriceEvent shopItem) {
    shopItem.price = Integer.parseInt(shopPrices.get(shopItem.itemId)[0]);
  }*/

  //endregion

  //region Ultimate
  public void ultimateGuardBreak(final PlayerBattleEntity player, final MonsterBattleEntity monster, final AttackEvent attack) {
    final int encounterId = encounterId_800bb0f8;

    if(encounterId == 415) {
      if(!attack.attackType.isPhysical()) {
        if(monster.spellId_4e == 117) {
          player.guard_54 = 0;
        }
      }
    }
  }


  public void ultimateMPAttack(final PlayerBattleEntity player, final MonsterBattleEntity monster, final AttackEvent attack) {
    final int encounterId = encounterId_800bb0f8;

    if(attack.damage > 0) {
      if(encounterId == 415) {
        if(attack.attackType.isPhysical()) {
          if(monster.spellId_4e == 33) {
            player.stats.getStat(MP_STAT.get()).setCurrent(Math.max(0, player.stats.getStat(MP_STAT.get()).getCurrent() - 10));
          }
        }
      }
    }
  }
  //endregion

  //region Hotkey
  @EventListener public void inputPressed(final InputPressedEvent event) {
    this.hotkey.add(event.inputAction);
    this.dramodHotkeys();
  }

  @EventListener public void inputReleased(final InputReleasedEvent event) {
    this.hotkey.remove(event.inputAction);
  }

  public void dramodHotkeys() {
    final String difficulty = GameEngine.CONFIG.getConfig(DIFFICULTY.get());

    if(engineState_8004dd20 == EngineStateEnum.COMBAT_06) { // Combat
      if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1) && this.hotkey.contains(InputAction.DPAD_UP)) { //Exit Dragoon Slot 1
        if(battleState_8006e398.dragoonTurnsRemaining_294[0] > 0) {
          battleState_8006e398.dragoonTurnsRemaining_294[0] = 1;
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1) && this.hotkey.contains(InputAction.DPAD_RIGHT)) { //Exit Dragoon Slot 2
        if(battleState_8006e398.dragoonTurnsRemaining_294[1] > 0) {
          battleState_8006e398.dragoonTurnsRemaining_294[1] = 1;
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1) && this.hotkey.contains(InputAction.DPAD_LEFT)) { //Exit Dragoon Slot 3
        if(battleState_8006e398.dragoonTurnsRemaining_294[2] > 0) {
          battleState_8006e398.dragoonTurnsRemaining_294[2] = 1;
        }
      }

      if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
        if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_WEST)) { //Burn Stacks Mode
          if(this.burnStacks > 0) {
            this.burnStackMode = !this.burnStackMode;
          }
        } else if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && this.hotkey.contains(InputAction.DPAD_UP)) { //Dragoon Guard Slot 1
          final PlayerBattleEntity player = battleState_8006e398.playerBents_e40[0].innerStruct_00;
          final int dragoonTurns = battleState_8006e398.dragoonTurnsRemaining_294[0];
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          if(player.isDragoon() && player.dlevel_06 >= 6 && dragoonTurns > 1 && sp >= 100) {
            battleState_8006e398.dragoonTurnsRemaining_294[0] -= 1;
            player.stats.getStat(SP_STAT.get()).setCurrent(sp - 100);
            player.guard_54 = 1;
          }
        } else if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && this.hotkey.contains(InputAction.DPAD_RIGHT)) { //Dragoon Guard Slot 2
          final PlayerBattleEntity player = battleState_8006e398.playerBents_e40[1].innerStruct_00;
          final int dragoonTurns = battleState_8006e398.dragoonTurnsRemaining_294[1];
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          if(player.isDragoon() && player.dlevel_06 >= 6 && dragoonTurns > 1 && sp >= 100) {
            battleState_8006e398.dragoonTurnsRemaining_294[1] -= 1;
            player.stats.getStat(SP_STAT.get()).setCurrent(sp - 100);
            player.guard_54 = 1;
          }
        } else if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && this.hotkey.contains(InputAction.DPAD_LEFT)) { //Dragoon Guard Slot 3
          final PlayerBattleEntity player = battleState_8006e398.playerBents_e40[2].innerStruct_00;
          final int dragoonTurns = battleState_8006e398.dragoonTurnsRemaining_294[2];
          final int sp = player.stats.getStat(SP_STAT.get()).getCurrent();
          if(player.isDragoon() && player.dlevel_06 >= 6 && dragoonTurns > 1 && sp >= 100) {
            battleState_8006e398.dragoonTurnsRemaining_294[2] -= 1;
            player.stats.getStat(SP_STAT.get()).setCurrent(sp - 100);
            player.guard_54 = 1;
          }
        } else if(this.hotkey.contains(InputAction.BUTTON_SOUTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_1)) { // Shana Rapid fire
          for(int i = 0; i < 0x48; i++) {
            try {
              final ScriptState<?> state = scriptStatePtrArr_800bc1c0[i];
              if((state.name.contains("Char ID 2") || state.name.contains("Char Id 8"))) {
                for(int x = 0; x < battleState_8006e398.getAllBentCount(); x++) {
                  final ScriptState<? extends BattleEntity27c> playerstate = battleState_8006e398.allBents_e0c[x];
                  final BattleEntity27c bobj = playerstate.innerStruct_00;
                  if(bobj instanceof final PlayerBattleEntity player) {
                    if(player.isDragoon() && this.shanaRapidFireContinue[player.charSlot_276]) {
                      if(scriptStatePtrArr_800bc1c0[i].offset_18 == 0x1d2) {
                        scriptStatePtrArr_800bc1c0[i].offset_18 = 0x2050; //TODO not this lol
                        this.shanaRapidFireCount[player.charSlot_276]++;
                        if(this.shanaRapidFireCount[player.charSlot_276] == 2) {
                          this.shanaRapidFireContinue[player.charSlot_276] = false;
                        }
                      }
                      break;
                    }
                  }
                }
              }
            } catch (final Exception ignored) {}
          }
        } else if(this.hotkey.contains(InputAction.BUTTON_SOUTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1)) { //Shana Rapid Fire Activator
          for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
            final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
            final BattleEntity27c bobj = state.innerStruct_00;
            if(bobj instanceof final PlayerBattleEntity player) {
              if((player.charId_272 == 2 || player.charId_272 == 8) && player.charSlot_276 == this.currentPlayerSlot && !this.shanaRapidFire[player.charSlot_276] && player.isDragoon() && player.dlevel_06 >= 6) {
                final int mp = player.stats.getStat(MP_STAT.get()).getCurrent();
                if(mp >= 20) {
                  player.stats.getStat(MP_STAT.get()).setCurrent(mp - 20);
                  this.shanaRapidFire[player.charSlot_276] = true;
                  this.shanaRapidFireContinue[player.charSlot_276] = true;
                  this.shanaRapidFireCount[player.charSlot_276] = 0;
                  player.dragoonAttack_ac = this.dragonBlockStaff ? 165 * 8 : 165;
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
                                int mp = player.stats.getStat(MP_STAT.get()).getCurrent();
                                if(mp >= 100) {
                                    player.stats.getStat(MP_STAT.get()).setCurrent(mp - 100);
                                    previousElement[player.charSlot_276] = player.element;
                                    elementalAttack[player.charSlot_276] = true;
                                    player.element = Element.fromFlag(32);
                                    player.dragoonAttack_ac = dragonBlockStaff ? 550 * 8 : 550;
                                }
                            }
                        }
                    }*/
        } else if(this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_2)) { //Meru Boost
          for(int i = 0; i < battleState_8006e398.getAllBentCount(); i++) {
            final ScriptState<? extends BattleEntity27c> state = battleState_8006e398.allBents_e0c[i];
            final BattleEntity27c bobj = state.innerStruct_00;
            if(bobj instanceof final PlayerBattleEntity player) {
              if(player.charId_272 == 6 && player.charSlot_276 == this.currentPlayerSlot && player.isDragoon() && player.dlevel_06 >= 7) {
                final int mp = player.stats.getStat(MP_STAT.get()).getCurrent();
                if(mp >= 100) {
                  final int maxHP = player.stats.getStat(HP_STAT.get()).getMax();
                  player.stats.getStat(MP_STAT.get()).setCurrent(mp - 100);
                  this.meruBoost[player.charSlot_276] = true;
                  this.meruBoostTurns[player.charSlot_276] = 5;
                  this.meruMDFSave[player.charSlot_276] = player.magicDefence_3a;
                  this.meruMaxHpSave[player.charSlot_276] = maxHP;
                  player.stats.getStat(HP_STAT.get()).setMaxRaw(maxHP * 3);
                  player.stats.getStat(HP_STAT.get()).setCurrent(maxHP * 3);
                }
              }
            }
          }
        }
      }
    } else {
      if(this.hotkey.contains(InputAction.BUTTON_CENTER_1) && this.hotkey.contains(InputAction.BUTTON_THUMB_1)) { //Add Shana
        gameState_800babc8.charData_32c[2].partyFlags_04 = gameState_800babc8.charData_32c[2].partyFlags_04 == 0 ? 3 : 0;
      } else if(this.hotkey.contains(InputAction.BUTTON_CENTER_1) && this.hotkey.contains(InputAction.BUTTON_THUMB_2)) { //Add Lavitz
        gameState_800babc8.charData_32c[1].partyFlags_04 = gameState_800babc8.charData_32c[1].partyFlags_04 == 0 ? 3 : 0;
      } else if(this.hotkey.contains(InputAction.BUTTON_SOUTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_1)) { //Add Dragoons Start
        final int mapId = submapCut_80052c30;
        if(mapId == 10) {
          gameState_800babc8.goods_19c[0] ^= 1 << 0;
          gameState_800babc8.goods_19c[0] ^= 1 << 1;
          gameState_800babc8.goods_19c[0] ^= 1 << 2;
          gameState_800babc8.goods_19c[0] ^= 1 << 3;
          gameState_800babc8.goods_19c[0] ^= 1 << 4;
          gameState_800babc8.goods_19c[0] ^= 1 << 5;
          gameState_800babc8.goods_19c[0] ^= 1 << 6;
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_SOUTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1)) { //Solo/All Character Start
        final int mapId = submapCut_80052c30;
        if(mapId == 10) {
          for(int i = 0; i < 9; i++) {
            gameState_800babc8.charData_32c[i].partyFlags_04 = 3;
            gameState_800babc8.charData_32c[i].dlevel_13 = 1;
            gameState_800babc8.charData_32c[i].level_12 = 1;
            gameState_800babc8.charData_32c[i].xp_00 = 0;
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.WEAPON, REGISTRIES.equipment.getEntry("lod:broad_sword").get());
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.HELMET, REGISTRIES.equipment.getEntry("lod:bandana").get());
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.ARMOUR, REGISTRIES.equipment.getEntry("lod:leather_armor").get());
            gameState_800babc8.charData_32c[i].equipment_14.put(EquipmentSlot.BOOTS, REGISTRIES.equipment.getEntry("lod:leather_boots").get());
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
          if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
            if(Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get())) >= 34) {
              gameState_800babc8.goods_19c[0] ^= 1 << 7;
              if(mapId == 736) {
                gameState_800babc8.goods_19c[0] |= 1 << 0;
              }
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

          this.faustBattle = true;
        } else if(mapId == 190) {
          submapCut_80052c30 = 177;
          ((SMap)currentEngineState_8004dd04).smapLoadingStage_800cb430 = SubmapState.CHANGE_SUBMAP_4;
        } else if(mapId == 177) {
          final ScriptState<SubmapObject210> state = ((SMap)currentEngineState_8004dd04).sobjs_800c6880[0];
          final float posX = state.innerStruct_00.model_00.coord2_14.coord.transfer.x;
          if(posX >= 580.0f && posX <= 603.0f) {
            state.innerStruct_00.model_00.coord2_14.coord.transfer.x += 100.0f;
          }
        } else if(mapId == 181) {
          final ScriptState<SubmapObject210> state = ((SMap)currentEngineState_8004dd04).sobjs_800c6880[0];
          final float posX = state.innerStruct_00.model_00.coord2_14.coord.transfer.x;
          final float posZ = state.innerStruct_00.model_00.coord2_14.coord.transfer.z;
          if((posX >= 190.0f && posX <= 230.0f) && (posZ >= 260.0f && posZ <= 290.0f)) {
            submapCut_80052c30 = 182;
            ((SMap)currentEngineState_8004dd04).smapLoadingStage_800cb430 = SubmapState.CHANGE_SUBMAP_4;
          }
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_SOUTH) && this.hotkey.contains(InputAction.BUTTON_THUMB_2)) { //Add all party members back
        for(int i = 0; i < 9; i++) {
          gameState_800babc8.charData_32c[i].partyFlags_04 = 3;
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_SOUTH) && this.hotkey.contains(InputAction.BUTTON_CENTER_2)) { //???
        gameState_800babc8.charData_32c[8].partyFlags_04 = 0;
      } else if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_EAST)) { //Level Up Party
        int highestInPartyEXP = 0;
        boolean maxedSwapEXP = false;
        for(int i = 0; i < 9; i++) {
          if(gameState_800babc8.charData_32c[i].partyFlags_04 > 0 && gameState_800babc8.charData_32c[i].xp_00 > highestInPartyEXP) {
            highestInPartyEXP = gameState_800babc8.charData_32c[i].xp_00;
          }
        }

        if("Hard Mode".equals(difficulty) || "Us + Hard Bosses".equals(difficulty)) {
          if(highestInPartyEXP > 80000) {
            maxedSwapEXP = true;
          }
        }

        if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
          if(highestInPartyEXP > 160000) {
            maxedSwapEXP = true;
          }
        }

        if(!maxedSwapEXP) {
          for(int i = 0; i < 9; i++) {
            if(gameState_800babc8.charData_32c[i].partyFlags_04 > 0) {
              while (highestInPartyEXP > getXpToNextLevel(i)) {
                gameState_800babc8.charData_32c[i].level_12++;
              }
            }
          }
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_WEST)) {
        if(!this.swappedEXP) {
          this.swappedEXP = true;
          this.print("[DRAGOON MODIFIER] Preparing Switch EXP...");
          System.arraycopy(gameState_800babc8.charIds_88, 0, this.swapEXPParty, 0, 3);
        } else {
          this.swappedEXP = false;
          int slot1 = -1;
          int slot2 = -1;
          for(int i = 0; i < 3; i++) {
            if(this.swapEXPParty[i] != gameState_800babc8.charIds_88[i]) {
              slot1 = i;
            }
          }

          for(int i = 0; i < 3; i++) {
            if(this.swapEXPParty[slot1] == gameState_800babc8.charIds_88[i]) {
              slot2 = i;
              final int char1 = gameState_800babc8.charIds_88[slot1];
              final int char2 = gameState_800babc8.charIds_88[slot2];
              final int slot1EXP = gameState_800babc8.charData_32c[char1].xp_00;
              final int slot2EXP = gameState_800babc8.charData_32c[char2].xp_00;
              boolean disableSwap = false;

              if("Hard Mode".equals(difficulty) || "Us + Hard Bosses".equals(difficulty)) {
                if(slot1EXP > 80000 || slot2EXP > 80000) {
                  disableSwap = true;
                }
              }

              if("Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
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
            System.out.println("[DRAGOON MODIFIER] EXP Switched.");
          } else {
            System.out.println("[DRAGOON MODIFIER] Switch EXP character removed from party.");
          }
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_EAST) && this.hotkey.contains(InputAction.BUTTON_CENTER_2)) {
        if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
          final int mapId = submapCut_80052c30;
          if(mapId >= 393 && mapId <= 405) {
            if(gameState_800babc8.chapterIndex_98 == 3) {
              final int ultimateBossesDefeated = Integer.parseInt(GameEngine.CONFIG.getConfig(ULTIMATE_BOSS_DEFEATED.get()));
              int ultimateBossSelected = GameEngine.CONFIG.getConfig(ULTIMATE_BOSS.get()) - 1;

              if(mapId >= 393 && mapId <= 394) {
                if(ultimateBossSelected > 2 && ultimateBossesDefeated > 2) {
                  ultimateBossSelected = 2;
                } else {
                  if(ultimateBossSelected > ultimateBossesDefeated) {
                    ultimateBossSelected = ultimateBossesDefeated;
                  }
                }
                this.ultimateLevelCap = 30;
              } else if(mapId >= 395 && mapId <= 397) {
                if(ultimateBossSelected > 7 && ultimateBossesDefeated > 7) {
                  ultimateBossSelected = 7;
                } else {
                  if(ultimateBossSelected > ultimateBossesDefeated) {
                    ultimateBossSelected = ultimateBossesDefeated;
                  }
                }
                this.ultimateLevelCap = 40;
              } else if(mapId >= 398 && mapId <= 400) {
                if(ultimateBossSelected > 21 && ultimateBossesDefeated > 21) {
                  ultimateBossSelected = 21;
                } else {
                  if(ultimateBossSelected > ultimateBossesDefeated) {
                    ultimateBossSelected = ultimateBossesDefeated;
                  }
                }
                this.ultimateLevelCap = 50;
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
      } else if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_1)) {
        if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
          if(gameState_800babc8.chapterIndex_98 >= 1) {
            Scus94491BpeSegment_8007.shopId_8007a3b4 = 42;
            Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
            Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
          }
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_1)) {
        if("Hard Mode".equals(difficulty) || "US + Hard Bosses".equals(difficulty) || "Hell Mode".equals(difficulty) || "Hard + Hell Bosses".equals(difficulty)) {
          if(gameState_800babc8.chapterIndex_98 >= 1) {
            Scus94491BpeSegment_8007.shopId_8007a3b4 = 43;
            Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
            Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
          }
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_RIGHT_2)) {
        if(gameState_800babc8.chapterIndex_98 >= 1) {
          Scus94491BpeSegment_8007.shopId_8007a3b4 = 40;
          Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
          Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
        }
      } else if(this.hotkey.contains(InputAction.BUTTON_NORTH) && this.hotkey.contains(InputAction.BUTTON_SHOULDER_LEFT_2)) {
        Scus94491BpeSegment_8007.shopId_8007a3b4 = 41;
        Scus94491BpeSegment_800b.whichMenu_800bdc38 = WhichMenu.INIT_SHOP_MENU_6;
        Scus94491BpeSegment_800b.inventoryMenuState_800bdc28 = InventoryMenuState._9;
      }
    }
  }
  //endregion
}
