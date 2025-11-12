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

/**
 * Basic power model for Element 1.
 * Models solar array charging, flight computer power drain, and battery charge.
 */
public class PowerModel {

    public MutableResource<Discrete<Double>> SolarArrayChargingRate; // Wh 

    public MutableResource<Discrete<Double>> FlightComputerDrainRate; // Wh

    public MutableResource<Discrete<Double>> BatteryCharge; // Wh

    public static final Double SOLAR_ARRAY_CHARGE_RATE = 200.0; // Wh
    public static final Double INITIAL_BATTERY_CHARGE = 2000.0; // Wh
    public static final Double BATTERY_CAPACITY = 2000.0; // Wh
    public static final Double FLIGHT_COMPUTER_DRAIN_RATE = 100.0; // Wh

    public PowerModel(Registrar registrar, Configuration config)
    {
        SolarArrayChargingRate = resource(discrete(SOLAR_ARRAY_CHARGE_RATE)); // Solar array charging rate while in sunlight
        BatteryCharge = resource(discrete(INITIAL_BATTERY_CHARGE)); // Initial battery charge
        FlightComputerDrainRate = resource(discrete(FLIGHT_COMPUTER_DRAIN_RATE)); // Default drain rate
        registrar.discrete("SolarArrayChargingRate", SolarArrayChargingRate, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("BatteryCharge", BatteryCharge, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("FlightComputerDrainRate", FlightComputerDrainRate, withUnit("Watt hours", new DoubleValueMapper()));
    }

    /*
     * Solar array charging daemon.
     * Incrementally increases battery charge based on solar array charging rate every hour.
     */
    public void solarArrayCharge() {
        Duration SOLAR_ARRAY_CHARGE_INTERVAL = Duration.duration(1, Duration.HOURS);
        while(true)
        {
            delay(SOLAR_ARRAY_CHARGE_INTERVAL);
            Double currentSolarChargeRate = currentValue(SolarArrayChargingRate);
            if (currentValue(BatteryCharge) < BATTERY_CAPACITY) {
                Double chargeAdd = currentSolarChargeRate * SOLAR_ARRAY_CHARGE_INTERVAL.ratioOver(Duration.HOUR);
                Double chargeAddToReachCapacity = BATTERY_CAPACITY - currentValue(BatteryCharge);
                DiscreteEffects.increase(BatteryCharge, Math.min(
                    chargeAdd,
                    chargeAddToReachCapacity));
            }
        }
    }

    /*
     * Flight computer power drain daemon.
     * Incrementally decreases battery charge based on flight computer drain rate every hour.
     */
    public void flightComputerDrain() {
        Duration DRAIN_INTERVAL = Duration.duration(1, Duration.HOURS);
        while(true) {
            delay(DRAIN_INTERVAL);
            DiscreteEffects.decrease(BatteryCharge, currentValue(FlightComputerDrainRate) *
                DRAIN_INTERVAL.ratioOver(Duration.HOURS));
        }
    }
}
