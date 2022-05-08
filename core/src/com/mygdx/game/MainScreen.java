package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.codesponge.CodeSponge;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class MainScreen implements Screen {

    Stage stage;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Viewport viewport;
    OrthographicCamera camera;

    MyGdxGame game;

    Skin skin;

    Table table;
    Label lotsOfText;
    TextButton urlButton;

    BufferedReader bufferedReader;
    FileReader fileReader;

    String currentAction = "no folder selected";
    String docID = "";

    boolean showExceptions = false;
    boolean showConstructors = false;

    public MainScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);

        batch = new SpriteBatch();

        stage = new Stage(viewport, batch);

        Gdx.input.setInputProcessor(stage);

        shapeRenderer = new ShapeRenderer();

        skin = new Skin(Gdx.files.internal("Skins/sgx-ui.json"));

        stage.addActor(setupScrollPane());

        setupTable();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Color c = Color.GRAY;
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
        stage.act();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
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

    }

    public Label setupTitle(){
        Label title;
        title = new Label("Code Viper", skin);
        title.setPosition(Gdx.graphics.getWidth()/2f - title.getWidth()/2f, Gdx.graphics.getHeight() - title.getHeight());
        stage.addActor(title);
        return title;
    }

    public TextButton setupInputButton(){
        TextButton addInputButton;
        addInputButton = new TextButton("Add Input", skin);
        addInputButton.setPosition(Gdx.graphics.getWidth()/2f - addInputButton.getWidth()/2f, Gdx.graphics.getHeight() - addInputButton.getHeight());
        addInputButton.addListener(event -> {
            if (event instanceof InputEvent){
                InputEvent inputEvent = (InputEvent) event;
                if (inputEvent.getType() == InputEvent.Type.touchDown){
                    String file = getJavaFile();
                    if (file != null){
                        String fileContents = readFile(file);

                        updateLotsOfText(fileContents);
                        String docID = CodeSponge.fragmentCode(fileContents, new CodeSponge.Settings(showConstructors, showExceptions));

                        if (!Objects.equals(docID, "")){
                            this.docID = docID;
                            urlButton.setVisible(true);
                        }
                    }
                }
            }
            return false;
        });
        stage.addActor(addInputButton);
        return addInputButton;
    }

    public TextButton setupURLButton(){
        urlButton = new TextButton("open doc", skin);
        urlButton.setVisible(false);
        urlButton.setSize(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f);
        stage.addActor(urlButton);

        urlButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!Objects.equals(docID, "")){
                    Gdx.net.openURI("https://docs.google.com/document/d/" + docID + "/edit");
                }
            }
        });

        return urlButton;
    }

    public void updateCurrentAction(){
        lotsOfText.setText(currentAction);
    }

    public Label setupLotsOfText(){
        lotsOfText = new Label(currentAction, skin);
        lotsOfText.setWrap(true);
        lotsOfText.setFontScale(0.5f);
        lotsOfText.setPosition(Gdx.graphics.getWidth()/2f - lotsOfText.getWidth()/2f, Gdx.graphics.getHeight() - lotsOfText.getHeight() * 2);
        stage.addActor(lotsOfText);
        return lotsOfText;
    }

    public void updateLotsOfText(String text){
        lotsOfText.setText(text);
    }

    public ScrollPane setupScrollPane(){
        ScrollPane scrollPane;
        scrollPane = new ScrollPane(setupLotsOfText(), skin);
        int offset = 15;
        scrollPane.setSize(Gdx.graphics.getWidth() * 0.65f, Gdx.graphics.getHeight() * 0.9f);
        scrollPane.setPosition(Gdx.graphics.getWidth() - scrollPane.getWidth() - offset, offset);
        stage.addActor(scrollPane);
        return scrollPane;
    }

    public CheckBox setupToggleBox(String text){
        CheckBox toggleBox = new CheckBox(text, skin);
        toggleBox.setSize(Gdx.graphics.getWidth() * 0.25f, Gdx.graphics.getHeight() * 0.1f);
        stage.addActor(toggleBox);

        return toggleBox;
    }

    public void setupTable(){
        table = new Table();
        table.setWidth(stage.getWidth());
        table.align(Align.center | Align.top);

        table.setPosition(0, stage.getHeight());

        Label t = setupTitle();
        table.add(t).padBottom(30);
        table.row();

        table.add(setupInputButton()).expandX().left().padBottom(5);
        table.row();
        table.add(setupURLButton()).expand().left().padBottom(30);
        table.row();

        CheckBox exceptions = setupToggleBox("exceptions");
        exceptions.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showExceptions = exceptions.isChecked();
            }
       });

        table.add(exceptions).expandX().left().padBottom(5);
        table.row();

        CheckBox constructors = setupToggleBox("Show constructors");
        constructors.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showConstructors = constructors.isChecked();
            }
         });

        table.add(constructors).expandX().left();

        table.setDebug(true);
        stage.addActor(table);
    }

    public String getJavaFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("C:\\Users\\jamescoward\\Desktop\\Java\\CodeViperGdx\\core\\src\\com\\mygdx\\game"));
        System.out.println(fileChooser.getCurrentDirectory() + " is the current directory");
        FileFilter filter = new FileNameExtensionFilter("Java file","java");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showOpenDialog(null);

        File fileCollected = fileChooser.getSelectedFile();

        return Objects.equals(String.valueOf(fileCollected), "null") ? null : String.valueOf(fileCollected);
    }

    public String readFile(String fileLocation){
        try {
            fileReader = new FileReader(fileLocation);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }

            bufferedReader.close();
            fileReader.close();

            return stringBuilder.toString();
        } catch (NullPointerException | IOException e){
            System.out.println("No file selected");
        }
        return null;
    }
}
