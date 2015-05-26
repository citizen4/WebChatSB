package kc87.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import kc87.service.SessionService;
import kc87.web.protocol.ChatMsg;
import kc87.web.protocol.LoginMsg;
import kc87.web.protocol.Message;
import kc87.web.protocol.ResultMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


@ServerEndpoint(value = "/ws", configurator = WsChatServer.Config.class)
@SuppressWarnings("unused")
public class WsChatServer implements ApplicationContextAware {
   private static final Logger LOG = LogManager.getLogger(WsChatServer.class);
   private static final int IDLE_TIMEOUT_SEC = 60;
   private static final String[] PEER_COLORS = {"#38F", "#f00", "#ff0", "#f08", "#0ff",
         "#888", "#8ff", "#f80", "#ff4", "#fff"};
   private static final int PEER_COLOR_NB = PEER_COLORS.length;
   private static final AbstractMap<String, String> userColorMap = new ConcurrentHashMap<>();
   private static AtomicInteger usersLoggedIn = new AtomicInteger(0);
   private static ApplicationContext appContext = null;
   //private static AccountService accountService = null;
   private static SessionService sessionService = null;
   //private static SessionRegistry sessionRegistry = null;
   private Session thisSession = null;
   private HttpSession httpSession = null;
   private SecurityContext securityContext = null;
   private long lastActivityTime = 0;
   private int defaultSessionTimeout = 0;
   private final Consumer<String> callback = this::sessionDestroyed;
   private volatile int fooBar = 0;


   @PostConstruct
   private void init() {
      LOG.debug("@PostConstruct");
      sessionService = appContext.getBean(SessionService.class);
   }

   @OnOpen
   public void onOpen(Session session) {
      LOG.debug("onOpen(): " + session.getId());
      thisSession = session;
      httpSession = (HttpSession) thisSession.getUserProperties().get("httpSession");

      LOG.debug("fooBar: " + (fooBar++));

      if(httpSession == null || httpSession.getAttribute("wsSession") != null){
         activeClose(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Message from unauthorized peer"));
         return;
      }

      httpSession.setAttribute("wsSession",thisSession);

      thisSession.setMaxIdleTimeout(IDLE_TIMEOUT_SEC * 1000);
      thisSession.getUserProperties().clear();

      LOG.debug("Open sessions: "+session.getOpenSessions().size());
      LOG.debug("Creation time: " + httpSession.getCreationTime());
      LOG.debug("Last access time: " + httpSession.getLastAccessedTime());
      LOG.debug("Current time: " +System.currentTimeMillis());


      /*
      defaultSessionTimeout = httpSession.getMaxInactiveInterval();
      httpSession.setMaxInactiveInterval(0);
      lastActivityTime = System.currentTimeMillis();
      sessionService.addOnSessionDestroyedListener(callback);
      */

      enterChat();
   }

   @OnMessage
   public void onTextMsg(String jsonStr) {
      Gson gson = new GsonBuilder().serializeNulls().create();

      try {
         jsonStr = jsonStr.trim();
         LOG.debug("Rcv.:" + jsonStr + " from: " + thisSession.getId());
         Message clientMsg = gson.fromJson(jsonStr, Message.class);

         if (clientMsg.TYPE.equals("ACCOUNT")) {
            //startChat();
            //handleAccount(clientMsg);
         }

         //checkHttpSession();
         validateUserSession();

         if (clientMsg.TYPE.equals("PING")) {
            sendPong();
         }

         if (clientMsg.TYPE.equals("CHAT")) {
            handleChat(clientMsg);
         }

      } catch (JsonSyntaxException e) {
         LOG.error(e);
      }
   }

   @OnClose
   public void onClose() {
      LOG.debug("onClose(): " + thisSession.getId());

      int sessionIdleTime = (int)((System.currentTimeMillis() - httpSession.getLastAccessedTime()) / 1000);

      LOG.debug("Max idle timeout: " + (sessionIdleTime + defaultSessionTimeout));

      sessionService.removeOnSessionDestroyedListener(callback);
      httpSession.removeAttribute("wsSession");
      httpSession.setMaxInactiveInterval(sessionIdleTime + defaultSessionTimeout);

      // Handle close without logout (peer went down due to network trouble etc.)
      if (thisSession.getUserProperties().containsKey("USER")) {
         //logoutUser();
         leaveChat();
      }
   }


   @Override
   public void setApplicationContext(ApplicationContext ctx) throws BeansException {
      appContext = ctx;
   }

   private void enterChat() {
      String userColor;
      int userNb = usersLoggedIn.incrementAndGet();
      Message serverMsg = new Message();
      LoginMsg loginMsg = new LoginMsg();
      ResultMsg resultMsg = new ResultMsg();
      Message joinMsg = new Message();

      UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)thisSession.getUserPrincipal();
      User user = (User)token.getPrincipal();
      LOG.debug("startChat() user: " + user.toString());


      defaultSessionTimeout = httpSession.getMaxInactiveInterval();
      httpSession.setMaxInactiveInterval(0);
      lastActivityTime = System.currentTimeMillis();
      sessionService.addOnSessionDestroyedListener(callback);


      thisSession.getUserProperties().put("USER", user.getUsername());
      // If a user is active more than once, give him the same color:
      if (userColorMap.containsKey(user.getUsername())) {
         userColor = userColorMap.get(user.getUsername());
      } else {
         userColor = PEER_COLORS[userNb % PEER_COLOR_NB];
         userColorMap.put(user.getUsername(), userColor);
      }

      thisSession.getUserProperties().put("COLOR", userColor);

      loginMsg.USER = user.getUsername();

      serverMsg.TYPE = "ACCOUNT";
      serverMsg.SUBTYPE = "LOGIN";
      serverMsg.USER_LIST = buildUserList(true);
      serverMsg.LOGIN_MSG = loginMsg;
      serverMsg.STATS_MSG = usersLoggedIn.get() + " User" + (usersLoggedIn.get() > 1 ? "s " : " ") + "online!";

      resultMsg.CODE = "OK";
      resultMsg.MSG = "Entered successful!";

      serverMsg.RESULT_MSG = resultMsg;

      sendMessage(serverMsg);

      joinMsg.TYPE = "INFO";
      joinMsg.SUBTYPE = "JOIN";
      joinMsg.INFO_MSG = user.getUsername() + " has entered the building";
      joinMsg.STATS_MSG = userNb + " User" + (userNb > 1 ? "s " : " ") + "online!";
      joinMsg.USER_LIST = buildUserList(true);

      broadcastMessage(joinMsg, false);
   }


