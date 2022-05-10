package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class MyGdxGame extends Game {
	SpriteBatch batch;
	Texture img;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		Gdx.graphics.setTitle("Code Viper");

		MainScreen ms = new MainScreen(this);

		System.out.println(FileSystems.getDefault().getPath("").toAbsolutePath());

		setScreen(ms);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
