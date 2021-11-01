package xyz.grantlmul.xbfl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LoginController {
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
                    IOUtils.write(body, connection.getOutputStream(), StandardCharsets.UTF_8);
                    String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                    return JsonParser.parseString(responseBody).getAsJsonObject();
                }
                @Override
                public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                    System.out.println("server handling >" + target + "<");
                    if (target.equals("/auth")) {
                        System.out.println("baseRequest >" + baseRequest.getQueryParameters() + "<");
                        String code = baseRequest.getParameter("code");
                        response.getWriter().write("You can now close this tab and return to XBF Launcher, you will shortly be signed in.");
                        // ms auth
                        String ms_access_token;
                        {
                            HttpClient httpClient = HttpClient.newHttpClient();
                            HttpPost httpPost = new HttpPost("https://login.live.com/oauth20_token.srf");
                            String body = "client_id=" + App.authConf.get("client_id").getAsString() +
                                    "&client_secret=" + App.authConf.get("client_secret").getAsString() +
                                    "&code=" + code +
                                    "&grant_type=authorization_code" +
                                    "&redirect_uri=" + App.REDIRECT_URI;
                            HttpRequest httpRequest = HttpRequest
                                    .newBuilder(URI.create(""))
                                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                            HttpResponse re = null;
                            try {
                                re = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                response.sendError(500, e.toString());
                            }
                            JsonObject responseJson = JsonParser.parseString((String) re.body()).getAsJsonObject();
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
                            xbl_token = r.get("Token").getAsString();
                            xbl_hash = r.get("DisplayClaims.xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
                        }

                        // xsts auth
                        String xsts_token = "xsts_token";
                        {
                            JsonObject r = jsonRequest("", "{\"Properties\": {\"SandboxId\": \"RETAIL\",\"UserTokens\": [\""+xbl_token+"\" // from above]},\"RelyingParty\": \"rp://api.minecraftservices.com/\",\"TokenType\": \"JWT\"}", "POST");
                            if (r.has("Err")) {
                                App.showError("Error signing in to Xbox:\n" + r.get("Message").getAsString());
                                try {
                                    die();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                xsts_token = r.get("Token").getAsString();
                                return;
                            }
                        }

                        // minecraft auth
                        String access_token;
                        {
                            JsonObject r = jsonRequest("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"XBL3.0 x="+xbl_hash+";"+xsts_token+"\"}", "POST");
                            access_token = r.get("access_token").getAsString();
                        }

                        // ownership verification
                        {
                            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.minecraftservices.com/entitlements/mcstore").openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("Authorization", "Bearer " + access_token);
                            String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                            if (responseJson.get("items").getAsJsonArray().size() < 2) {
                                App.showError("This Microsoft account does not have a Minecraft: Java Edition account connected to it.");
                                try {
                                    die();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // get account data
                        JsonObject accountData;
                        {
                            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("Authorization", "Bearer " + access_token);
                            String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                            accountData = JsonParser.parseString(responseBody).getAsJsonObject();
                        }

                        // save account data
                        accountData.addProperty("access_token", access_token);
                        File dataFile = new File(App.dataDir(), "accountdata.json");
                        IOUtils.write(accountData.toString(), new FileWriter(dataFile));
                        System.out.println("gaming as " + accountData.get("name").getAsString());
                        App.showInfo("Signed in as user " + accountData.get("name").getAsString() + "successfully!");

                        try {
                            die();
                        } catch (Exception e) {
                            e.printStackTrace();
                            App.showError("I tried killing myself, but I just won't die!");
                        }
                    }
                    baseRequest.setHandled(true);
                }
            });
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
                App.showError("Something fucked up while trying to set things up for logging in.");
            }
        }
        public void die() throws Exception {
            server.stop();
        }
    }

}
