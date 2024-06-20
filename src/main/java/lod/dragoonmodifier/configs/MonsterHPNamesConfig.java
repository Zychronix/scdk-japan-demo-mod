package lod.dragoonmodifier.configs;

import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigStorageLocation;
import legend.game.saves.EnumConfigEntry;
import lod.dragoonmodifier.values.MonsterHPNames;

public class MonsterHPNamesConfig extends EnumConfigEntry<MonsterHPNames> {
  public MonsterHPNamesConfig() {
    super(MonsterHPNames.class, MonsterHPNames.OFF, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
  }
}
