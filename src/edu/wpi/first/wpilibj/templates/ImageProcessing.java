/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;

/**
 *
 * @author Rajath
 * find targets
 */
public class ImageProcessing {

    ParticleAnalysisReport particles[];
    Physics imageCalculations;
    CriteriaCollection criteriaCollection = new CriteriaCollection();
    ParticleAnalysisReport bottomTarget, topTarget, middleTarget;
    int numberOfDegreesInVerticalFieldOfView = 33;
    final int numberOfPixelsVerticalInFieldOfView = 480;
    final int numberOfPixelsHorizontalInFieldOfView = 640;
    final int heightToTheTopOfTheTopTarget = 118;
    final int heightToBottomOfTopTarget = 100;
    int PixelsFromLevelToBottomOfTopTarget = 0;
    int PixelsFromLevelToTopOfTopTarget = 0;
    boolean isLookingAtTopTarget = false;
    int cameraHeight = 49;

    public ImageProcessing() {
        criteriaCollection.addCriteria(
                MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
        criteriaCollection.addCriteria(
                MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
        imageCalculations = new Physics(true);
    }

    public void getPixelsFromLevelToBottomOfTopTarget(
            ParticleAnalysisReport particle) {
        PixelsFromLevelToBottomOfTopTarget =
                numberOfPixelsVerticalInFieldOfView - particle.center_mass_y
                - (particle.boundingRectHeight / 2);
    }

    public void getPixelsFromLevelToTopOfTopTarget(
            ParticleAnalysisReport particle) {
        PixelsFromLevelToTopOfTopTarget =
                numberOfPixelsVerticalInFieldOfView - particle.center_mass_y
                - (particle.boundingRectHeight / 2);
    }

    public double getPhi(int PixelsFromLevelToTopOfTopTarget) {
        double phi =
                (PixelsFromLevelToTopOfTopTarget /
                    numberOfPixelsVerticalInFieldOfView)
                * numberOfDegreesInVerticalFieldOfView;
        return phi;
    }

    public double getTheta(int PixelsFromLevelToBottomOfTopTarget) {
        double theta =
                (PixelsFromLevelToBottomOfTopTarget /
                    numberOfPixelsVerticalInFieldOfView)
                * numberOfDegreesInVerticalFieldOfView;

        return theta;
    }

    public double getHypotneuse1(double angle) {
        double opposite1 = heightToTheTopOfTheTopTarget - cameraHeight;
        double hypotneuse_1 =
                opposite1
                / MathX.sin(getPhi(PixelsFromLevelToTopOfTopTarget));
        return hypotneuse_1;
    }

    public double getHypotneuse0(double angle) {

        double opposite0 = heightToBottomOfTopTarget - cameraHeight;
        double hypotneuse_0 =
                opposite0
                / MathX.sin(getTheta(PixelsFromLevelToBottomOfTopTarget));
        return hypotneuse_0;
    }

    public boolean isLookingAtTopTarget() {
        double adjacent1 =
                MathX.cos(getPhi(PixelsFromLevelToTopOfTopTarget))
                * getHypotneuse1(getPhi(PixelsFromLevelToTopOfTopTarget));
        double adjacent0 =
                MathX.cos(getTheta(PixelsFromLevelToBottomOfTopTarget))
                * getHypotneuse0(getTheta(PixelsFromLevelToBottomOfTopTarget));
        System.out.println("Adjacent0 : " + adjacent0);
        System.out.println("Adjacent1 : " + adjacent1);
        if (adjacent0 == adjacent1) {
            return true;
        } else {
            return false;
        }
    }

    public void idTopTarget(ParticleAnalysisReport[] particles) {
        for (int i = 0; i < particles.length; i++) {
            ParticleAnalysisReport particle = particles[i];

            getPixelsFromLevelToTopOfTopTarget(particle);
            getPixelsFromLevelToBottomOfTopTarget(particle);

            isLookingAtTopTarget = isLookingAtTopTarget();
        }
    }

    public void getTheParticles(AxisCamera cam) throws Exception {
         //get image from the camera
        ColorImage colorImg = cam.getImage();
        //seperate the light and dark image
        BinaryImage binImg = colorImg.thresholdRGB(0, 42, 71, 255, 0, 255);
        colorImg.free();
        //remove the small objects
        BinaryImage clnImg = binImg.removeSmallObjects(false, 2);
        //fill the rectangles that were created
        BinaryImage convexHullImg =
                clnImg.convexHull(false);
        BinaryImage filteredImg =
                convexHullImg.particleFilter(criteriaCollection);
        particles = filteredImg.getOrderedParticleAnalysisReports();
        organizeParticles(particles, getTotalXCenter(particles),
                getTotalYCenter(particles));
        binImg.free();
        clnImg.free();
        convexHullImg.free();
        filteredImg.free();
    }

    public static ParticleAnalysisReport getTopMost(
            ParticleAnalysisReport[] particles) {
        ParticleAnalysisReport particle = particles[0];
        for (int i = 0; i < particles.length; i++) {
            if (particle.center_mass_y < particles[i].center_mass_y) {
                particle = particles[i];
            }
        }
        return particle;
    }

    public int getTotalXCenter(ParticleAnalysisReport[] particles) {
        int averageHeight = 0;

        if (particles.length == 0) {
            averageHeight = -1;
        } else {
            for (int i = 0; i < particles.length; i++) {
                averageHeight += particles[i].center_mass_x;
            }
            averageHeight /= particles.length;
        }
        return averageHeight;
    }

    public int getTotalYCenter(ParticleAnalysisReport[] particles) {
        int averageWidth = 0;

        if (particles.length == 0) {
            averageWidth = -1;
        } else {
            for (int i = 0; i < particles.length; i++) {
                averageWidth += particles[i].center_mass_y;
            }
            averageWidth /= particles.length;
        }
        return averageWidth;
    }

    public void organizeParticles(
            ParticleAnalysisReport[] particles,
            int centerMassHorizontal,
            int centerMassVertical) {
        double calculatedHeight;
        // the following values are in pixels
        double cameraOffset = 49;
        double bottomHeight = 38;
        double middleHeight = 71;
        double topHeight = 108;
        double errorRange = 3;
        String display = "";
        if (centerMassHorizontal == -1 || centerMassVertical == -1) {
            display += "No targets have been found\n";
        } else {
            display += particles.length + "Report"
                    + ((particles.length == 1) ? "" : "s") + "\n";
            for (int i = 0; i < particles.length; i++) {
                ParticleAnalysisReport particle = particles[i];
                display += particle.imageHeight + "\n";
                calculatedHeight =
                        imageCalculations.getHeight(
                        particle.imageHeight, particle.center_mass_y)
                        + cameraOffset;
                display += calculatedHeight + "\n";
                if (Math.abs(bottomHeight - calculatedHeight) < errorRange) {
                    display += "Bottom\n";
                    bottomTarget = particle;
                } else if (Math.abs(middleHeight - calculatedHeight)
                        < errorRange) {
                    display += "Middle\n";
                    middleTarget = particle;
                } else if (Math.abs(topHeight - calculatedHeight)
                        < errorRange) {
                    display += "Top\n";
                    topTarget = particle;
                }
            }
        }
        display += "----------------------\n";
        System.out.print(display);
    }
}
