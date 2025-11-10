package missionmodel;

import static gov.nasa.jpl.aerie.contrib.streamline.core.Resources.currentValue;
import static gov.nasa.jpl.aerie.merlin.framework.ModelActions.delay;
import static gov.nasa.jpl.aerie.merlin.framework.ModelActions.spawn;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import gov.nasa.jpl.aerie.merlin.framework.Registrar;
import gov.nasa.jpl.aerie.merlin.framework.junit.MerlinExtension;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

@ExtendWith(MerlinExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MissionResourceTest {

  private final Mission mission;

  public MissionResourceTest(Registrar registrar) {
    this.mission = new Mission(registrar, Configuration.defaultConfiguration());
  }

  @Test
  public void simpleBatteryPowerCheck() {
    assertEquals(currentValue(mission.powerModel.BatteryCharge), 2000.0);
    delay(10, Duration.SECONDS);
  }
}
