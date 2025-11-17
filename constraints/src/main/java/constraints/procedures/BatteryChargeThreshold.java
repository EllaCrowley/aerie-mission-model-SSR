package constraints.procedures;

import gov.nasa.ammos.aerie.procedural.constraints.Constraint;
import gov.nasa.ammos.aerie.procedural.constraints.annotations.ConstraintProcedure;
import gov.nasa.ammos.aerie.procedural.constraints.Violations;
import gov.nasa.ammos.aerie.procedural.timeline.collections.profiles.Real;
import gov.nasa.ammos.aerie.procedural.timeline.plan.Plan;
import gov.nasa.ammos.aerie.procedural.timeline.plan.SimulationResults;
import missionmodel.Utils;

/*
 * Constraint procedure to ensure that the battery charge remains above a specified threshold.
 */
@ConstraintProcedure
public record BatteryChargeThreshold(Double threshold) implements Constraint {
  @Override
  public Violations run(Plan plan, SimulationResults simResults) {
    final var charge = simResults.resource(Utils.getBatteryChargeResourceName(), Real.deserializer());
    final Double chargeThreshold = this.threshold != null ? this.threshold : missionmodel.PowerModel.LOW_BATTERY_THRESHOLD.value();

    return Violations.on(
      charge.lessThan(chargeThreshold),
      true
    );
  }
}
