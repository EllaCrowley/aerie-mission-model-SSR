package missionmodel;

import static gov.nasa.jpl.aerie.contrib.streamline.core.Resources.currentValue;
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

    @Parameter
    public int durationMinutes;

    @ActivityType.EffectModel
    public void run(Mission model) {

        /*
         Drop solar array charging rate to zero during eclipse
        */
        Double initialSolarArrayChargingRate = currentValue(model.powerModel.SolarArrayChargingRate);
        DiscreteEffects.set(model.powerModel.SolarArrayChargingRate, 0.0);
        delay(Duration.minutes(durationMinutes));
        DiscreteEffects.set(model.powerModel.SolarArrayChargingRate, initialSolarArrayChargingRate);

    }
}

