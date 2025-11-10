package missionmodel;

import static gov.nasa.jpl.aerie.contrib.metadata.UnitRegistrar.withUnit;
import static gov.nasa.jpl.aerie.contrib.streamline.core.MutableResource.resource;
import static gov.nasa.jpl.aerie.contrib.streamline.core.Resources.currentValue;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.Discrete.discrete;
import static gov.nasa.jpl.aerie.merlin.framework.ModelActions.delay;

import gov.nasa.jpl.aerie.contrib.serialization.mappers.DoubleValueMapper;
import gov.nasa.jpl.aerie.contrib.streamline.core.MutableResource;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.Registrar;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.Discrete;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.DiscreteEffects;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

public class PowerModel {

    public MutableResource<Discrete<Double>> SolarArrayChargingRate; // Wh 

    public MutableResource<Discrete<Double>> FlightComputerDrainRate; // Wh

    public PowerModel(Registrar registrar, Configuration config)
    {
        SolarArrayChargingRate = resource(discrete(200.0)); // Solar array charging rate while in sunlight
        BatteryCharge = resource(discrete(2000.0)); // Initial battery charge
        FlightComputerDrainRate = resource(discrete(100.0)); // Default drain rate
        registrar.discrete("SolarArrayChargingRate", SolarArrayChargingRate, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("BatteryCharge", BatteryCharge, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("FlightComputerDrainRate", FlightComputerDrainRate, withUnit("Watt hours", new DoubleValueMapper()));
    }

    public void solarArrayCharge() {
        Duration SOLAR_ARRAY_CHARGE_INTERVAL = Duration.duration(1, Duration.HOURS);
        while(true)
        {
            delay(SOLAR_ARRAY_CHARGE_INTERVAL);
            Double currentSolarChargeRate = currentValue(SolarArrayChargingRate);
            DiscreteEffects.increase(BatteryCharge, 
                currentSolarChargeRate * SOLAR_ARRAY_CHARGE_INTERVAL.ratioOver(Duration.HOUR));   
        }
    }
    
    public void flightComputerDrain() {
        Duration DRAIN_INTERVAL = Duration.duration(1, Duration.HOURS);
        while(true) {
            delay(DRAIN_INTERVAL);
            DiscreteEffects.decrease(BatteryCharge, currentValue(FlightComputerDrainRate) *
                DRAIN_INTERVAL.ratioOver(Duration.HOURS));
        }
    }

    public void solarArrayChargingCycle() {
        Duration eclipseDuration = Duration.duration(30, Duration.MINUTES);
        Duration orbitalPeriod = Duration.duration(100, Duration.MINUTES);
        Double initialSolarArrayChargeRate = currentValue(SolarArrayChargingRate);
        
        while(true) {
            DiscreteEffects.set(SolarArrayChargingRate, initialSolarArrayChargeRate);
            delay(orbitalPeriod.minus(eclipseDuration));
            DiscreteEffects.set(SolarArrayChargingRate, 0.0);
            delay(eclipseDuration);
        }
    }
    // TODO: model eclipse as an activity that occurs for a duration of 20min every 100min orbital period
}
