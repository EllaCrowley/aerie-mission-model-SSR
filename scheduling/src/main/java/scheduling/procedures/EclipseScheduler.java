package scheduling.procedures;

import java.util.List;
import java.util.Map;

import gov.nasa.ammos.aerie.procedural.scheduling.Goal;
import gov.nasa.ammos.aerie.procedural.scheduling.annotations.SchedulingProcedure;
import gov.nasa.ammos.aerie.procedural.scheduling.plan.EditablePlan;
import gov.nasa.ammos.aerie.procedural.scheduling.plan.NewDirective;
import gov.nasa.ammos.aerie.procedural.timeline.collections.Instances;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.ExternalEvent;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.AnyDirective;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.AnyInstance;
import gov.nasa.ammos.aerie.procedural.timeline.payloads.activities.DirectiveStart;
import gov.nasa.ammos.aerie.procedural.timeline.plan.EventQuery;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import missionmodel.Utils;

/**
 * Scheduling procedure to create Eclipse activities based on EclipseWindow external events.
 * For the duration of each EclipseWindow event, an Eclipse activity is created if one does not already exist.
 * During an Eclipse activity, the solar array charging rate is set to zero.
 */
@SchedulingProcedure
public record EclipseScheduler() implements Goal {

  boolean areThereSpansForType(Instances<AnyInstance> spans, Duration startTime, String activityType) {
    final var filteredByType = spans.filter(false, it -> it.getType().equals(activityType));
    final var filteredByTime = filteredByType.filter(false, it -> it.getStartTime().equals(startTime));
    return !filteredByTime.collect().isEmpty();
  }

  @Override
  public void run(EditablePlan plan) {

    final var eclipseEventTypeName = "EclipseWindow";
    final var eclipseSourceTypeName = Utils.getEclipseActivityName();

    EventQuery eclipseExternalEvents = new EventQuery(null, List.of(eclipseEventTypeName), null);
    List<ExternalEvent> eclipseEvents = plan.events(eclipseExternalEvents).collect();

    final var simResults = plan.simulate();
    final var existingSpans = simResults.instances();

    for (var currentEvent : eclipseEvents) {
      if (!areThereSpansForType(existingSpans, currentEvent.getInterval().start, eclipseSourceTypeName)) {
        final var newActivityName = currentEvent.key + " Activity";
        final var eclipseDurationMinutes = currentEvent.attributes.get("durationMinutes").asInt().get();
        plan.create(
          new NewDirective(
            new AnyDirective(Map.of(
              "durationMinutes", SerializedValue.of(eclipseDurationMinutes)
            )),
            newActivityName,
            eclipseSourceTypeName,
            new DirectiveStart.Absolute(currentEvent.getInterval().start)
          )
        );
      }
    }
    plan.commit();
  }
}