   private void leaveChat() {
      ResultMsg resultMsg = new ResultMsg();

      LOG.debug("leaveChat(): " + thisSession.getUserProperties().get("USER"));

      int userNb = usersLoggedIn.decrementAndGet();

      Message unjoinMsg = new Message();

      unjoinMsg.TYPE = "INFO";
      unjoinMsg.SUBTYPE = "JOIN";
      unjoinMsg.INFO_MSG = thisSession.getUserProperties().get("USER") + " has left the building";
      unjoinMsg.STATS_MSG = userNb + " User" + (userNb > 1 ? "s " : " ") + "online!";
      unjoinMsg.USER_LIST = buildUserList(false);

      thisSession.getUserProperties().clear();

      broadcastMessage(unjoinMsg, false);
   }

   /*
   private void handleAccount(final Message clientMsg) {
      Message serverMsg = new Message();
      serverMsg.TYPE = "ACCOUNT";

      if (clientMsg.SUBTYPE.equals("LOGIN")) {
         serverMsg.SUBTYPE = "LOGIN";
         serverMsg.RESULT_MSG = loginUser(clientMsg.LOGIN_MSG.USER, clientMsg.LOGIN_MSG.PASSWD);

         if (thisSession.getUserProperties().containsKey("USER")) {
            LoginMsg loginMsg = new LoginMsg();
            loginMsg.USER = (String) thisSession.getUserProperties().get("USER");
            serverMsg.USER_LIST = buildUserList(true);
            serverMsg.LOGIN_MSG = loginMsg;
            serverMsg.STATS_MSG = usersLoggedIn.get() + " User" + (usersLoggedIn.get() > 1 ? "s " : " ") + "online!";
         }
      }

      if (clientMsg.SUBTYPE.equals("LOGOUT")) {
         serverMsg.SUBTYPE = "LOGOUT";
         serverMsg.RESULT_MSG = logoutUser();
      }

      sendMessage(serverMsg);
   }*/


   private void handleChat(final Message clientMsg) {
      Message broadcastMsg;

      if (clientMsg.SUBTYPE.equals("MSG")) {

         lastActivityTime = System.currentTimeMillis();

         ChatMsg chatMsg = new ChatMsg();
         broadcastMsg = clientMsg;
         // You can't trust nobody ;)
         chatMsg.MSG = clientMsg.CHAT_MSG.MSG.replace("<", "&lt;").replace("&", "&amp;");
         chatMsg.COLOR = (String) thisSession.getUserProperties().get("COLOR");
         chatMsg.FROM = (String) thisSession.getUserProperties().get("USER");
         broadcastMsg.CHAT_MSG = chatMsg;
         broadcastMessage(broadcastMsg, true);
      }
   }


   private void sendPong() {

      if(System.currentTimeMillis() - lastActivityTime > 300 * 1000){
         httpSession.invalidate();
         return;
      }

      Message pongMsg = new Message();
      pongMsg.TYPE = "PONG";
      sendMessage(pongMsg);
   }


