package com.mygdx.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.scenes.Hud;
import com.mygdx.game.sprites.Goomba;
import com.mygdx.game.sprites.Mario;
import com.mygdx.game.tools.B2WorldCreator;
import com.mygdx.game.tools.WorldContactListener;

import org.w3c.dom.Text;

public class PlayScreen implements Screen {

    private MyGdxGame myGdxGame;
    private TextureAtlas atlas;

    private OrthographicCamera gamecam;
    private Viewport viewport;
    private Hud hud;
    private Mario player;// Mario class object
    private Goomba goomba;

    // tiled map var
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer render;

    // box2d
    private World world;
    private Box2DDebugRenderer b2dr;

    //music & sound
    private Music music;

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

        new B2WorldCreator(this);

        music = MyGdxGame.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.play();

        goomba = new Goomba(this, 5.64f, .16f);
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void handleInput(float dt) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2) {
            player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2) {
            player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
        }
    }

    public void update(float dt) {
        handleInput(dt);

        world.step(1/60f, 6, 2);

        player.update(dt);
        goomba.update(dt);
        hud.update(dt);

        gamecam.position.x = player.b2body.getPosition().x;

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
        goomba.draw(myGdxGame.batch);
        myGdxGame.batch.end();

        myGdxGame.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
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
}
