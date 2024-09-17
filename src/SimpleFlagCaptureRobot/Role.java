package SimpleFlagCaptureRobot;

public enum Role {

  GATHERER(1),
  BATTLE(2),
  SEEKER(3),
  FLAG_CARRIER(4),
  FLAG_PROTECTOR(5);

  private int index;

  Role(int ind) {
    this.index = ind;
  }

  public int getIndex() {
    return index;
  }

}
