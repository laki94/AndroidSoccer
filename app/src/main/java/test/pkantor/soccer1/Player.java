package test.pkantor.soccer1;

/**
 * Created by Pawel on 06.11.2017.
 */

public class Player {

    private String name;
    private int points = 0;
    private boolean move = false;
    private boolean additionalMove = false;

    public Player()
    {

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdditionalMove() {
        return additionalMove;
    }

    public void setAdditionalMove(boolean additionalMove) {
        this.additionalMove = additionalMove;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

}
