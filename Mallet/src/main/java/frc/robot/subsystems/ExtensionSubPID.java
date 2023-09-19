package frc.robot.subsystems;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.SparkMaxPIDController;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.K_ExtSub;
import frc.robot.Constants.K_PivotSub;
import java.lang.Math;

public class ExtensionSubPID extends SubsystemBase{
  // These are the Pivot Motors
  // Idle - Break on both
  // ID 6
  private final CANSparkMax motor;
  private final SparkMaxPIDController pid;
  private final RelativeEncoder encoder;
  private double kP, kI, kD, kIz, kFF, kMaxOutput, kMinOutput, maxVel, minVel, maxAcc, allowedErr;
  // This is the motorControllerGroup of the 2 prior motors
  // Intended to make the Pivot Point Turn

  // Limit Switches
  // WARNING - MAKE SURE THE LIMITS ARE HAVING THE YELLOW IN GROUND!
  //           YES IT LOOKS WRONG BUT BLAME ELECTRICAL FOR THEIR WIRING!
  //           --> DEFAULT IS ALWAYS TRUE BUT WHEN HIT THEY RETURN FALSE!

  // Determines if we got to stop all movement on the motor
  private double desiredPosition = -12;
  private double maxPosition = 0;
  private double minPosition = -12;
  
  public ExtensionSubPID(){
    if(K_PivotSub.isUsingPivot){
      motor = new CANSparkMax(6, MotorType.kBrushless);
      encoder = motor.getEncoder();
      pid = motor.getPIDController();
      motor.setIdleMode(IdleMode.kBrake);

      // set conversion factor so getPosition returns degrees

      // arc length = r(14/16 of an inch?)*theta
      encoder.setPositionConversionFactor(K_ExtSub.gearRadius*(360.0/K_ExtSub.gearRatio)/180*Math.PI); // .091629
      // set conversion ratio to 1 ONLY FOR CALIBRATING FOR ANGLE
      // encoder1.setPositionConversionFactor(1);

      encoder.setPosition(desiredPosition);
      desiredPosition = encoder.getPosition();

      // PID coefficients
      kP = 0.00000006015; 
      kI = 0.0000005;
      kD = 0; 
      kIz = 0.005; 
      kFF = 0.000101; 
      kMaxOutput = .2; 
      kMinOutput = -.2;
      // Smart Motion Coefficients

      
      double rps = K_ExtSub.inchesPerSecond / K_ExtSub.gearRadius / 2 / Math.PI;
      maxVel = rps*60*K_ExtSub.gearRatio; // rpm: .3rps -> 12 rpm -> (adjusted by gear ratio)
      maxAcc = maxVel*1.5;

      // set PID coefficients
      pid.setP(kP);
      pid.setI(kI);
      pid.setD(kD);
      pid.setIZone(kIz);
      pid.setFF(kFF);
      pid.setOutputRange(kMinOutput, kMaxOutput);

      /**
       * Smart Motion coefficients are set on a SparkMaxPIDController object
       * 
       * - setSmartMotionMaxVelocity() will limit the velocity in RPM of
       * the pid controller in Smart Motion mode
       * - setSmartMotionMinOutputVelocity() will put a lower bound in
       * RPM of the pid controller in Smart Motion mode
       * - setSmartMotionMaxAccel() will limit the acceleration in RPM^2
       * of the pid controller in Smart Motion mode
       * - setSmartMotionAllowedClosedLoopError() will set the max allowed
       * error for the pid controller in Smart Motion mode
       */
      int smartMotionSlot = 0;
      pid.setSmartMotionMaxVelocity(maxVel, smartMotionSlot);
      pid.setSmartMotionMinOutputVelocity(minVel, smartMotionSlot);
      pid.setSmartMotionMaxAccel(maxAcc, smartMotionSlot);
      pid.setSmartMotionAllowedClosedLoopError(allowedErr, smartMotionSlot);

      // display PID coefficients on SmartDashboard
      SmartDashboard.putNumber("Extension P Gain", kP);
      SmartDashboard.putNumber("Extension I Gain", kI);
      SmartDashboard.putNumber("Extension D Gain", kD);
      SmartDashboard.putNumber("Extension I Zone", kIz);
      SmartDashboard.putNumber("Extension Feed Forward", kFF);
      SmartDashboard.putNumber("Extension Max Output", kMaxOutput);
      SmartDashboard.putNumber("Extension Min Output", kMinOutput);

      // display Smart Motion coefficients
      SmartDashboard.putNumber("Extension Max Velocity", maxVel);
      SmartDashboard.putNumber("Extension Min Velocity", minVel);
      SmartDashboard.putNumber("Extension Max Acceleration", maxAcc);
      SmartDashboard.putNumber("Extension Allowed Closed Loop Error", allowedErr);
      SmartDashboard.putNumber("Extension Set Position", 0);

      // button to toggle between velocity and smart motion modes
    
      SmartDashboard.putBoolean("Extension Mode", true);

    }
  }

