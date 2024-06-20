package lod.dragoonmodifier.configs;

import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigStorageLocation;
import legend.game.saves.EnumConfigEntry;
import lod.dragoonmodifier.values.ElementalBomb;

public class ElementalBombConfig extends EnumConfigEntry<ElementalBomb> {
  public ElementalBombConfig() {
    super(ElementalBomb.class, ElementalBomb.OFF, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY);
  }
}
