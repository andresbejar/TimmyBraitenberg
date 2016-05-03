package braitenbergVehicles;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Braitenberg {
	
	static RegulatedMotor leftMotor;
	static RegulatedMotor rightMotor;
	private static MovePilot pilot;
	private static EV3ColorSensor sensor;

	public static void main(String[] args) {
		// hacer el setup del vehiculo
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.C);
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		
		Wheel leftWheel = WheeledChassis.modelWheel(leftMotor, 81.6).offset(-70);
		Wheel rightWheel = WheeledChassis.modelWheel(rightMotor, 81.6).offset(70);
		
		Chassis chassis = new WheeledChassis(new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(chassis);
		pilot.setLinearSpeed(1000);
		//el sensor de color
		sensor = new EV3ColorSensor(SensorPort.S1);
		//meter el otro sensor de color!
		sensor.setFloodlight(false);
		BraitenbergAgresivo();

	}
	
	private static void BraitenbergAgresivo(){
		float [] firstSample = new float[1];
		SampleProvider sample;
		//se toma la primera muestra
		sample = sensor.getAmbientMode();
		sample.fetchSample(firstSample, 0);
		float defaultLightValue = firstSample[0];
		float newLightValue;
		boolean agresivo = false;
		while(true){
			sample=sensor.getAmbientMode();
			sample.fetchSample(firstSample, 0);
			newLightValue = firstSample[0];
			System.out.println(newLightValue);
			if(newLightValue > defaultLightValue + 0.20){
				//acelerar
				if(!agresivo){
					agresivo = true;
					pilot.forward();
				}
				
			}
			else{
				//frenar
				if(agresivo){
					agresivo = false;
					pilot.stop();
				}
			}
		}
	}

}
