package lod.dragoonmodifier.values;

public enum DamageTracker {
  OFF("Off"),
  ON("On"),
  ;

  public final String name;

  DamageTracker(final String name) {
    this.name = name;
  }
}