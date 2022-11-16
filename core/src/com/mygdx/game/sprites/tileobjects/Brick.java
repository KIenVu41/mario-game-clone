package com.mygdx.game.sprites.tileobjects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.scenes.Hud;
import com.mygdx.game.screens.PlayScreen;

public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen screen, MapObject object){
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(MyGdxGame.BRICK_BIT);
    }

    @Override
    public void onHeadHit() {
        setCategoryFilter(MyGdxGame.DESTROYED_BIT);
        getCell().setTile(null);
        Hud.addScore(200);
        MyGdxGame.manager.get("audio/sounds/breakblock.wav", Sound.class).play();

    }
}
