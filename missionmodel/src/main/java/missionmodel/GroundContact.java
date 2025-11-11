package missionmodel;

import static gov.nasa.jpl.aerie.contrib.streamline.core.Resources.currentValue;
import static gov.nasa.jpl.aerie.merlin.framework.ModelActions.delay;

import gov.nasa.jpl.aerie.contrib.metadata.Unit;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.DiscreteEffects;
import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType;
import gov.nasa.jpl.aerie.merlin.framework.annotations.Export.Parameter;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

@ActivityType("GroundContact")
public class GroundContact {

    @Parameter
    @Unit("Watt hours")
    public Double contactPowerDrain = 500.0; // Watt hours; power drain during ground contact

    @Parameter
    public Duration duration = Duration.duration(10, Duration.MINUTES);

    @ActivityType.EffectModel
    public void run(Mission model) {

        /*
         Increase power drain during ground contact
        */
        Double initialFlightComputerDrainRate = currentValue(model.powerModel.FlightComputerDrainRate);
        DiscreteEffects.set(model.powerModel.FlightComputerDrainRate, contactPowerDrain);
        delay(duration);
        DiscreteEffects.set(model.powerModel.FlightComputerDrainRate, initialFlightComputerDrainRate);

    }
}

