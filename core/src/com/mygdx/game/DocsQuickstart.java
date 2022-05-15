package com.mygdx.game;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;
import com.mygdx.game.codesponge.ClassInstance;
import com.mygdx.game.codesponge.CodeSponge;
import com.mygdx.game.codesponge.MethodInstance;
import com.mygdx.game.codesponge.VariableInstance;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.List;

/* class to demonstarte use of Docs get documents API */
public /* this is a comment badly placed */ class DocsQuickstart { //this is a comment on a class //this is another comment in a comment
    /** Application name. */
    private static final String APPLICATION_NAME = "Google Docs API Java Quickstart";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String DOCUMENT_ID = "195j9eDD3ccgjQRttHhJPymLJUCOUjs-jmwTrekvdjFE";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final
    List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static int number_of_requests = 0;

    private static final int MAX_REQUESTS_PER_MINUTE = 500;

    private static Queue<Request> requests_queue = new LinkedList<>();

    private static MainScreen mainScreen;

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.

        System.out.println("This is a quote \"this is a quote inside a quote\"");

        InputStream in = DocsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static String createFullDoc(ArrayList<ClassInstance> classes, CodeSponge.Settings settings, MainScreen mainScreen2) throws IOException, GeneralSecurityException {
        mainScreen = mainScreen2;

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        //creates the document
        String docId = createDoc(service);

        for (ClassInstance c : classes) {
            //loops through every class and creates two tables, one for the methods
            //and one for the attributes

            createEntireTableTemplate(docId, service, c, settings);
        }

        return docId;
    }

    public static void add_requests(List<Request> requests, Docs service, String docID) throws IOException {
        number_of_requests += requests.size();

        if (number_of_requests > 500) {
            System.out.println("Sleeping for a minute");
        }

        if (number_of_requests >= MAX_REQUESTS_PER_MINUTE) {
            System.out.println("Sleeping for 60 seconds");
            mainScreen.updateCurrentAction("request quota \n exceeded.\nSleeping for 60 seconds");
            long current_time = System.currentTimeMillis();
            while (number_of_requests >= MAX_REQUESTS_PER_MINUTE) {
                float count_down = 60 - (Math.round(((System.currentTimeMillis() - current_time) / 1000f) * 100f) / 100f);
                mainScreen.updateCurrentAction("request quota \nexceeded.\nSleeping for " + (count_down) +"\nseconds");

                if (System.currentTimeMillis() - current_time > 60000) {
                    System.out.println("Exceeded max requests per minute");
                    number_of_requests = 0;
                }
            }
        }
        System.out.println(" this is the request count " + number_of_requests);
        mainScreen.updateCurrentAction("Updating doc \nRequests: " + number_of_requests);

        BatchUpdateDocumentRequest batchUpdateRequest = new BatchUpdateDocumentRequest().setRequests(requests);
        BatchUpdateDocumentResponse batchUpdateResponse = service.documents().batchUpdate(docID, batchUpdateRequest).execute();
    }

    private static String createDoc(Docs service) throws IOException {
        Document doc = new Document()
                .setTitle("Code Viper Class Tables");
        doc = service.documents().create(doc)
                .execute();
        System.out.println(doc.getDocumentId() + " created.");

        System.out.println("link: https://docs.google.com/document/d/" + doc.getDocumentId() + "/edit");

        return doc.getDocumentId();
    }

    public static void createEntireTableTemplate(String docID, Docs service, ClassInstance cI, CodeSponge.Settings settings) throws IOException {
        createClassVariablesTableTemplate(docID, service, cI);
        createClassMethodsTableTemplate(docID, service, cI, settings);
    }

    public static void createClassVariablesTableTemplate(String docID, Docs service, ClassInstance cI) throws IOException {
        List<Request> requests = new ArrayList<>();

        int colCount = 3;
        int tableAddIndex = 1;

        //Create the table
        requests.add(new Request().setInsertTable(new InsertTableRequest()
                .setRows(cI.variables.size() + 1)
                .setColumns(colCount)
                .setLocation(new Location().setIndex(tableAddIndex).setSegmentId(""))));


        //send the requests to the API
        add_requests(requests, service, docID);

        //get the updated document
//        Document doc = service.documents().get(docID).execute();

        //clear requests
        requests.clear();

        //calculate the indices
        int tableIndex = tableAddIndex + 1;
        int cellOneIndex = tableIndex + 3;

        //The google docs api recommends a method of requests called 'writting backwards'
        //because the indexes increment when something is added it makes adding things in order
        //more difficult as if you add 'hello' to index 1 and then try to add 'goodbye' to index 2
        //it will create 'hgoodbyeello' as the next index should instead be 1 + len('hello') = 6
        //this problem is exacerbated when using tables and so instead you add to the end of the table
        //first and then move backwards. I do not like this method.

        //add the variables to the table
        for (int i = cI.variables.size() - 1; i > -1 ; i--) {
            VariableInstance vI = cI.variables.get(i);
            requests.addAll(addVariableToTable(vI, cellOneIndex, i + 2, colCount));
        }

        //add the headers
        ArrayList<String> headings = new ArrayList<>(
                List.of(new String[] {"Data", "Type", "Comment"}));

        requests.addAll(setupColumnHeadings(cellOneIndex, 1, colCount, headings));

        //send new requests
        add_requests(requests, service, docID);
    }

    public static void createClassMethodsTableTemplate(String docID, Docs service, ClassInstance cI, CodeSponge.Settings settings) throws IOException {
        List<Request> requests = new ArrayList<>();

        ArrayList<String> headings = new ArrayList<>(
                List.of(new String[]{"Name", "Parameters", "Return Type", "Comment"}));
        if (settings.isShowExceptions()){
            headings.add(3, "Exceptions");
        }

        int colCount = headings.size();

        int tableAddLocation = 1;

        int row_count = cI.methods.size() + 3;
        if (!settings.isShowConstructors()){
            row_count -= cI.number_of_constructors;
        }
        requests.add(new Request().setInsertTable(new InsertTableRequest()
                .setRows(row_count)
                .setColumns(colCount)
                .setLocation(new Location().setIndex(tableAddLocation).setSegmentId(""))));

        add_requests(requests, service, docID);

        Document t = service.documents().get(docID).execute();
        List<StructuralElement> contents = t.getBody().getContent();

        requests.clear();

        int tableIndex = tableAddLocation + 1;
        int cellOneIndex = tableIndex + 3;

        for (int i = cI.methods.size() - 1; i >= 0 ; i--) {
            MethodInstance mI = cI.methods.get(i);
            if (mI.isConstructor()){
                if (!settings.isShowConstructors()){
                    continue;
                }
            }
            int row_index = 4 + i;
            if (!settings.isShowConstructors()){
                row_index -= cI.number_of_constructors;
            }
            requests.addAll(Objects.requireNonNull(addMethodToTable(mI, cellOneIndex, row_index, colCount, settings.isShowExceptions())));
        }

        requests.addAll(Objects.requireNonNull(setupColumnHeadings(cellOneIndex, 3, colCount, headings)));

        requests.add(Objects.requireNonNull(addTextToTable("Methods", cellOneIndex, 2, 1, colCount)));

        requests.addAll(Objects.requireNonNull(mergeCells(contents, 2, 1, 1, 0, colCount)));
        requests.addAll(Objects.requireNonNull(mergeCells(contents, 2, 0, 1, 0, colCount)));

        String classNameHeader = cI.getName() +
                (cI.getInnerTo() != null ? " inner class to " + cI.getInnerTo() : "") +
                (cI.getExtendedClass() != null ? " extends " + cI.getExtendedClass() : "") +
                (cI.getImplementedInterfaces().size() > 0 ? " implements " + String.join(", ", cI.getImplementedInterfaces()) : "");

        requests.addAll(Objects.requireNonNull(addClassName(classNameHeader, 2)));

        add_requests(requests, service, docID);
    }

    public static List<Request> addVariableToTable(VariableInstance vI, int tableIndex, int rowIndex, int colCount){
        List<Request> requests = new ArrayList<>();

        requests.add(addTextToTable(vI.getType(), tableIndex, rowIndex, 2, colCount));

        String text = vI.getName();
        if (vI.getAccessLevel() != null){
            text = vI.getAccessLevel() + " " + text;
        }

        requests.add(addTextToTable(text, tableIndex, rowIndex, 1, colCount));

        return requests;
    }

    public static List<Request> addMethodToTable(MethodInstance mI, int tableIndex, int rowIndex, int colCount, boolean showExceptions){
        List<Request> requests = new ArrayList<>();

        if (showExceptions) {
            requests.addAll(addExceptionsToTable(mI.getExceptions(), tableIndex, rowIndex, colCount));
        }
        requests.add(addTextToTable(mI.getReturnType(), tableIndex, rowIndex, 3, colCount));
        requests.addAll((addMethodParameters(mI, tableIndex, rowIndex, colCount)));
        requests.add(addTextToTable(mI.getAccessLevel() + " " + mI.getName(), tableIndex, rowIndex, 1, colCount));

        return requests;
    }

    public static List<Request> addExceptionsToTable(ArrayList<String> exceptions, int tableIndex, int rowIndex, int colCount){
        List<Request> requests = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        if (exceptions.size() > 0) {
            for (String ex : exceptions) {
                sb.append(ex).append("\n");
            }
        }

        requests.add(addTextToTable(sb.toString(), tableIndex, rowIndex, 4, colCount));

        return requests;
    }

    public static List<Request> addMethodParameters(MethodInstance mI, int tableIndex, int rowIndex, int colCount) {
        List<Request> requests = new ArrayList<>();
        ArrayList<VariableInstance> parameters = mI.getParameters();

        if (parameters.size() <= 0) {
            requests.add(addTextToTable("none", tableIndex, rowIndex, 2, colCount));
            return requests;
        }

        StringBuilder text = new StringBuilder();
        for (VariableInstance v : parameters) {
            if (!Objects.equals(v.getType(), "")){
                text.append(v.getType()).append(" ").append(v.getName());
            } else {
                text.append(v.getName());
            }
            text.append("\n");
        }

        requests.add(addTextToTable(text.toString(), tableIndex, rowIndex, 2, colCount));

        return requests;
    }

    public static List<Request> setupColumnHeadings(int tableIndex, int rowIndex, int colCount, ArrayList<String> headings) {
        List<Request> requests = new ArrayList<>();

        for (int i = colCount - 1; i > -1 ; i--) {
            requests.add(addTextToTable(headings.get(i), tableIndex, rowIndex, i + 1, colCount));
        }

        return requests;
    }

    public static Request addTextToTable(String text, int tableIndex, int rowIndex, int columnIndex, int tableColCount){
        if (text.isEmpty()) {
            return null;
        }
        int totIndex = 2 * (tableColCount * (rowIndex - 1) + (columnIndex - 1)) + (rowIndex - 1) + tableIndex;

        return (new Request().setInsertText(new InsertTextRequest()
                .setLocation(new Location().setSegmentId("").setIndex(totIndex))
                .setText(text)));
    }

    public static List<Request> addClassName(String className, int tableIndex) {
        List<Request> requests = new ArrayList<>();

        InsertTextRequest iTR = new InsertTextRequest().setLocation(new Location().setIndex(tableIndex + 5).setSegmentId("")).setText(className);

        requests.add(new Request().setInsertText(iTR));
        requests.add(new Request().setUpdateTextStyle(
                new UpdateTextStyleRequest()
                        .setRange(new Range().setStartIndex(iTR.getLocation().getIndex()).setEndIndex(iTR.getLocation().getIndex() + className.length()))
                        .setTextStyle(new TextStyle().setFontSize(new Dimension().setMagnitude(20d).setUnit("PT")))
                        .setFields("fontSize")
        ));

        return requests;
    }

    public static ArrayList<StructuralElement> getTables(List<StructuralElement> contents) {
        ArrayList<StructuralElement> tables = new ArrayList<>();
        for (StructuralElement se : contents) {
            Table table = se.getTable();
            if (table != null) {
                tables.add(se);
            }
        }
        return tables;
    }

    public static List<Request> mergeCells(List<StructuralElement> contents, int tableIndex,
                                           int rowLocation, int rowCount,
                                           int columnLocation, int columnCount) {

        ArrayList<StructuralElement> tables = getTables(contents);
        StructuralElement table = null;
        for (StructuralElement se : tables) {
            if (se.getStartIndex() == tableIndex) {
                table = se;
            }
        }

        if (table != null) {
            List<Request> requests = new ArrayList<>();

            requests.add(new Request().setMergeTableCells(new MergeTableCellsRequest()
                    .setTableRange(new TableRange().setColumnSpan(columnCount).setRowSpan(rowCount)
                            .setTableCellLocation(new TableCellLocation().setTableStartLocation(new Location()
                                    .setIndex(table.getStartIndex()).setSegmentId("")).setRowIndex(rowLocation).setColumnIndex(columnLocation)
                            ))));

            return requests;
        }
        return null;
    }

    public static class test extends CodeSponge implements Serializable, Cloneable {
        String helloWorld = " Hello World";

        public test() {
            System.out.println("test");
        }

        @Override
        public test clone() {
            try {
                // TODO: copy mutable state here, so the clone can't change the internals of the original
                return (test) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    public static class testTwo{
        public testTwo(){
            System.out.println("testTwo created!");
        }

        public boolean testTwoMethod(String test){
            return test.equals("testTwo");
        }
    }
}