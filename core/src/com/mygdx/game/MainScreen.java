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
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.codesponge.ClassInstance;
import com.mygdx.game.codesponge.CodeSponge;
import com.mygdx.game.codesponge.CodeSpongeTwo;
import com.mygdx.game.codesponge.MethodInstance;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;

public class MainScreen implements Screen {

    Stage stage;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Viewport viewport;
    OrthographicCamera camera;

    MyGdxGame game;

    Skin skin;

    Table table;
    Label codeLabel;
    TextButton urlButton;
    Label currentActionLabel;
    SelectBox<String> code_select_box;

    BufferedReader bufferedReader;
    FileReader fileReader;

    String docID = "";

    boolean showExceptions = false;
    boolean showConstructors = false;

    boolean isDrawing = false;
    com.badlogic.gdx.utils.Queue<String> code_stack = new com.badlogic.gdx.utils.Queue<>();

    ArrayList<ClassInstance> classes = new ArrayList<>();

    ArrayList<String> filePaths = new ArrayList<>();

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
        try {
            isDrawing = true;
            stage.draw();
            isDrawing = false;
        }catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
            System.out.println("IndexOutOfBoundsException when drawing" +
                    codeLabel.getText());
//            stage.getBatch().end();
        }catch (IllegalStateException e2){
            e2.printStackTrace();
            System.out.println("IllegalStateException when drawing");
        }

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

    public Label setupCurrentActionLabel(){
        currentActionLabel = new Label("no folder selected", skin);
        return currentActionLabel;
    }

    public TextButton setupInputButton(){
        TextButton addInputButton;
        addInputButton = new TextButton("Add Input", skin);
        addInputButton.setPosition(Gdx.graphics.getWidth()/2f - addInputButton.getWidth()/2f, Gdx.graphics.getHeight() - addInputButton.getHeight());
        addInputButton.addListener(event -> {
            if (event instanceof InputEvent){
                InputEvent inputEvent = (InputEvent) event;
                if (inputEvent.getType() == InputEvent.Type.touchDown){
                    String dir = getJavaDir();
                    if (dir != null) {
                        Thread t = new Thread(() -> {
                            updateCurrentAction("locating java files");
                            ArrayList<File> files = (getJavaFiles(dir));
                            Array<String> fileNames = new Array<>();
                            for (File f : files) {
                                fileNames.add(f.getName());
                                this.filePaths.add(f.getPath());
                            }

//                            code_select_box.setItems(fileNames);

                            System.out.println("this is the number of java files: " + files.size());

                            for (File file : files) {
                                updateCurrentAction("reading files");
                                System.out.println("this is the file: " + file.getName() + " and this is the path: " + file.getPath());
                                String fileContents = "";
                                try {
                                    fileContents = readFile(String.valueOf(file));
                                } catch (Exception e) {
                                    System.out.println("this is the exception: " + e);
                                }

                                if (fileContents == null) {
                                    continue;
                                }

                                if (fileContents.isEmpty()) {
                                    continue;
                                }

                                ArrayList<ClassInstance> classes = CodeSpongeTwo.fragment_class(fileContents);

                                for (ClassInstance classInstance : classes) {
                                    System.out.println(classInstance.toString());
                                }

                                this.classes.addAll(classes);

                                updateCodeLabel(fileContents);

                                try {
                                    Thread.sleep(30);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            CodeSponge.Settings settings = new CodeSponge.Settings(showConstructors, showExceptions);

                            int tot_req = calculate_requests(classes, settings);
                            System.out.println("This is the total requests: " + tot_req);

                            try {
                                ;
                                String docID = DocsQuickstart.createFullDoc(classes, settings, this, tot_req);

                                if (!Objects.equals(docID, "")) {
                                    this.docID = docID;
                                    urlButton.setVisible(true);
                                    addInputButton.setVisible(true);
                                }
                                updateCurrentAction("document created");
                            } catch (IOException | GeneralSecurityException e) {
                                e.printStackTrace();
                            }
                        });

                        urlButton.setVisible(false);
                        t.start();
                        addInputButton.setVisible(false);
                    }
                }
            }
            return false;
        });
        stage.addActor(addInputButton);
        return addInputButton;
    }

    public int calculate_requests(ArrayList<ClassInstance> classes, CodeSponge.Settings settings) {
        int numberOfRequests = 0;

        for (ClassInstance c : classes) {
            int method_multiplier = 2 + (settings.isShowConstructors() ? 1 : 0);
            int variable_requests = c.variables.size() * 2;
            numberOfRequests += variable_requests;

            for (MethodInstance m : c.methods) {
                int method_add = m.getParameters().size();
                numberOfRequests += method_add;
            }
            int method_requests = c.methods.size() * method_multiplier;
            numberOfRequests += method_requests;
        }

        int class_add = classes.size() * 13;

        return numberOfRequests + class_add;
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

//    public void

    public void updateCurrentAction(String currentAction){
        if (isDrawing){
            return;
        }
        currentActionLabel.setText(currentAction);
    }

    public Label setupCodeLabel(){
        codeLabel = new Label("code will appear when file loaded", skin);
        codeLabel.setWrap(true);
        codeLabel.setFontScale(0.5f);
        codeLabel.setPosition(Gdx.graphics.getWidth()/2f - codeLabel.getWidth()/2f, Gdx.graphics.getHeight() - codeLabel.getHeight() * 2);
        return codeLabel;
    }

    public void updateCodeLabel(String text){
        System.out.println("updating code label");
        code_stack.addLast(text);
        if (!isDrawing) {
            codeLabel.setText(code_stack.removeFirst());
        }
    }

    public ScrollPane setupScrollPane(){
        ScrollPane scrollPane;
        scrollPane = new ScrollPane(setupCodeLabel(), skin);
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

    public TextButton setupTextButton(String text, Runnable runnable){
        TextButton button = new TextButton(text, skin);
        button.setSize(Gdx.graphics.getWidth() * 0.25f, Gdx.graphics.getHeight() * 0.1f);
        button.pack();
        button.setPosition(0, 0);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                runnable.run();
            }
       });
        return button;
    }

    public void setupTable(){
        table = new Table();
        table.setWidth(stage.getWidth());
        table.align(Align.center | Align.top);

        table.setPosition(0, stage.getHeight());

        table.add(setupTitle()).padBottom(30);
        table.row();

        table.add(setupCurrentActionLabel()).expandX().left().padBottom(10);
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
        table.row();

        stage.addActor(setupTextButton("Delete Credentials", this::deleteCreds));

        stage.addActor(table);
    }

    public SelectBox<String> setup_code_list(){
        SelectBox<String> code_list = new SelectBox<>(skin);
        code_list.setPosition(100, 50);
        code_list.setItems("test", "test2");
        this.code_select_box = code_list;
        return code_list;
    }

    public void deleteCreds(){
        Path absolutePath = FileSystems.getDefault().getPath("").toAbsolutePath();

        File file = new File(absolutePath + "/tokens/StoredCredential");
        System.out.println(file.getAbsolutePath());
        if (file.delete()) {
            System.out.println("File deleted successfully");
        } else {
            System.out.println("Failed to delete file");
        }
    }

    public String getJavaDir(){
        JFileChooser fileChooser = new JFileChooser(){
            public void approveSelection() {
                if (getSelectedFile().isFile()) {
                    super.approveSelection();
                } else
                    super.approveSelection();
            }
        };
        fileChooser.setCurrentDirectory(new File("C:\\Users\\jamescoward\\Desktop\\Java\\MrWilordFour\\core\\src\\com\\mygdx\\game\\Screens\\GameScreen.java"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.showDialog(new JList<>(), "Select a directory");

        File fileCollected = fileChooser.getSelectedFile();

        if (fileCollected != null) {
            System.out.println("this is the selected dir : " + fileCollected.getAbsolutePath());
        }
        return Objects.equals(String.valueOf(fileCollected), "null") ? null : String.valueOf(fileCollected);
    }

    public ArrayList<File> getJavaFiles(String dir){
        ArrayList<File> files_to_check = new ArrayList<>();
        File file = new File(dir);

        if (file.isFile()) {
            ArrayList<File> out = new ArrayList<>();
            out.add(file);
            return out;
        }

        try {
            files_to_check = new ArrayList<>(Arrays.asList(Objects.requireNonNull(file.listFiles())));
        }catch (NullPointerException e){
            System.out.println("No files found in directory");
        }


        ArrayList<File> output = new ArrayList<>();

        while (!files_to_check.isEmpty()) {
            File f = files_to_check.get(0);
            files_to_check.remove(0);
            if (f.getName().endsWith(".jar")){
                System.out.println("Found a jar file : " + f.getAbsolutePath());
            }

            if (checkFileType(f, ".java")) {
                output.add(f);
                continue;
            }

            File[] child_files = f.listFiles();
            if (child_files != null) {
                for (File child : child_files) {
                    if (child.isDirectory()){
                        output.addAll(getJavaFiles(child.getAbsolutePath()));
                    }else{
                        files_to_check.add(child);
                    }
                }
            }
        }

        return output;
    }

    public boolean checkFileType(File f, String type){
        return f.getName().endsWith(type);
    }

    public void updateFileContents(int file_index) {
        String content = readFile(this.filePaths.get(file_index));

        updateCodeLabel(content);
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
