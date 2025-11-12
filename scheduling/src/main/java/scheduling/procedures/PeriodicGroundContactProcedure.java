package scheduling.procedures;

import gov.nasa.ammos.aerie.procedural.scheduling.Goal;
import gov.nasa.ammos.aerie.procedural.scheduling.plan.EditablePlan;
import gov.nasa.ammos.aerie.procedural.scheduling.annotations.SchedulingProcedure;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.Directive;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.AnyDirective;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.DirectiveStart;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

import missionmodel.Utils;

import java.util.Map;
import java.util.List;

/*
 * Scheduling procedure to create periodic GroundContact activities every 5 hours, 
 * starting at hour 1, and lasting for 10 minutes each.
 */
@SchedulingProcedure
public record PeriodicGroundContactProcedure() implements Goal {

  boolean overlap(List<Directive<AnyDirective>> existingContacts, Duration proposedStart, Duration proposedDuration) {
    for (var contact : existingContacts) {
      var contactStart = contact.getInterval().start;
      var contactEnd = contactStart.plus(proposedDuration);
      var proposedEnd = proposedStart.plus(proposedDuration);
      
      if (proposedStart.shorterThan(contactEnd) && proposedEnd.longerThan(contactStart)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void run(EditablePlan plan) {

    final var firstOccurrenceTime = Duration.hours(1);
    final var step = Duration.hours(5);

    var existingGroundContacts = plan.directives(Utils.getGroundContactActivityName()).collect();
    final var groundContactDuration = Duration.minutes(10);

    var currentTime = firstOccurrenceTime;
    while (currentTime.shorterThan(plan.duration())) {
      if (!overlap(existingGroundContacts, currentTime, groundContactDuration))
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
