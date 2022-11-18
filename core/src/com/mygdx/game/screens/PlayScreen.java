package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.scenes.Hud;
import com.mygdx.game.sprites.enemies.Enemy;
import com.mygdx.game.sprites.Mario;
import com.mygdx.game.sprites.items.Item;
import com.mygdx.game.sprites.items.ItemDef;
import com.mygdx.game.sprites.items.Mushroom;
import com.mygdx.game.tools.B2WorldCreator;
import com.mygdx.game.tools.WorldContactListener;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class PlayScreen implements Screen {

    private MyGdxGame myGdxGame;
    private TextureAtlas atlas;

    private OrthographicCamera gamecam;
    private Viewport viewport;
    private Hud hud;
    private Mario player;// Mario class object

    // tiled map var
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer render;

    // box2d
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    //music & sound
    private Music music;

    private Array<Item> items;
    private LinkedBlockingDeque<ItemDef> itemsToSpawn;

    public PlayScreen(MyGdxGame myGdxGame) {
        atlas = new TextureAtlas("Mario_and_Enemies.pack");

        this.myGdxGame = myGdxGame;
        world = new World(new Vector2(0, -10), true);
        player = new Mario(this); // initialization of  Mario class object
        world.setContactListener(new WorldContactListener());
        gamecam = new OrthographicCamera();
        viewport = new FillViewport(MyGdxGame.V_WIDTH / MyGdxGame.PPM, MyGdxGame.V_HEIGHT / MyGdxGame.PPM, gamecam);
        hud =new Hud(myGdxGame.batch);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("level1.tmx");
        render = new OrthogonalTiledMapRenderer(map, 1/MyGdxGame.PPM);
        gamecam.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        music = MyGdxGame.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        //music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingDeque<ItemDef>();
    }

    public void spawnItem(ItemDef itemDef) {
        itemsToSpawn.add(itemDef);
    }

    public void handleSpawningItems() {
        if(!itemsToSpawn.isEmpty()) {
            ItemDef itemDef = itemsToSpawn.poll();
            if(itemDef.type == Mushroom.class) {
                items.add(new Mushroom(this, itemDef.position.x, itemDef.position.y));
            }
        }
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void handleInput(float dt) {
        if(player.currentState != Mario.State.DEAD) {
            if(Gdx.input.isKeyPressed(Input.Keys.W)) {
                //player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
                player.jump();
            }
            if(Gdx.input.isKeyPressed(Input.Keys.D) && player.b2body.getLinearVelocity().x <= 2) {
                player.b2body.applyLinearImpulse(new Vector2(2f, 0), player.b2body.getWorldCenter(), true);
            }
            if(Gdx.input.isKeyPressed(Input.Keys.A) && player.b2body.getLinearVelocity().x >= -2) {
                Gdx.app.log("Debug", "a pressed!");
                player.b2body.applyLinearImpulse(new Vector2(-2f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                player.fire();
        }
    }

    public void update(float dt) {
        handleInput(dt);
        handleSpawningItems();

        world.step(1 / 60f, 6, 2);

        player.update(dt);
//        for(Enemy e: creator.getGoombas()) {
//            e.update(dt);
//            if(e.getX() < player.getX() + 224 / MyGdxGame.PPM) {
//                e.b2body.setActive(true);
//            }
//        }

        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if(enemy.getX() < player.getX() + 224 / MyGdxGame.PPM) {
                enemy.b2body.setActive(true);
            }
        }

        for(Item item : items) {
            item.update(dt);
        }

        hud.update(dt);

        // attach gamecam to player.x coordinate
        if(player.currentState != Mario.State.DEAD) {
            gamecam.position.x = player.b2body.getPosition().x;
        }

        gamecam.update();
        render.setView(gamecam);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        // clear game screen with Black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // render game map
        render.render();

        //  render Box2DDebugLines
        b2dr.render(world, gamecam.combined);

        myGdxGame.batch.setProjectionMatrix(gamecam.combined);
        myGdxGame.batch.begin();
        player.draw(myGdxGame.batch);
        for (Enemy enemy : creator.getEnemies())
            enemy.draw(myGdxGame.batch);

        for(Item item : items) {
            item.draw(myGdxGame.batch);
        }
        myGdxGame.batch.end();

        myGdxGame.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()) {
            myGdxGame.setScreen(new GameOverScreen(myGdxGame));
            dispose();
        }
    }

    public boolean gameOver() {
        if(player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);

    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        render.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public Hud getHud(){ return hud; }
}
