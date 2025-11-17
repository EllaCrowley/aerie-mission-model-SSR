package missionmodel;

import gov.nasa.jpl.aerie.contrib.streamline.unit_aware.Unit;
import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.StandardUnits.HOUR;
import static gov.nasa.jpl.aerie.contrib.streamline.unit_aware.StandardUnits.WATT;

public class Utils {

  public static final Unit WATT_HOUR = Unit.derived("Wh", "watt hour", WATT.multiply(HOUR));

  public static String getGroundContactActivityName() {
    return GroundContact.GROUND_CONTACT_TYPE_NAME;
  }
  public static String getEclipseActivityName() {
    return Eclipse.ECLIPSE_TYPE_NAME;
  }
  public static String getBatteryChargeResourceName() {
    return PowerModel.BATTERY_CHARGE_RESOURCE_NAME;
  }
}
