/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;

/**
 *
 * @author Michael
 */
public class Controls {

    private Joystick joystick;

    public Controls(Joystick _joystick) {
        joystick = _joystick;
    }

    public boolean button1() {
        return joystick.getRawButton(1);
    }

    public boolean button2() {
        return joystick.getRawButton(2);
    }

    public boolean button3() {
        return joystick.getRawButton(3);
    }

    public boolean button4() {
        return joystick.getRawButton(4);
    }
    
    public boolean button5() {
        return joystick.getRawButton(5);
    }
    
    public boolean button6() {
        return joystick.getRawButton(6);
    }
    
    public boolean button7() {
        return joystick.getRawButton(7);
    }
    
    public boolean button8() {
        return joystick.getRawButton(8);
    }
    
    public boolean button9() {
        return joystick.getRawButton(9);
    }

}
