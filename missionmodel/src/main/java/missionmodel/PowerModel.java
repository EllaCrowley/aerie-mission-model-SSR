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

    public MutableResource<Discrete<Double>> solarArrayChargingRate; // W
    public MutableResource<Discrete<Double>> flightComputerDrainRate; // W
    public MutableResource<Discrete<Double>> batteryCharge; // Wh

    public static final Double SOLAR_ARRAY_CHARGE_RATE = 200.0; // W
    public static final Double FLIGHT_COMPUTER_DRAIN_RATE = 100.0; // W

    public static final Double INITIAL_BATTERY_CHARGE = 2000.0; // Wh
    public static final Double BATTERY_CAPACITY = 2000.0; // Wh

    public PowerModel(Registrar registrar, Configuration config)
    {
        solarArrayChargingRate = resource(discrete(SOLAR_ARRAY_CHARGE_RATE)); // Solar array charging rate while in sunlight
        batteryCharge = resource(discrete(INITIAL_BATTERY_CHARGE)); // Initial battery charge
        flightComputerDrainRate = resource(discrete(FLIGHT_COMPUTER_DRAIN_RATE)); // Default drain rate

        registrar.discrete("SolarArrayChargingRate", solarArrayChargingRate, withUnit("Watts", new DoubleValueMapper()));
        registrar.discrete("BatteryCharge", batteryCharge, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("FlightComputerDrainRate", flightComputerDrainRate, withUnit("Watts", new DoubleValueMapper()));
    }

    /*
     * Solar array charging daemon.
     * Increments battery charge based on solar array charging rate.
     */
    public void solarArrayCharge() {
        Duration SOLAR_ARRAY_CHARGE_INTERVAL = Duration.duration(1, Duration.MINUTES);
        while(true) {
            delay(SOLAR_ARRAY_CHARGE_INTERVAL);
            Double currentSolarChargeRate = currentValue(solarArrayChargingRate);
            if (currentValue(batteryCharge) < BATTERY_CAPACITY) {
                DiscreteEffects.increase(batteryCharge, Math.min(
                    currentSolarChargeRate * SOLAR_ARRAY_CHARGE_INTERVAL.ratioOver(Duration.HOUR),
                    BATTERY_CAPACITY - currentValue(batteryCharge)));
            }
        }
    }

    /*
     * Flight computer power drain daemon.
     * Decrements battery charge based on flight computer drain rate.
     */
    public void flightComputerDrain() {
        Duration DRAIN_INTERVAL = Duration.duration(1, Duration.MINUTES);
        while(true) {
            delay(DRAIN_INTERVAL);
            Double currentDrainRate = currentValue(flightComputerDrainRate);
            if (currentValue(batteryCharge) < BATTERY_CAPACITY) {
                // Charge rate units in watts, battery charge in watt hours
                DiscreteEffects.decrease(batteryCharge, Math.min(
                    currentDrainRate * DRAIN_INTERVAL.ratioOver(Duration.HOUR),
                    currentValue(batteryCharge)));
            }
        }
    }
}
