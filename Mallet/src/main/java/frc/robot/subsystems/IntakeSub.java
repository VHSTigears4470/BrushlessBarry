package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.K_ClawSub;

public class IntakeSub extends SubsystemBase{
  // Idle - Break
  // ID - 7
  private final CANSparkMax motor;
  private final RelativeEncoder encoder;

  // -1 = grab, 0 = idle, 1 = throw
  private double direction;
  // controls speed of motor
  private double voltage;
  
  public IntakeSub(){
    if(K_ClawSub.isUsingClaw){
      motor = new CANSparkMax(7, MotorType.kBrushless);
      encoder = motor.getEncoder();
      direction = 0;

      motor.setIdleMode(IdleMode.kBrake);
      motor.setInverted(true);
      // set original position which should represent original position
      encoder.setPosition(0);

      // control intake speed
      motor.setSmartCurrentLimit(8, 100);
    } else {
      motor = null;
      encoder = null;
    }
  }

  //Return the encoder
  public RelativeEncoder getClawEncoder(){
    return encoder;
  }

  //Return if the intake is grabbing or throwing or neither
  public double getDirection(){
    if(K_ClawSub.isUsingClaw){
        return direction;
    }
    return 0;
  }

  public void setDirection(double newDirection) {
    direction = newDirection;
  }

  // returns current through motor
  public double getCurrent() {
    if(K_ClawSub.isUsingClaw){
      return motor.getOutputCurrent();
    }
    return 0.0;
  }

  // Stops the motor in case of emergency
  public void emergencyStop(){
    motor.stopMotor();
  }

  @Override
  public void periodic() {
    if (K_ClawSub.isUsingClaw) {
      motor.setVoltage(direction * voltage);
      SmartDashboard.putNumber("Claw Encoder", encoder.getPosition());
      SmartDashboard.putNumber("Claw Direction", direction);
    }
  }
}