package missionmodel;

import static gov.nasa.jpl.aerie.contrib.metadata.UnitRegistrar.withUnit;
import static gov.nasa.jpl.aerie.contrib.streamline.core.MutableResource.resource;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.Discrete.discrete;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.monads.DiscreteResourceMonad.map;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.asPolynomial;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.clampedIntegrate;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.constant;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.assumeLinear;

import gov.nasa.jpl.aerie.contrib.serialization.mappers.DoubleValueMapper;
import gov.nasa.jpl.aerie.contrib.streamline.core.MutableResource;
import gov.nasa.jpl.aerie.contrib.streamline.core.Resource;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.Registrar;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.Discrete;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.Polynomial;

/**
 * Basic power model for Element 1.
 * Models solar array charging, flight computer power drain, and battery charge.
 */
public class PowerModel {

    public MutableResource<Discrete<Double>> solarArrayChargingRate; // Wh 

    public MutableResource<Discrete<Double>> flightComputerDrainRate; // Wh

    public Resource<Polynomial> batteryCharge; // Wh

    public static final Double SOLAR_ARRAY_CHARGE_RATE = 200.0; // Wh
    public static final Double INITIAL_BATTERY_CHARGE = 2000.0; // Wh
    public static final Double BATTERY_CAPACITY = 2000.0; // Wh
    public static final Double FLIGHT_COMPUTER_DRAIN_RATE = 100.0; // Wh

    public PowerModel(Registrar registrar, Configuration config)
    {
        solarArrayChargingRate = resource(discrete(SOLAR_ARRAY_CHARGE_RATE)); // Solar array charging rate while in sunlight
        flightComputerDrainRate = resource(discrete(FLIGHT_COMPUTER_DRAIN_RATE)); // Default drain rate

        Resource<Discrete<Double>> batteryChargeRate = map(solarArrayChargingRate, flightComputerDrainRate,
          (Double source$, Double sink$) -> {
            return source$ - sink$;
          });

        // model battery charge as integral of charge rate, with min 0 and max BATTERY_CAPACITY
        var result = clampedIntegrate(
                asPolynomial(batteryChargeRate),
                constant(0),
                constant(BATTERY_CAPACITY),
                INITIAL_BATTERY_CHARGE);
        batteryCharge = result.integral();

        registrar.discrete("SolarArrayChargingRate", solarArrayChargingRate, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("FlightComputerDrainRate", flightComputerDrainRate, withUnit("Watt hours", new DoubleValueMapper()));
        registrar.discrete("BatteryChargeRate", batteryChargeRate, withUnit("Watt hours", new DoubleValueMapper()));
        // must assume 1-degree polynomial to register
        registrar.real("BatteryCharge", assumeLinear(batteryCharge));
    }
}
