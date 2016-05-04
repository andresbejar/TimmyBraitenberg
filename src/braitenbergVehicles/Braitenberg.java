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
	private static EV3ColorSensor leftSensor;
	private static EV3ColorSensor rightSensor;

	public static void main(String[] args) {
		// hacer el setup del vehiculo
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.C);
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		
		Wheel leftWheel = WheeledChassis.modelWheel(leftMotor, 81.6).offset(-70);
		Wheel rightWheel = WheeledChassis.modelWheel(rightMotor, 81.6).offset(70);
		
		Chassis chassis = new WheeledChassis(new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(chassis);
		pilot.setLinearSpeed(500);
		//el sensor de color
		leftSensor = new EV3ColorSensor(SensorPort.S1);
		rightSensor = new EV3ColorSensor(SensorPort.S3);
		
		leftSensor.setFloodlight(false);
		rightSensor.setFloodlight(false);
		BraitenbergAgresivo();
		
		/*
		 * Agregarle aqui los otros metodos
		 * Que impriman el estado en el que esta y continuen
		 * en un loop hasta que se presione ENTER
		 */

	}
	
	private static void BraitenbergAgresivo(){
		System.out.println("Entrando en modo agresivo");
		Delay.msDelay(5000);
		float [] firstSample = new float[2];
		
		//Los sensores devuelven SampleProviders
		SampleProvider leftSample;
		SampleProvider rightSample;
		
		//se toma la primera muestra
		//TODO: se tiene que volver a crear el sampleprovider por cada muestra?
		//o creando uno y luego haciendole fetchSample() multiples veces funciona?
		//PROBAR
		leftSample = leftSensor.getAmbientMode();
		rightSample = rightSensor.getAmbientMode();
		leftSample.fetchSample(firstSample, 0);
		rightSample.fetchSample(firstSample, 1);
		
		//se calcula un valor default usando el promedio de lo que reporte cada sensor
		float defaultLightValue = (firstSample[0] + firstSample[1]) / 2.0f;
		float newLightValue;
		
		//numero determinado heuristicamente que describe la diferencia
		//en intensidad percibida por los sensores cuando la luz viene de un solo
		//lado (izq o derecha)
		float magicNumber = 0.10f;
		//un flag
		boolean agresivo = false;
		
		//en teoria timmy tiene que empezar moviendose lentamente hacia al frente
		//y acelerar y girar violentamente si detecta luz
		pilot.forward();
		
		//seguir hasta que se presione ENTER
		while(!Button.ENTER.isDown()){
			leftSample= leftSensor.getAmbientMode();
			rightSample = rightSensor.getAmbientMode();
			leftSample.fetchSample(firstSample, 0);
			rightSample.fetchSample(firstSample, 1);
			newLightValue = (firstSample[0] + firstSample[1]) / 2.0f;
			System.out.println(newLightValue);
			if(newLightValue > defaultLightValue + 0.20){
				//acelerar
				//tal vez haya que quitarle el flag
				//si ya esta agresivo y le muevo la luz al lado opuesto
				//y el flag no cambia entonces no gira hacia el otro lado
				//hay que probar si da chance para que la luz vuelva a nivel estandar
				if(!agresivo){
					agresivo = true;
					pilot.setLinearSpeed(1000); //la velocidad se puede cambiar mientras se mueve?
					//si la luz es mas intensa del lado izquierdo
					//girar hacia la izquierda
					//aqui se deberian usar heuristicas para ver cuanto mas
					//intenso es la luz del lado izquierdo poniendo el sensor
					//para calibrar al robot
					if(firstSample[0] > firstSample[1] + magicNumber){
						pilot.rotate(45);
					}
					//de lo contrario, girar a la derecha
					else if (firstSample[1] > firstSample[0] + magicNumber){
						pilot.rotate(-45);
					}
					//la diferencia no es suficiente, no gire
					pilot.forward();
				}
				
			}
			else{
				//bajar la velocidad
				if(agresivo){
					agresivo = false;
					pilot.setLinearSpeed(500);
					pilot.forward();
				}
			}
		}
	}

}
