package missionmodel;

import gov.nasa.jpl.aerie.contrib.streamline.modeling.Registrar;

/**
 * Top-level Mission Model Class
 *
 * Declare, define, and register resources within this class or its delegates
 * Spawn daemon tasks using spawn(objectName::nameOfMethod) within the class constructor
 */
public final class Mission {

  // Special registrar class that handles simulation errors via auto-generated resources
  public final Registrar errorRegistrar;

  public final PowerModel powerModel;

  public Mission(final gov.nasa.jpl.aerie.merlin.framework.Registrar registrar, final Configuration config) {
    this.errorRegistrar = new Registrar(registrar, Registrar.ErrorBehavior.Log);

    this.powerModel = new PowerModel(this.errorRegistrar, config);

  }
}
