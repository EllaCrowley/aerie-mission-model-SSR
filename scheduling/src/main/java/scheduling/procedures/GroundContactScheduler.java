package scheduling.procedures;

import gov.nasa.ammos.aerie.procedural.scheduling.Goal;
import gov.nasa.ammos.aerie.procedural.scheduling.plan.EditablePlan;
import gov.nasa.ammos.aerie.procedural.scheduling.plan.NewDirective;
import gov.nasa.ammos.aerie.procedural.scheduling.annotations.SchedulingProcedure;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.Directive;
import gov.nasa.ammos.aerie.procedural.timeline.collections.Instances;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.ExternalEvent;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.AnyDirective;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.AnyInstance;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.DirectiveStart;
import gov.nasa.ammos.aerie.procedural.timeline.plan.EventQuery;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import missionmodel.Utils;

import java.util.Map;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

/*
 * Scheduling procedure to create GroundContact activities for
 * each GroundContact external event.
 */
@SchedulingProcedure
public record GroundContactScheduler() implements Goal {

  boolean areThereSpansForType(Instances<AnyInstance> spans, Duration startTime, String activityType) {
    final var filteredByType = spans.filter(false, it -> it.getType().equals(activityType));
    final var filteredByTime = filteredByType.filter(false, it -> it.getStartTime().equals(startTime));
    return !filteredByTime.collect().isEmpty();
  }

  @Override
  public void run(EditablePlan plan) {

    EventQuery eclipseExternalEvents = new EventQuery(null, List.of("GroundContacts"), null);
    List<ExternalEvent> eclipseEvents = plan.events(eclipseExternalEvents).collect();

    final var simResults = plan.simulate();
    final var existingSpans = simResults.instances();
    final var contactActivityType = Utils.getGroundContactActivityName()

    for (var currentEvent : eclipseEvents) {
      if (!areThereSpansForType(existingSpans, currentEvent.getInterval().start, contactActivityType)) {
        final var newActivityName = currentEvent.key + " Activity";
        plan.create(
          new NewDirective(
            new AnyDirective(Map.of()),
            newActivityName,
            contactActivityType,
            new DirectiveStart.Absolute(currentEvent.getInterval().start)
          )
        );
      }
    }
    plan.commit();
  }
}
