package constraints.procedures;

import gov.nasa.ammos.aerie.procedural.constraints.Constraint;
import gov.nasa.ammos.aerie.procedural.constraints.Violations;
import gov.nasa.ammos.aerie.procedural.constraints.annotations.ConstraintProcedure;
import gov.nasa.ammos.aerie.procedural.timeline.plan.Plan;
import gov.nasa.ammos.aerie.procedural.timeline.plan.SimulationResults;
import missionmodel.Utils;

/*
 * Constraint procedure to ensure that GroundContact activities do not overlap in time.
 */
@ConstraintProcedure
public record GroundContactOverlap() implements Constraint {
  @Override
  public Violations run(Plan plan, SimulationResults simResults) {
    return Violations.on(
      simResults.instances(Utils.getGroundContactActivityName()).countActive().greaterThan(1),
      true);
  }
}
