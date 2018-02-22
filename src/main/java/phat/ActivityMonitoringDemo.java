/*
 * Copyright (C) 2014 Pablo Campillo-Sanchez <pabcampi@ucm.es>
 *
 * This software has been developed as part of the
 * SociAAL project directed by Jorge J. Gomez Sanz
 * (http://grasia.fdi.ucm.es/sociaal)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package phat;
import java.util.logging.Logger;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import com.jme3.scene.Node;
import com.jme3.scene.control.Control;
import phat.agents.actors.ActorFactory;
import phat.agents.actors.parkinson.HandTremblingControl;
import phat.app.PHATApplication;
import phat.app.PHATInitAppListener;
import phat.body.BodiesAppState;
import phat.body.commands.PlayBodyAnimationCommand;
import phat.body.commands.SetBodyInCoordenatesCommand;
import phat.body.commands.SetSpeedDisplacemenetCommand;
import phat.body.control.parkinson.HeadTremblingControl;
import phat.control.LeftArmMoveControl;
import phat.control.RightArmMoveControl;
import phat.devices.DevicesAppState;
import phat.devices.commands.CreateAccelerometerSensorCommand;
import phat.devices.commands.SetDeviceOnPartOfBodyCommand;
import phat.environment.SpatialEnvironmentAPI;
import phat.sensors.accelerometer.AccelerometerControl;
import phat.sensors.accelerometer.XYAccelerationsChart;
import phat.server.ServerAppState;
import phat.server.commands.ActivateAccelerometerServerCommand;
import phat.structures.houses.HouseFactory;
import phat.structures.houses.commands.CreateHouseCommand;
import phat.util.Debug;
import phat.util.SimpleScenario;
import phat.world.WorldAppState;

/**
 * Activity Monitoring gesture.
 */
public class ActivityMonitoringDemo extends SimpleScenario {

    private static final Logger logger = Logger.getLogger(ActivityMonitoringDemo.class.getName());

    DevicesAppState devicesAppState;
    WorldAppState worldAppState;
    ServerAppState serverAppState;

    /**
     * @param args
     */
    public static void main(String[] args) {
        ActivityMonitoringDemo app = new ActivityMonitoringDemo();
        app.setDisplayFps(false);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.setDisplayStatView(false);

        app.start();
    }

    @Override
    public void createTerrain() {
        Debug.enableDebugGrid(5f, assetManager, rootNode);
    }

    @Override
    public void createOtherObjects() {
        ActorFactory.init(rootNode, assetManager, bulletAppState);
        Node model = ActorFactory.createActorModel("Model", "Models/People/Elder/Elder.j3o", 4f);

        rootNode.attachChild(model);
        initAnimation(model);
        initGestures(model);

        for(int i = 0; i < model.getNumControls(); i++) {
            Control c = model.getControl(i);
            System.out.println("------>" + c.getClass().getSimpleName());
        }
    }

    private void initGestures(Node model) {
        HandTremblingControl htcR = new HandTremblingControl(HandTremblingControl.Hand.RIGHT_HAND);
        model.addControl(htcR);

        HandTremblingControl htcL = new HandTremblingControl(HandTremblingControl.Hand.LEFT_HAND);
        model.addControl(htcL);

        HeadTremblingControl headtc = new HeadTremblingControl();
        model.addControl(headtc);

        LeftArmMoveControl lac = new LeftArmMoveControl();
        model.addControl(lac);

        RightArmMoveControl rac = new RightArmMoveControl();
        model.addControl(rac);

    }

    int cycleCounter = 0;
    private void initAnimation(final Node model) {
        logger.info("----> init animation");
        AnimControl ac = ActorFactory.findControl(model, AnimControl.class);

        AnimChannel anim = ac.createChannel();

        anim.setAnim("EatStanding", 5f);
        anim.setLoopMode(LoopMode.Loop);

        final RightArmMoveControl rac_ = new RightArmMoveControl();
        final LeftArmMoveControl lac_ = new LeftArmMoveControl();

        ac.addListener(new AnimEventListener() {
            float deterioring = FastMath.PI;

            @Override
            public void onAnimCycleDone(AnimControl ac, AnimChannel ac1, String animName) {
                logger.info("--> onAnimCycleDone Time:" + getTimer().getTime());
                model.addControl(lac_);
                model.addControl(rac_);

                AnimChannel animChanel = ac.getChannel(0);

                if(cycleCounter == 5) {
                    if(animName.equals("EatStanding")) {
                        animChanel.setAnim("WalkForward", 2f);
                        logger.info("cycle: " + cycleCounter + ", Anim: WalkForward, time: " + animChanel.getTime() + ", speed: " + animChanel.getSpeed());
                    } else {
                        animChanel.setAnim("EatStanding", 10f);
                        logger.info("cycle: " + cycleCounter + ", Anim: EatStanding, time: " + animChanel.getTime() + ", speed: " + animChanel.getSpeed());
                    }
                    model.getControl(RightArmMoveControl.class).setAngular(FastMath.PI/deterioring);
                    cycleCounter = 0;
                } else {
                    //if(animChanel.getTime() >= 0.0){
                    logger.info("cycle: " + cycleCounter + ", time: " + animChanel.getTime() + ", speed: " + animChanel.getSpeed());
                    //animChanel.setAnim("EatStanding", 10f);

                    if(animChanel.getTime() > 1.0 && animChanel.getTime() < 2.5){
                        model.getControl(RightArmMoveControl.class).setAngular(FastMath.PI/deterioring);
                        model.getControl(RightArmMoveControl.class).setMinAngle(-FastMath.QUARTER_PI);
                        model.addControl(new LeftArmMoveControl(-FastMath.PI/2, 0, FastMath.PI/deterioring));
                        logger.info("---> Change animation in time: " + animChanel.getTime());
                    }else{
                        model.getControl(RightArmMoveControl.class).setMinAngle(-FastMath.PI/2);
                        model.addControl(new LeftArmMoveControl(-FastMath.PI/2, 0, FastMath.PI/deterioring));
                        logger.info("---> NO Change animation in time: " + animChanel.getTime());
                    }/*
                		if(animChanel.getTime() > 2.5 && animChanel.getTime() < 3.5){
                			animChanel.setAnim("LookBehindR", 2f);
                			model.getControl(RightArmMoveControl.class).setMinAngle(-FastMath.PI/2);
                			model.addControl(new LeftArmMoveControl(-FastMath.PI/2, 0, FastMath.PI/deterioring));
                			logger.info("---> NO Change animation in time: " + animChanel.getTime());
                		}
                		if(animChanel.getTime() > 3.5 && animChanel.getTime() < 4.0){
                			animChanel.setAnim("IdleStanding", 2f);
                			model.getControl(RightArmMoveControl.class).setMinAngle(-FastMath.PI/2);
                			model.addControl(new LeftArmMoveControl(-FastMath.PI/2, 0, FastMath.PI/deterioring));
                			logger.info("---> NO Change animation in time: " + animChanel.getTime());
                		}*/
                    //}
                    cycleCounter++;
                }
                deterioring += FastMath.PI/cycleCounter;
            }

            @Override
            public void onAnimChange(AnimControl ac, AnimChannel ac1, String string) {
                //logger.info("--> onAninChange");
                //ac1.setSpeed(5f);
            }
        });
    }
}