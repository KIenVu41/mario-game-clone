package com.mygdx.game.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.scenes.Hud;

public class Brick extends InteractiveTileObject{
    public Brick(World world, TiledMap map, Rectangle bounds) {
        super(world, map, bounds);
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
