package lod.dragoonmodifier.items;

import legend.game.combat.bent.BattleEntity27c;
import legend.game.inventory.Item;
import legend.game.scripting.ScriptFile;
import legend.game.scripting.ScriptStackFrame;
import legend.game.scripting.ScriptState;
import legend.game.unpacker.Unpacker;
import legend.lodmod.items.ShieldItem;

import java.nio.file.Path;

public class DraModShieldItem extends ShieldItem {
  final String location;

  public DraModShieldItem(final int icon, final int price, final int useItemEntrypoint, final boolean physicalImmunity, final boolean magicalImmunity, final String location) {
    super(icon, price, useItemEntrypoint, physicalImmunity, magicalImmunity);
    this.location = location;
  }

  @Override
  protected void injectScript(final ScriptState<? extends BattleEntity27c> user, final Path path, final int entrypoint, final Runnable onLoad) {
    Unpacker.loadFile(Unpacker.resolveMods("dragoon_modifier/scripts/items/" + this.location), data -> {
      final ScriptFile file = new ScriptFile(this.location, data.getBytes());
      user.pushFrame(new ScriptStackFrame(file, file.getEntry(entrypoint)));
      user.context.commandOffset_0c = user.frame().offset;
      onLoad.run();
    });
  }
}
