package lod.dragoonmodifier.configs;

import legend.core.IoHelper;
import legend.game.inventory.screens.controls.Label;
import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigCollection;
import legend.game.saves.ConfigEntry;
import legend.game.saves.ConfigStorageLocation;

import static legend.core.GameEngine.CONFIG;

public class UltimateBossDefeatedConfig extends ConfigEntry<String> {
  public UltimateBossDefeatedConfig() {
    super(
      "0",
      ConfigStorageLocation.SAVE,
      ConfigCategory.GAMEPLAY,
      str -> IoHelper.stringToBytes(str, 4),
      bytes -> IoHelper.stringFromBytes(bytes, 4, "0")
    );

    this.setEditControl((current, gameState) -> {
      return new Label(CONFIG.getConfig(this).toString());
    });
  }

  @Override
  public void onChange(final ConfigCollection configCollection, final String oldValue, final String newValue) {
    super.onChange(configCollection, oldValue, newValue);
  }
}
