import java.awt.*;

/**
 * Defined variables for Players
 */
public class Player {
    private final String playerName;
    private final Color playerColor;
    public Player(String name, Color color){
        playerName = name;
        playerColor = color;
    }
    public String getPlayerName(){
        return playerName;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

}
