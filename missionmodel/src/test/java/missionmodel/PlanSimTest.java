package missionmodel;

import gov.nasa.ammos.aerie.procedural.scheduling.plan.EditablePlan;
import gov.nasa.ammos.aerie.procedural.scheduling.utils.DefaultEditablePlanDriver;
import gov.nasa.ammos.aerie.procedural.timeline.collections.profiles.Real;
import gov.nasa.ammos.aerie.procedural.timeline.collections.profiles.Numbers;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.DirectiveStart;
import gov.nasa.ammos.aerie.procedural.timeline.plan.SimulationResults;
import gov.nasa.ammos.aerie.procedural.utils.TypeUtilsEditablePlanAdapter;
import gov.nasa.ammos.aerie.procedural.utils.TypeUtilsPlanAdapter;
import gov.nasa.jpl.aerie.contrib.serialization.mappers.DurationValueMapper;
import gov.nasa.jpl.aerie.contrib.serialization.mappers.EnumValueMapper;
import gov.nasa.jpl.aerie.merlin.driver.MissionModel;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.orchestration.simulation.SimulationUtility;
import gov.nasa.jpl.aerie.types.Plan;
import gov.nasa.jpl.aerie.types.Timestamp;
import missionmodel.generated.GeneratedModelType;

import org.apache.commons.math3.analysis.function.Power;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example test for simulating a mission model
 * General workflow:
 * 1. Create a {@link SimulationUtility} instance.
 * 2. Load the mission model using the sim utility.
 * 3. Create a new empty plan. You'll need to use a couple adapters, see SchedulingProcedureTests.beforeEach.
 *    for an example.
 * 4. Add activities to the plan as desired
 * 5. Look at resultant simulation results and check with assertions
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlanSimTest {

  private MissionModel<?> model;
  private SimulationUtility simUtility;
  private Instant simulationStartTime;
  private EditablePlan plan;

  /* Setup and instantiate the mission model. */
  @BeforeAll
  void beforeAll() {
    // Note no resource file streamer is required since we don't plan to write out resources to a file
    simUtility = new SimulationUtility();
    Configuration simConfig = Configuration.defaultConfiguration();
    simulationStartTime = Instant.parse("2026-08-01T00:00:00Z");
    model = SimulationUtility.instantiateMissionModel(new GeneratedModelType(), simulationStartTime, simConfig);

  }

  /* Close out . */
  @AfterAll
  void afterAll() {
    simUtility.close();
  }

  @BeforeEach
  void beforeEach() {
    plan = new DefaultEditablePlanDriver(
      new TypeUtilsEditablePlanAdapter(
        new TypeUtilsPlanAdapter(
          new Plan("TestPlan", new Timestamp(simulationStartTime), new Timestamp(simulationStartTime.plusSeconds(60 * 60 * 72)), Map.of(), Map.of())
        ),
        simUtility,
        model
      )
    );
  }

  @Test
  final void testSchedulePeriodicDataCollections() {

    // Create three activities. Two GroundContact activities and a CollectData in between
    plan.create("GroundContact", new DirectiveStart.Absolute(Duration.hours(2)), Map.of());
    plan.create("GroundContact", new DirectiveStart.Absolute(Duration.hours(12)), Map.of());

    // Simulate Plan
    SimulationResults simResults = plan.simulate();

    // Perform assertions
    Numbers modeProfile = simResults.resource("BatteryCharge", Numbers.deserializer());
    assertEquals(PowerModel.INITIAL_BATTERY_CHARGE, Double.valueOf(String.valueOf(modeProfile.sample(Duration.hours(0.5)))));

    Numbers rateProfile = simResults.resource("SolarArrayChargingRate", Numbers.deserializer());
    assertEquals(PowerModel.INITIAL_SOLAR_ARRAY_CHARGE_RATE, Double.valueOf(String.valueOf(rateProfile.sample(Duration.hours(0.5)))));

  }

}