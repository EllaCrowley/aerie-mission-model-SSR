package scheduling.procedures;

import gov.nasa.ammos.aerie.procedural.scheduling.Goal;
import gov.nasa.ammos.aerie.procedural.scheduling.plan.EditablePlan;
import gov.nasa.ammos.aerie.procedural.scheduling.annotations.SchedulingProcedure;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.DirectiveStart;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

import missionmodel.Utils;

import java.util.Map;

/*
 * Scheduling procedure to create periodic GroundContact activities every 5 hours, 
 * starting at hour 1, and lasting for 10 minutes each.
 */
@SchedulingProcedure
public record PeriodicGroundContactProcedure() implements Goal {
  @Override
  public void run(EditablePlan plan) {
    final var firstTime = Duration.hours(1);
    final var step = Duration.hours(5);

    var currentTime = firstTime;
    while (currentTime.shorterThan(plan.duration())) {
      plan.create(
        Utils.getGroundContactActivityName(),
        new DirectiveStart.Absolute(currentTime),
        Map.of()
      );
      currentTime = currentTime.plus(step);
    }
    plan.commit();
  }
}
