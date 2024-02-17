package frc.robot.commands.elevator;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ElevatorSub;

public class ElevatorDown extends Command{
    private ElevatorSub myElevator;

    // Creation Function of the Class
    public ElevatorDown(ElevatorSub elevator) {
        myElevator = elevator;
        addRequirements(elevator);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
    }

    // Called every time the scheduler runs while the command is scheduled.
    // Relatively change claw by joystick
    @Override
    public void execute() {
        myElevator.setDirection(-.4);
    }

    // Called once the command ends or is interrupted.
    // Nothing is called here as it is covered already in the subsystem to stop the
    // motor.
    @Override
    public void end(boolean interrupted) {
        myElevator.setDirection(0);
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return false;
    }
}
