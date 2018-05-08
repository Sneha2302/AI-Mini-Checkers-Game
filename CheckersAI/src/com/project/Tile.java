package com.project;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {
    //sets up the tiles on the board
    private Piece piece;

    public void setPiece(Piece piece) {
        this.piece = piece;
    }
    public Piece getPiece() {
        return piece;
    }

    public Tile(boolean light, int x, int y) {
        setWidth(MiniCheckers.TILE_SIZE);
        setHeight(MiniCheckers.TILE_SIZE);

        relocate(x * MiniCheckers.TILE_SIZE, y * MiniCheckers.TILE_SIZE);

        setFill(light ? Color.WHITE : Color.GRAY);
    }
}
