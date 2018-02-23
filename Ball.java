import java.awt.*;
 
/**
 * Defines important ball variables used in the array
 */
public class Ball {
    private Color ballColor;
    private final int maxValue;
    private int value;
    private int horizontalLine;
    private int verticalLine;
 
    public Ball(String type) {
        switch (type) {
            case "Corner":
                maxValue = 2;
                value = 0;
                break;
            case "Edge":
                maxValue = 3;
                value = 0;
                break;
                default:
                maxValue = 4;
                value = 0;
                break;
 
        }
    }
 
    public int getVerticalLine() {
        return verticalLine;
    }
 
    public int getHorizontalLine() {
        return horizontalLine;
    }
 
    public int getValue() {
        return value;
    }
 
    public Color getBallColor() {
        return ballColor;
    }
 
    public int getMaxValue() {
        return maxValue;
    }
 
    public void setHorizontalLine(int h) {
        horizontalLine = h;
    }
 
    public void setVerticalLine(int v) {
        verticalLine = v;
    }
 
    public void setValue(int v) {
        value = v;
    }
 
    public void setColor(Color c) {
        ballColor = c;
    }
}
