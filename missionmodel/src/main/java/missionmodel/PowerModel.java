package missionmodel;

import static gov.nasa.jpl.aerie.contrib.metadata.UnitRegistrar.withUnit;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.DiscreteResources.discreteResource;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.monads.DiscreteResourceMonad.map;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.approximateAsLinear;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.asUnitAwarePolynomial;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.clamp;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.constant;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.integrate;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.lessThan$;
import static gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.PolynomialResources.scale;
import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.Quantities.quantity;
import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.StandardUnits.WATT;

import gov.nasa.jpl.aerie.contrib.serialization.mappers.DoubleValueMapper;
import gov.nasa.jpl.aerie.contrib.streamline.core.MutableResource;
import gov.nasa.jpl.aerie.contrib.streamline.core.Resource;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.Registrar;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.Discrete;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.discrete.DiscreteResources;
import gov.nasa.jpl.aerie.contrib.streamline.modeling.polynomial.Polynomial;
import gov.nasa.jpl.aerie.contrib.streamline.unit_aware.UnitAware;

/**
 * Basic power model for Element 1.
 * Models solar array charging, flight computer power drain, and battery charge.
 */
public class PowerModel {

    public UnitAware<MutableResource<Discrete<Double>>> solarArrayChargingRate; // Watts
    public UnitAware<MutableResource<Discrete<Double>>> flightComputerDrainRate; // Watts
    public UnitAware<Resource<Discrete<Double>>> combinedCharge;

    UnitAware<Resource<Polynomial>> batteryCharge;
    UnitAware<Resource<Polynomial>> clampedBatteryCharge;
    Resource<Discrete<Boolean>> lowPower;

    public static final Double SOLAR_ARRAY_CHARGE_RATE = 200.0; // Watts
    public static final Double FLIGHT_COMPUTER_DRAIN_RATE = -100.0; // Watts

    public static final UnitAware<Double> INITIAL_BATTERY_CHARGE = quantity(2000.0, Utils.WATT_HOUR);
    public static final UnitAware<Double> BATTERY_CAPACITY = quantity(2000.0, Utils.WATT_HOUR);
    public static final UnitAware<Double> LOW_BATTERY_THRESHOLD = quantity(500.0, Utils.WATT_HOUR);

    public static final String BATTERY_CHARGE_RESOURCE_NAME = "Battery Charge";
    public static final String SOLAR_ARRAY_CHARGE_RESOURCE_NAME = "Solar Array Charging Rate";
    public static final String FLIGHT_COMPUTER_DRAIN_RESOURCE_NAME = "Flight Computer Drain Rate";
    public static final String COMBINED_CHARGE_RESOURCE_NAME = "Combined Charge Rate";

    public PowerModel(Registrar registrar, Configuration config)
    {
        solarArrayChargingRate = DiscreteResources.unitAware(discreteResource(SOLAR_ARRAY_CHARGE_RATE), WATT); // Solar array charging rate while in sunlight
        flightComputerDrainRate = DiscreteResources.unitAware(discreteResource(FLIGHT_COMPUTER_DRAIN_RATE), WATT); // Default drain rate
        combinedCharge = DiscreteResources.unitAware(map(solarArrayChargingRate.value(), flightComputerDrainRate.value(),
          (Double solarChargeRate$, Double computerDrainRate$) -> {
            return solarChargeRate$ + computerDrainRate$;
          }), WATT);

        batteryCharge = integrate(asUnitAwarePolynomial(combinedCharge), INITIAL_BATTERY_CHARGE);
        clampedBatteryCharge = clamp(batteryCharge, constant(quantity(0, Utils.WATT_HOUR)), constant(BATTERY_CAPACITY));
        lowPower = lessThan$(batteryCharge, LOW_BATTERY_THRESHOLD);

        // extract non-unit-aware values for registration
        registrar.discrete(SOLAR_ARRAY_CHARGE_RESOURCE_NAME, solarArrayChargingRate.value(), withUnit("Watts", new DoubleValueMapper()));
        registrar.discrete(FLIGHT_COMPUTER_DRAIN_RESOURCE_NAME, flightComputerDrainRate.value(), withUnit("Watts", new DoubleValueMapper()));
        registrar.discrete(COMBINED_CHARGE_RESOURCE_NAME, combinedCharge.value(), withUnit("Watts", new DoubleValueMapper()));

        // divide by 3600 to convert Joules resulting from integral to Watt-hours
        registrar.real(BATTERY_CHARGE_RESOURCE_NAME, approximateAsLinear(scale(clampedBatteryCharge.value(), 1/3600.0)), "Battery state of charge in Watt-hours");


    }
}
