package xyz.grantlmul.xbfl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import xyz.grantlmul.xbfl.auth.Minecraft;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    @FXML TextField usernameField;
    @FXML PasswordField passwordField;
    @FXML Button msButton;

    public void initialize(Window window) {
        window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::exitApplication);
    }

    public void handleRegisterClick(MouseEvent mouseEvent) {

    }

    public void handleLoginClick(MouseEvent mouseEvent) {
    }

    private class ServerRunnable implements Runnable {
        Server server;
        @Override
        public void run() {
            System.out.println("running server");
            QueuedThreadPool threadPool = new QueuedThreadPool();
            server = new Server(threadPool);
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(54321);
            server.addConnector(connector);
            server.setHandler(new AbstractHandler() {
                JsonObject jsonRequest(String url, String body, String requestMethod) throws IOException {
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestMethod(requestMethod);
                    connection.setDoOutput(true);
                    IOUtils.write(body, connection.getOutputStream(), StandardCharsets.UTF_8);
                    String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                    return JsonParser.parseString(responseBody).getAsJsonObject();
                }
                @Override
                public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                    System.out.println("server handling >" + target + "<");
                    if (target.equals("/auth")) {
                        baseRequest.setHandled(true);
                        System.out.println("baseRequest >" + baseRequest.getQueryParameters() + "<");
                        String code = baseRequest.getParameter("code");
                        response.getWriter().write("You can now close this tab and return to XBF Launcher, you will shortly be signed in.");
                        // ms auth
                        String ms_access_token;
                        {
                            CloseableHttpClient httpClient = HttpClients.createDefault();
                            HttpPost httpPost = new HttpPost("https://login.live.com/oauth20_token.srf");
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("client_id", App.authConf.get("client_id").getAsString()));
                            //params.add(new BasicNameValuePair("client_secret", App.authConf.get("client_secret").getAsString()));
                            params.add(new BasicNameValuePair("code", code));
                            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                            params.add(new BasicNameValuePair("redirect_uri", App.REDIRECT_URI));
                            httpPost.setEntity(new UrlEncodedFormEntity(params));
                            CloseableHttpResponse re = httpClient.execute(httpPost);
                            JsonObject responseJson = JsonParser.parseString(new String(re.getEntity().getContent().readAllBytes())).getAsJsonObject();
                            System.out.println(responseJson);
                            ms_access_token = responseJson.get("access_token").getAsString();
                        }
                        // xbox auth
                        String xbl_token;
                        String xbl_hash;
                        {
                            JsonObject r = jsonRequest(
                                    "https://user.auth.xboxlive.com/user/authenticate",
                                    "{\"Properties\":{\"AuthMethod\": \"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d="+ms_access_token+"\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}",
                                    "POST");
                            System.out.println(r);
                            xbl_token = r.get("Token").getAsString();
                            xbl_hash = r.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
                        }

                        // xsts auth
                        String xsts_token = null;
                        {
                            JsonObject r = jsonRequest("https://xsts.auth.xboxlive.com/xsts/authorize", "{\"Properties\": {\"SandboxId\": \"RETAIL\",\"UserTokens\": [\""+xbl_token+"\"]},\"RelyingParty\": \"rp://api.minecraftservices.com/\",\"TokenType\": \"JWT\"}", "POST");
                            System.out.println(r);
                            if (r.has("Err")) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setContentText(r.get("Message").getAsString());
                                    alert.show();
                                });
                                try {
                                    die();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                xsts_token = r.get("Token").getAsString();
                            }
                        }

                        // minecraft auth
                        String access_token;
                        {
                            JsonObject r = jsonRequest("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"XBL3.0 x="+xbl_hash+";"+xsts_token+"\"}", "POST");
                            System.out.println(r);
                            access_token = r.get("access_token").getAsString();
                            System.out.println("mcauthed");
                        }

                        // ownership verification
                        {
                            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.minecraftservices.com/entitlements/mcstore").openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("Authorization", "Bearer " + access_token);
                            String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                            System.out.println(responseBody);
                            if (responseJson.get("items").getAsJsonArray().size() < 2) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setContentText("This Microsoft account does not have a Minecraft: Java Edition account connected to it.");
                                    alert.show();
                                });
                                try {
                                    die();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                runningServer = false;
                                msButton.setText("Microsoft Log In");
                                listenerThread.stop();
                                return;
                            }
                            System.out.println("owning");
                        }

                        // get account data
                        JsonObject accountData;
                        {
                            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("Authorization", "Bearer " + access_token);
                            String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                            System.out.println(responseBody);
                            accountData = JsonParser.parseString(responseBody).getAsJsonObject();
                            System.out.println("data");
                        }

                        // save account data
                        accountData.addProperty("access_token", access_token);
                        File dataFile = new File(App.dataDir(), "accountdata.json");
                        dataFile.delete();
                        FileUtils.writeStringToFile(dataFile, accountData.toString(), StandardCharsets.UTF_8);
                        Minecraft.userData = accountData;
                        System.out.println("gaming as " + accountData.get("name").getAsString());
                        Platform.runLater(() -> {
                            Parent parent = null;
                            try {
                                parent = FXMLLoader.load(getClass().getResource("home.fxml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Scene scene = new Scene(parent, 884, 541);
                            scene.getStylesheets().clear();
                            scene.getStylesheets().add(getClass().getResource("styles.css").toString());
                            App.getMainStage().setScene(scene);
                        });

                        Platform.runLater(() -> {
                            try {
                                runningServer = false;
                                die();
                                listenerThread.stop();
                                msButton.setText("Microsoft Log In");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setContentText("I tried killing myself, but I just won't die!");
                                alert.show();
                            }
                        });
                    }
                }
            });
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something fucked up while trying to set things up for logging in.");
                alert.show();
            }
        }
        public void die() throws Exception {
            server.stop();
        }
    }
    ServerRunnable runner = new ServerRunnable();
    Thread listenerThread;
    boolean runningServer = false;

    @FXML
    public void exitApplication(WindowEvent event) {
        if (runningServer) {
            try {
                runner.die();
            } catch (Exception e) {
                e.printStackTrace();
            }
            listenerThread.stop();
            msButton.setText("Microsoft Log In");
        }
    }

    public void handleMicrosoftClick(MouseEvent mouseEvent) throws Exception {
        listenerThread = new Thread(runner);
        if (runningServer) {
            runningServer = false;
            runner.die();
            listenerThread.stop();
            msButton.setText("Microsoft Log In");
        } else {
            Desktop.getDesktop().browse(App.OAUTH_URL.toURI());
            runningServer = true;
            listenerThread.start();
            msButton.setText("Cancel Microsoft Login");
        }
    }

}
