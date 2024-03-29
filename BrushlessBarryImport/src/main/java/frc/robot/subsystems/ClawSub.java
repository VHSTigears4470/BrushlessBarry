package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.K_IntakeSub;

public class ClawSub extends SubsystemBase {
  // This is the Claw Extension Motor
  // Idle - Break
  // ID - 7
  private final CANSparkMax motor;
  private final RelativeEncoder encoder;

  // Calculation variables
  private double openPosition;

  // close close to full clamp point
  private boolean isOpen = true;
  // private boolean isStopped = false;

  public ClawSub() {
    if (K_IntakeSub.isUsingIntake) {
      motor = new CANSparkMax(6, MotorType.kBrushless);
      encoder = motor.getEncoder();
      motor.setIdleMode(IdleMode.kBrake);
      // set conversion factor so getPosition returns degrees
      encoder.setPositionConversionFactor((K_IntakeSub.calibrateEndingAngle - K_IntakeSub.calibrateStartingAngle)
          / K_IntakeSub.calibrateAngleEncoderValue);
      // code to set default to find conversion factor
      // encoder.setPositionConversionFactor(1);
      motor.setInverted(true);

      // set original position which should represent original position
      encoder.setPosition(0);
      openPosition = 0;

      // control grab strength
      motor.setSmartCurrentLimit(8, 100);
    } else {
      motor = null;
      encoder = null;
    }
  }

  // Return the encoder
  public RelativeEncoder getClawEncoder() {
    return encoder;
  }

  // Return if the claw is open
  public boolean getIsOpen() {
    return isOpen;
  }

  // Handles motor movement
  // Adjusts voltage / motor speed based on difference between current and desired
  // angle
  // - voltage is close claw
  public void moveClaw() {
    if (K_IntakeSub.isUsingIntake)
      if (isOpen) {
        double calculatedVoltage = (openPosition - encoder.getPosition()) / 5;
        // If calculated voltage too high reset it to within +- maximum clamp seed
        if (Math.abs(calculatedVoltage) > K_IntakeSub.clampVoltage)
          calculatedVoltage = calculatedVoltage > 0 ? K_IntakeSub.clampVoltage : -K_IntakeSub.clampVoltage;
        motor.setVoltage(calculatedVoltage);
        // if open position is somehow negative past clamp position then set it back so
        // its open
      } else {
        motor.setVoltage(K_IntakeSub.clampVoltage);
      }

  }

  // Returns the current angle of the motor
  public double getCurrentAngle() {
    if (K_IntakeSub.isUsingIntake) {
      return encoder.getPosition();
    }
    return 0.0;
  }

  public void setOpen(boolean open) {
    isOpen = open;
  }

  // Return the intended angle of the motor
  public double getDesiredAngle() {
    if (K_IntakeSub.isUsingIntake) {
      return openPosition;
    }
    return 0.0;
  }

  // Changes angle that is the supposed open angle in case something goes wrong
  // with the encoder
  public void changeOpenPosition(double increment) {
    if (K_IntakeSub.isUsingIntake) {
      // controller deadzone
      openPosition += increment;
    }
    SmartDashboard.putNumber("Open Position", openPosition);
    SmartDashboard.putNumber("Open Position Increment", increment);
  }

  // returns current through motor
  public double getCurrent() {
    if (K_IntakeSub.isUsingIntake) {
      return motor.getOutputCurrent();
    }
    return 0.0;
  }

  public boolean isOpen() {
    return isOpen;
  }

  // Stops the motor in case of emergency
  public void emergencyStop() {
    if (K_IntakeSub.isUsingIntake) {
      motor.stopMotor();
    }
  }

  @Override
  public void periodic() {
    if (K_IntakeSub.isUsingIntake)
      SmartDashboard.putNumber("Claw Encoder", encoder.getPosition());
    SmartDashboard.putNumber("Claw Open Position", openPosition);
  }
}
