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



    public UnitAware<MutableResource<Discrete<Double>>> solarArrayChargingRate; // W
    public UnitAware<MutableResource<Discrete<Double>>> flightComputerDrainRate; // W
    public UnitAware<Resource<Discrete<Double>>> combinedCharge;

    UnitAware<Resource<Polynomial>> batteryCharge;
    UnitAware<Resource<Polynomial>> clampedBatteryCharge;
    Resource<Discrete<Boolean>> lowPower;

    public static final Double SOLAR_ARRAY_CHARGE_RATE = 200.0; // W
    public static final Double FLIGHT_COMPUTER_DRAIN_RATE = -100.0; // W

    public static final UnitAware<Double> INITIAL_BATTERY_CHARGE = quantity(2000.0, Utils.WATT_HOUR); // Wh
    public static final UnitAware<Double> BATTERY_CAPACITY = quantity(2000.0, Utils.WATT_HOUR); // Wh
    public static final UnitAware<Double> LOW_BATTERY_THRESHOLD = quantity(500.0, Utils.WATT_HOUR); // Wh
    public static final String BATTERY_CHARGE_RESOURCE_NAME = "Battery Charge";

    public PowerModel(Registrar registrar, Configuration config)
    {
        solarArrayChargingRate = DiscreteResources.unitAware(discreteResource(SOLAR_ARRAY_CHARGE_RATE), WATT); // Solar array charging rate while in sunlight
        flightComputerDrainRate = DiscreteResources.unitAware(discreteResource(FLIGHT_COMPUTER_DRAIN_RATE), WATT); // Default drain rate
        // combinedCharge = add(solarArrayChargingRate.value(), flightComputerDrainRate.value(),
        //   (Double source$, Double sink$) -> {
        //     return source$ - sink$;
        //   });

        combinedCharge = DiscreteResources.unitAware(map(solarArrayChargingRate.value(), flightComputerDrainRate.value(),
          (Double solarChargeRate$, Double computerDrainRate$) -> {
            return solarChargeRate$ + computerDrainRate$;
          }), WATT);

        batteryCharge = integrate(asUnitAwarePolynomial(combinedCharge), INITIAL_BATTERY_CHARGE);
        clampedBatteryCharge = clamp(batteryCharge, constant(quantity(0, Utils.WATT_HOUR)), constant(BATTERY_CAPACITY));
        lowPower = lessThan$(batteryCharge, LOW_BATTERY_THRESHOLD);

        // extract non-unit-aware values for registration
        registrar.discrete("Solar Array Charging Rate", solarArrayChargingRate.value(), withUnit("Watts", new DoubleValueMapper()));
        registrar.discrete("Flight Computer Drain Rate", flightComputerDrainRate.value(), withUnit("Watts", new DoubleValueMapper()));
        registrar.discrete("Combined Charge Rate", combinedCharge.value(), withUnit("Watts", new DoubleValueMapper()));
        registrar.real(BATTERY_CHARGE_RESOURCE_NAME, approximateAsLinear(clampedBatteryCharge.value()));

    }

    /*
     * Solar array charging daemon.
     * Increments battery charge based on solar array charging rate.
     */
    // public void solarArrayCharge() {
    //     Duration SOLAR_ARRAY_CHARGE_INTERVAL = Duration.duration(1, Duration.MINUTES);
    //     while(true) {
    //         delay(SOLAR_ARRAY_CHARGE_INTERVAL);
    //         Double currentSolarChargeRate = currentValue(solarArrayChargingRate);
    //         if (currentValue(batteryCharge) < BATTERY_CAPACITY) {
    //             DiscreteEffects.increase(batteryCharge, Math.min(
    //                 currentSolarChargeRate * SOLAR_ARRAY_CHARGE_INTERVAL.ratioOver(Duration.HOUR),
    //                 BATTERY_CAPACITY - currentValue(batteryCharge)));
    //         }
    //     }
    // }

    /*
     * Flight computer power drain daemon.
     * Decrements battery charge based on flight computer drain rate.
     */
    // public void flightComputerDrain() {
    //     Duration DRAIN_INTERVAL = Duration.duration(1, Duration.MINUTES);
    //     while(true) {
    //         delay(DRAIN_INTERVAL);
    //         Double currentDrainRate = currentValue(flightComputerDrainRate);
    //         if (currentValue(batteryCharge) < BATTERY_CAPACITY) {
    //             // Charge rate units in watts, battery charge in watt hours
    //             DiscreteEffects.decrease(batteryCharge, Math.min(
    //                 currentDrainRate * DRAIN_INTERVAL.ratioOver(Duration.HOUR),
    //                 currentValue(batteryCharge)));
    //         }
    //     }
    // }
}