  //Return the encoder
  public RelativeEncoder getEncoder(){
    return encoder;
  }

  //Return the maxAngle
  public double getMaxPosition(){
    return maxPosition;
  }

  // sets the desired angle to set angle to
  // 0 - 100 degrees
  public void setAngle (double angle) {
    if(K_PivotSub.isUsingPivot){
      desiredPosition = angle;
    }
    pid.setReference(desiredPosition, CANSparkMax.ControlType.kSmartMotion);
  }

  //Returns the current angle of the pivot
  public double getCurrentAngle(){
    if(K_PivotSub.isUsingPivot)
      return encoder.getPosition();
    return 0.0;
  }

  //Returns the current desired angle
  public double getDesiredPosition(){
    if(K_PivotSub.isUsingPivot)
      return desiredPosition;
    return 0.0;
  }

  // Changes angle to aim for
  // If change is past min or max in either direction revert the change
  public void changePosition (double increment) {
    if(K_ExtSub.isUsingExt){
      // controller deadzone
      desiredPosition += increment;
      if (desiredPosition > maxPosition) 
        desiredPosition= maxPosition;
      else if (desiredPosition < minPosition) 
        desiredPosition= minPosition;
    }
    pid.setReference(desiredPosition, CANSparkMax.ControlType.kSmartMotion);
  }


  // Stops the motor in case of emergency
  public void emergencyStop() {
    if(K_PivotSub.isUsingPivot){
      motor.stopMotor();
    }
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Extension Encoder", encoder.getPosition());
    SmartDashboard.putNumber("Extension Desired Angle", desiredPosition);
    double p = SmartDashboard.getNumber("Extension P Gain", 0);
    double i = SmartDashboard.getNumber("Extension I Gain", 0);
    double d = SmartDashboard.getNumber("Extension D Gain", 0);
    double iz = SmartDashboard.getNumber("Extension I Zone", 0);
    double ff = SmartDashboard.getNumber("Extension Feed Forward", 0);
    double max = SmartDashboard.getNumber("Extension Max Output", 0);
    double min = SmartDashboard.getNumber("Extension Min Output", 0);
    double maxV = SmartDashboard.getNumber("Extension Max Velocity", 0);
    double minV = SmartDashboard.getNumber("Extension Min Velocity", 0);
    double maxA = SmartDashboard.getNumber("Extension Max Acceleration", 0);
    double allE = SmartDashboard.getNumber("Extension Allowed Closed Loop Error", 0);

    // if PID coefficients on SmartDashboard have changed, write new values to controller
    if((p != kP)) { pid.setP(p); kP = p; }
    if((i != kI)) { pid.setI(i); kI = i; }
    if((d != kD)) { pid.setD(d); kD = d; }
    if((iz != kIz)) { pid.setIZone(iz); kIz = iz; }
    if((ff != kFF)) { pid.setFF(ff); kFF = ff; }
    if((max != kMaxOutput) || (min != kMinOutput)) { 
      pid.setOutputRange(min, max); 
      kMinOutput = min; kMaxOutput = max; 
    }
    if((maxV != maxVel)) { pid.setSmartMotionMaxVelocity(maxV,0); maxVel = maxV; }
    if((minV != minVel)) { pid.setSmartMotionMinOutputVelocity(minV,0); minVel = minV; }
    if((maxA != maxAcc)) { pid.setSmartMotionMaxAccel(maxA,0); maxAcc = maxA; }
    if((allE != allowedErr)) { pid.setSmartMotionAllowedClosedLoopError(allE,0); allowedErr = allE; }
    // desiredAngle = SmartDashboard.getNumber("Set Position", 0);
      /**
       * As with other PID modes, Smart Motion is set by calling the
       * setReference method on an existing pid object and setting
       * the control type to kSmartMotion
       */
      pid.setReference(desiredPosition, CANSparkMax.ControlType.kSmartMotion);
  }
}