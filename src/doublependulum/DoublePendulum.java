/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doublependulum;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JFrame;

public class DoublePendulum extends JFrame {

    int WIDTH = 800, HEIGHT = 800;

    //Changeable Variables
    double grav = 9.81;
    double len = 170;
    double firstAngle = Math.PI;
    double secondAngle = Math.PI*1.01;
    int dt = 1000 / 60;
    //end changeable variables
    
    
    int xStart = 400;
    int yStart = 400;

    Weight weight1 = new Weight(len, firstAngle);
    Weight weight2 = new Weight(len, secondAngle);
    int[][] lineBitMap = new int[WIDTH][HEIGHT]; //bitmap that draws the lines whenever the pendulum passes over it
    Point2D prevPoint = new Point2D((int) (xStart + len * Math.cos(weight1.angle - Math.PI / 2) + len * Math.cos(weight2.angle - Math.PI / 2)), (int) (yStart - len * Math.sin(weight1.angle - Math.PI / 2) - len * Math.sin(weight2.angle - Math.PI / 2)));
    Point2D nextPoint = prevPoint;

    public void paint(Graphics g) {
        Image img = createImage();
        g.drawImage(img, 0, 0, this);
    }

    private Image createImage() {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        Graphics2D G = (Graphics2D) bufferedImage.getGraphics();
        G.setColor(Color.white);

        G.fillOval(xStart - 5, yStart - 5, 10, 10);

        G.setColor(Color.darkGray);             //paints dark grey if pendulum goes over once
        for (int i = 0; i < 800; i++) {
            for (int j = 0; j < 800; j++) {
                if (lineBitMap[i][j] == 1) {
                    G.drawLine(i, j, i, j);
                }
            }
        }
        G.setColor(Color.lightGray);            //paints light grey if pendulum goes over twice
        for (int i = 0; i < 800; i++) {
            for (int j = 0; j < 800; j++) {
                if (lineBitMap[i][j] == 2) {
                    G.drawLine(i, j, i, j);
                }
            }
        }
        G.setColor(Color.white);                //paints white if pendulum goes over more than 2x
        for (int i = 0; i < 800; i++) {
            for (int j = 0; j < 800; j++) {
                if (lineBitMap[i][j] > 2) {
                    G.drawLine(i, j, i, j);
                }
            }
        }

        double x1 = xStart + len * Math.cos(weight1.angle - Math.PI / 2);
        double y1 = yStart - len * Math.sin(weight1.angle - Math.PI / 2);

        double x2 = x1 + len * Math.cos(weight2.angle - Math.PI / 2);
        double y2 = y1 - len * Math.sin(weight2.angle - Math.PI / 2);

        nextPoint = new Point2D((int) x2, (int) y2);

        G.setColor(Color.yellow);                               //draws pendulum
        G.drawLine(xStart, yStart, (int) (x1), (int) (y1));
        G.drawLine((int) (x1), (int) (y1), (int) (x2), (int) (y2));

        G.setColor(Color.red);
        G.fillOval((int) (x1 - 5), (int) (y1 - 5), 10, 10);

        G.setColor(Color.blue);
        G.fillOval((int) (x2 - 5), (int) (y2 - 5), 10, 10);

        return bufferedImage;
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
        }
    }
    
    public void updateWeights() {
        calculateAngleChange();
        calculateMomentumChange();
        applyMomentumAndAngleChange();
    }


    public void calculateMomentumChange() { //calculates momentum change
        weight1.dp = len * (-2 * grav * Math.sin(weight1.angle)
                - len * weight1.dAngle * weight2.dAngle * Math.sin(weight1.angle - weight2.angle));
        weight2.dp = len * (len * weight1.dAngle * weight2.dAngle * Math.sin(weight1.angle - weight2.angle)
                - grav * Math.sin(weight2.angle));
    }

    public void calculateAngleChange() { //calculates angle change
        double fact = 1 / (len * len) / (-2 + Math.pow(Math.cos(weight1.angle - weight2.angle), 2));
        weight1.dAngle = -fact * (weight1.p - Math.cos(weight1.angle - weight2.angle) * weight2.p);
        weight2.dAngle = -fact * (2 * weight2.p - Math.cos(weight1.angle - weight2.angle) * weight1.p);

    }

    private void applyMomentumAndAngleChange() { //updates values
        weight1.angle += dt * weight1.dAngle / 1000;
        weight2.angle += dt * weight2.dAngle / 1000;
        weight1.p += (dt * weight1.dp / 1000);
        weight2.p += (dt * weight2.dp / 1000);
    }

    public void updateLine() { //finds point values and updates the bitmap for drawing by connecting points to make lines in pixels
        if ((prevPoint.xVal - nextPoint.xVal) != 0) {
            double slope = (prevPoint.yVal - nextPoint.yVal) / (prevPoint.xVal - nextPoint.xVal);
            double b = prevPoint.yVal - slope * prevPoint.xVal;
            if (Math.abs(slope) > 1) {
                for (int i = (int) (prevPoint.yVal); i < (int) (nextPoint.yVal); i++) {
                    int xVal = (int) ((i - b) / slope);
                    if (xVal > 0 && xVal < 800 && i > 0 && i < 800) {
                        lineBitMap[xVal][i]++;
                    }
                }
                for (int i = (int) (nextPoint.yVal); i < (int) (prevPoint.yVal); i++) {
                    int xVal = (int) ((i - b) / slope);
                    if (xVal > 0 && xVal < 800 && i > 0 && i < 800) {
                        lineBitMap[xVal][i]++;
                    }
                }
            } else {
                for (int i = (int) (prevPoint.xVal); i < (int) (nextPoint.xVal); i++) {
                    int yVal = (int) (slope * i + b);
                    if (yVal > 0 && yVal < 800 && i > 0 && i < 800) {
                        lineBitMap[i][yVal]++;
                    }
                }
                for (int i = (int) (nextPoint.xVal); i < (int) (prevPoint.xVal); i++) {
                    int yVal = (int) (slope * i + b);
                    if (yVal > 0 && yVal < 800 && i > 0 && i < 800) {
                        lineBitMap[i][yVal]++;
                    }
                }
            }
            prevPoint = nextPoint;
        }

    }

    public static void main(String[] args) throws IOException {
        DoublePendulum pend = new DoublePendulum();
        pend.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pend.setSize(pend.WIDTH, pend.HEIGHT);
        pend.updateWeights();
        pend.setVisible(true);
        while (true) {
            for (int i = 0; i < 8; i++) {
                pend.updateWeights();
            }
            pend.repaint();
            pend.updateLine();
            DoublePendulum.sleep(pend.dt);
        }
    }
}
