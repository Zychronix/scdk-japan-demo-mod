package lod.dragoonmodifier.configs;

import legend.core.IoHelper;
import legend.core.MathHelper;
import legend.game.inventory.screens.controls.NumberSpinner;
import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigEntry;
import legend.game.saves.ConfigStorageLocation;

public class HellFlowerStormConfig extends ConfigEntry<Integer> {
  public HellFlowerStormConfig() {
    super(5, ConfigStorageLocation.CAMPAIGN, ConfigCategory.GAMEPLAY, HellFlowerStormConfig::serializer, HellFlowerStormConfig::deserializer);

    this.setEditControl((number, gameState) -> {
      final NumberSpinner<Integer> spinner = NumberSpinner.intSpinner(number, 1, 5);
      spinner.onChange(val -> gameState.setConfig(this, val));
      return spinner;
    });
  }

  private static byte[] serializer(final int val) {
    final byte[] data = new byte[4];
    MathHelper.set(data, 0, 4, val);
    return data;
  }

  private static int deserializer(final byte[] data) {
    if(data.length == 4) {
      return IoHelper.readInt(data, 0);
    }

    return 32;
  }
}
