package lod.dragoonmodifier.configs;

import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigStorageLocation;
import legend.game.saves.EnumConfigEntry;
import lod.dragoonmodifier.values.DamageTracker;

public class DamageTrackerConfig extends EnumConfigEntry<DamageTracker> {
  public DamageTrackerConfig() {
    super(DamageTracker.class, DamageTracker.OFF, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
  }
}
