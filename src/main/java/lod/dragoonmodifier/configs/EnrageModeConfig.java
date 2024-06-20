package lod.dragoonmodifier.configs;

import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigStorageLocation;
import legend.game.saves.EnumConfigEntry;
import lod.dragoonmodifier.values.EnrageMode;

public class EnrageModeConfig extends EnumConfigEntry<EnrageMode> {
  public EnrageModeConfig() {
    super(EnrageMode.class, EnrageMode.OFF, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
  }
}
