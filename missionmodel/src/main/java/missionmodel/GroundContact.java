package missionmodel;

import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.Quantities.quantity;
import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.StandardUnits.WATT;
import static gov.nasa.jpl.aerie.merlin.framework.ModelActions.delay;

import gov.nasa.jpl.aerie.contrib.metadata.Unit;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.DiscreteEffects;
import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType;
import gov.nasa.jpl.aerie.merlin.framework.annotations.Export.Parameter;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

/*
 * Basic ground contact activity, which increases flight computer power drain..
 */
@ActivityType("GroundContact")
public class GroundContact {

    public static final String GROUND_CONTACT_TYPE_NAME = "GroundContact";

    @Parameter
    @Unit("Watt hours")
    public Double contactPowerDrain = -500.0; // Watt hours; power drain during ground contact

    @Parameter
    public Duration duration = Duration.duration(10, Duration.MINUTES);

    @ActivityType.EffectModel
    public void run(Mission model) {

        /*
         Increase power drain during ground contact
        */
        DiscreteEffects.set(model.powerModel.flightComputerDrainRate, quantity(contactPowerDrain, WATT));
        delay(duration);
        DiscreteEffects.set(model.powerModel.flightComputerDrainRate, quantity(missionmodel.PowerModel.FLIGHT_COMPUTER_DRAIN_RATE, WATT));

    }
}

