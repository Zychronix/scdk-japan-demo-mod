package lod.dragoonmodifier.items;

import legend.game.combat.Battle;
import legend.game.combat.deff.DeffPackage;
import legend.game.unpacker.Unpacker;

import static legend.game.Scus94491BpeSegment_8004.currentEngineState_8004dd04;

public class DraModItemDeffPackage extends DeffPackage {
  private final String location;

  public DraModItemDeffPackage(final String location) {
    this.location = location;
  }

  @Override
  public void load() {
    ((Battle)currentEngineState_8004dd04).loadDeff(
      Unpacker.resolveMods("dragoon_modifier/scripts/items/" + this.location + '0'),
      Unpacker.resolveMods("dragoon_modifier/scripts/items/" + this.location + '1')
    );
  }
}
