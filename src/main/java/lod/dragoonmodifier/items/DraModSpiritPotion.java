package lod.dragoonmodifier.items;

import legend.game.combat.bent.BattleEntity27c;
import legend.game.scripting.ScriptFile;
import legend.game.scripting.ScriptStackFrame;
import legend.game.scripting.ScriptState;
import legend.game.unpacker.Unpacker;
import legend.lodmod.items.SpiritPotionItem;

import java.nio.file.Path;

public class DraModSpiritPotion extends SpiritPotionItem {
  final String location;

  public DraModSpiritPotion(final int icon, final int price, final boolean targetAll, final int percentage, final String location) {
    super(icon, price, targetAll, percentage);
    this.location = location;
  }

  @Override
  public boolean canBeUsed(final UsageLocation location) {
    return location == UsageLocation.BATTLE;
  }

  @Override
  protected int getUseItemScriptEntrypoint() {
    return 2;
  }

  @Override
  protected void useItemScriptLoaded(final ScriptState<BattleEntity27c> user, final int targetBentIndex) {
    user.storage_44[8] = 0x00ff00; // Colour
    user.storage_44[28] = targetBentIndex;
    user.storage_44[30] = user.index;
  }
}
