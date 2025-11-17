package missionmodel;

import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.Quantities.quantity;
import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.StandardUnits.WATT;
import static gov.nasa.jpl.aerie.merlin.framework.ModelActions.delay;

import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.DiscreteEffects;
import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType;
import gov.nasa.jpl.aerie.merlin.framework.annotations.Export.Parameter;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

/*
 * Basic eclipse activity, which drops solar array charging rate to zero.
 */
@ActivityType("Eclipse")
public class Eclipse {

    public static final String ECLIPSE_TYPE_NAME = "Eclipse";

    @Parameter
    public Integer durationMinutes = 30;

    @ActivityType.EffectModel
    public void run(Mission model) {

        /*
         Drop solar array charging rate to zero during eclipse
        */  
        DiscreteEffects.set(model.powerModel.solarArrayChargingRate, quantity(0.0, WATT));
        delay(Duration.minutes(durationMinutes));
        DiscreteEffects.set(model.powerModel.solarArrayChargingRate, quantity(missionmodel.PowerModel.SOLAR_ARRAY_CHARGE_RATE, WATT));

    }
}