   /*
   private ResultMsg loginUser(final String username, final String password) {
      LOG.debug("LOGIN User: " + username + " PW: " + password);
      ResultMsg resultMsg = new ResultMsg();

      resultMsg.CODE = "ERR";

      for (Session s : thisSession.getOpenSessions()) {
         if (s.getUserProperties().containsKey("USER")) {
            String sessionUserName = (String) (s.getUserProperties().get("USER"));
            if (sessionUserName.equalsIgnoreCase(username)) {
               resultMsg.MSG = "You are already logged in!";
               return resultMsg;
            }
         }
      }

      Account account = accountService.checkAccount(username, password);

      if (account != null) {

         String userColor;
         int userNb = usersLoggedIn.incrementAndGet();

         resultMsg.CODE = "OK";
         resultMsg.MSG = "Login successful!";

         thisSession.getUserProperties().put("USER", account.getUsername());

         // If a user is active more than once, give him the same color:
         if (userColorMap.containsKey(account.getUsername())) {
            userColor = userColorMap.get(account.getUsername());
         } else {
            userColor = PEER_COLORS[account.getId().intValue() % PEER_COLOR_NB];
            userColorMap.put(account.getUsername(), userColor);
         }

         thisSession.getUserProperties().put("COLOR", userColor);

         Message joinMsg = new Message();

         joinMsg.TYPE = "INFO";
         joinMsg.SUBTYPE = "JOIN";
         joinMsg.INFO_MSG = account.getUsername() + " has entered the building";
         joinMsg.STATS_MSG = userNb + " User" + (userNb > 1 ? "s " : " ") + "online!";
         joinMsg.USER_LIST = buildUserList(true);

         broadcastMessage(joinMsg, false);

         return resultMsg;
      }

      resultMsg.MSG = "Wrong username or password!";
      return resultMsg;
   }*/


   private ResultMsg logoutUser() {
      ResultMsg resultMsg = new ResultMsg();

      if (thisSession.getUserProperties().containsKey("USER")) {
         int userNb = usersLoggedIn.decrementAndGet();

         resultMsg.CODE = "OK";
         resultMsg.MSG = "You have successfully logged out!";

         Message unjoinMsg = new Message();

         unjoinMsg.TYPE = "INFO";
         unjoinMsg.SUBTYPE = "JOIN";
         unjoinMsg.INFO_MSG = thisSession.getUserProperties().get("USER") + " has left the building";
         unjoinMsg.STATS_MSG = userNb + " User" + (userNb > 1 ? "s " : " ") + "online!";
         unjoinMsg.USER_LIST = buildUserList(false);

         thisSession.getUserProperties().clear();

         broadcastMessage(unjoinMsg, false);

      } else {
         resultMsg.CODE = "ERR";
         resultMsg.MSG = "You where not logged in!";
      }

      return resultMsg;
   }

   /**
    * Validate this session
    *
    * @return "true" only if connection is open AND authorized
    */
   private boolean validateUserSession() {
      boolean result = false;

      // Close connection to unauthorized peers
      if (thisSession.isOpen()) {
         if (!thisSession.getUserProperties().containsKey("USER")) {
            LOG.debug("Closing connection to unauthorized peer: " + thisSession.getId());
            activeClose(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Message from unauthorized peer"));
            result = false;
         } else {
            result = true;
         }
      }

      return result;
   }

   private void activeClose(CloseReason reason) {
      try {
         if (thisSession.isOpen()) {
            LOG.debug("Closing connection to peer: " + thisSession.getId());
            thisSession.close(reason);
         }
      } catch (IOException e) {
         LOG.error(e);
      }
   }

   private String[] buildUserList(final boolean includeThis) {
      List<String> userList = new ArrayList<>();

      LOG.debug("buildUserList(): " + thisSession.getOpenSessions().size());

      for (Session session : thisSession.getOpenSessions()) {

         if (!includeThis && thisSession.equals(session)) {
            continue;
         }

         String userName = (String) session.getUserProperties().get("USER");
         String userColor = (String) session.getUserProperties().get("COLOR");
         userList.add(userColor + "*" + userName);
      }

      return (userList.size() == 0) ? null : userList.toArray(new String[userList.size()]);
   }


   private void sendMessage(final Message serverMsg) {
      final Gson gson = new Gson();

      try {
         final String jsonStr = gson.toJson(serverMsg);
         if (thisSession.isOpen()) {
            LOG.debug("Send: " + jsonStr + " to: " + thisSession.getId());
            thisSession.getBasicRemote().sendText(jsonStr);
         }
      } catch (Exception e) {
         LOG.error(e);
      }
   }

   private void broadcastMessage(final Message serverMsg, final boolean includeThis) {
      final Gson gson = new Gson();

      try {
         final String jsonStr = gson.toJson(serverMsg);

         for (Session session : thisSession.getOpenSessions()) {
            if (!includeThis && thisSession.equals(session)) {
               continue;
            }
            session.getAsyncRemote().sendText(jsonStr);
         }
      } catch (Exception e) {
         LOG.error(e);
      }
   }

   private void sessionDestroyed(final String username) {
      LOG.debug("sessionDestroyed(): " + username);
      if(thisSession.getUserProperties().get("USER").equals(username)) {
         thisSession.getUserProperties().clear();
         validateUserSession();
      }
   }


   public static class Config extends ServerEndpointConfig.Configurator {
      @Override
      public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
         super.modifyHandshake(sec, request, response);
         HttpSession httpSession = (HttpSession) request.getHttpSession();
         sec.getUserProperties().put("httpSession", httpSession);
      }
   }

}
