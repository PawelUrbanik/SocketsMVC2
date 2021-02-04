package clientFx.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

public class ChatController implements AutoCloseable {


    @FXML
    private void initialize() {
        String welcome = "Nice to see you there!This is a welcome message. " +
                "Say hello to other users.";
        messagesLayout = Jsoup.parse(
                "<html><head><meta charset='UTF-8'>" +
                        "</head><body><ul><li class=\"welcome\"><div class=\"message\"><div class=\"content\">" +
                        welcome +
                        "</div></div></li></ul></body></html>",
                "UTF-16",
                Parser.xmlParser()
        );
        webViewMessages.getEngine().loadContent(messagesLayout.html());
        webViewMessages.getEngine().setUserStyleSheetLocation(
                getClass().getResource("/clientFx/view/application.css").toString());
    }

    @FXML
    TextField messageTextField;
    @FXML
    Label welcomeLabel;
    @FXML
    WebView webViewMessages;
    @FXML
    Circle circleImage;
    @FXML
    ImageView sendImageView;
    private String userName = ""; // nazwa wybrana przez użytkownika
    private String senderName; // nazwa nadawcy wiadomości
    private String host ; // adres serwera
    private int port; //nr_portu
    private Socket socket; // obiekt gniazda
    private BufferedReader inputBufferedReader; // bufor wejściowy (dane odebrane z serwera)
    private PrintWriter outputPrintWriter; // bufor wyjściowy (dane do wysłania)
    private final int PROTOCOL_PREFIX_LENGTH = 3; // długość słów kluczowych komunikatów
    private Document messagesLayout;
    Task<Void> task;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        welcomeLabel.setText("Hello " + this.userName + "!");
        Image myImage = new Image(getClass().getResource("/clientFx/res/harveyspecter.png").toExternalForm(), false);
        ImagePattern pattern = new ImagePattern(myImage);
        circleImage.setFill(pattern);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String receiveMessage() throws IOException { return inputBufferedReader.readLine(); }

    public void run() throws IOException {
        socket = new Socket(host, port);
        inputBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputPrintWriter = new PrintWriter(socket.getOutputStream(), true);
        sendMessage(userName);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    while (true)
                    {
                        if (isCancelled()) return null;
                        String msg = receiveMessage();
                        showMessage(toHTML(decodeUID(msg), "response"));
                        System.out.println(msg);
                        Thread.sleep(100);
                    }
                }catch (IOException | InterruptedException ex)
                {
                    if (isCancelled()) return null;
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private Element toHTML(String message, String msgClass) {
        System.out.println("to HTML: "+ message);

        Element wrapper = new Element("li").attr("class", msgClass);
        Element image = new Element("img").attr("class", "avatar");
        if (msgClass.equals("response")){
                image.attr("src", new File(getClass().getClassLoader().getResource("/clientFx/res/mikeross.png").getFile()).toURI().toString());
                new Element("span").attr("class", "author").append(senderName).appendTo(wrapper);
        }

        if (msgClass.equals("request"))
        {
            image.attr("src", new File(getClass().getClassLoader().getResource("clientFx/res/harveyspecter.png").getFile()).toURI().toString());
            System.out.println(senderName);
            new Element("span").attr("class", "author").append(userName).appendTo(wrapper);
        }

        image.appendTo(wrapper);
        Element message_div = new Element("div").attr("class", "message").appendTo(wrapper);
        new Element("div").attr("class", "content").append(message).appendTo(message_div);
        return wrapper;
    }

    private String decodeUID(String msg) {
        msg = msg.substring(PROTOCOL_PREFIX_LENGTH);
        char sep = (char) 31;
        String[] param = msg.split(String.valueOf(sep));
        senderName = param[0];
        return msg.substring(param[0].length() +1);
    }

    private void sendMessage(String message) {
        outputPrintWriter.println(message);
    }

    private void showMessage(Element message){
        Element wrapper = messagesLayout.getElementsByTag("ul").first();
        wrapper.appendChild(message);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webViewMessages.getEngine().loadContent(messagesLayout.html());
            }
        });
    }


    @FXML
    void sendImageView_MouseReleased() {
        if(messageTextField.getLength()==0) return;
        sendMessage(messageTextField.getText());
        showMessage(toHTML(messageTextField.getText(), "request"));
        messageTextField.clear();
    }
    @FXML
    void messageTextField_KeyPressed(KeyEvent event) {
        if (event.getCode()== KeyCode.ENTER)
        {
            sendImageView_MouseReleased();
        }
    }


    @Override
    public void close() throws Exception {
        if (socket!= null)
        {
            socket.close();

        }
        if (task!= null) task.cancel();
    }
}
