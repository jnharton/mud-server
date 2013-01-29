package mud.miscellaneous;

import java.util.ArrayList;

import mud.weather.Weather;

public class Atmosphere {
	private Atmosphere parent;
	private ArrayList<Atmosphere> atmospheres;
	//private ArrayList<Layer> layers; 
	//private ArrayList<Effect> effects; // caustic, etc
	ArrayList<WeatherEffect> weatherEffects;
	public Weather weather; // rain, sun, freezing rain, sleet, hail, snow, wind // conditions may have intensity details
	private final static String DEFAULT_WEATHER = "Cloudy";

	public Atmosphere() {
		this.atmospheres = new ArrayList<Atmosphere>();
		//this.layers = new ArrayList<Layer>();
		//this.effects = new ArrayList<Effect>();
		this.weatherEffects = new ArrayList<WeatherEffect>();
		//this.weather = new Weather();
		this.weather = new Weather(Atmosphere.DEFAULT_WEATHER);
	}

	public Atmosphere(Atmosphere tParent) {
		this.parent = tParent;
		this.atmospheres = new ArrayList<Atmosphere>();
		//this.layers = new ArrayList<Layer>();
		//this.effects = new ArrayList<Effect>();
		this.weather = tParent.weather;
		this.weatherEffects = tParent.weatherEffects;
	}

	public void add(Atmosphere a) {
		this.atmospheres.add(a);
	}

	public void remove(Atmosphere a) {
		this.atmospheres.remove(a);
	}

	public void setParent(Atmosphere a) {
		this.parent = a;
		this.weather = this.parent.weather;
		this.weatherEffects = this.parent.weatherEffects;
	}
}
