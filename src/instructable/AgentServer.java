package instructable;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Amos Azaria on 28-May-15.
 */
public class AgentServer implements HttpHandler
{
        AgentDataAndControl agentDataAndControl;
    static public final String userSaysParam = "userSays";
    static public final String userIdParam = "userId";

        AgentServer(AgentDataAndControl agentDataAndControl)
        {
            this.agentDataAndControl = agentDataAndControl;
        }

        public void handle(HttpExchange httpExchange)
        {
            try
            {
                //Map<String, String> parameters = queryToMap(httpExchange.getRequestURI().getQuery());
                Map<String, Object> parameters =
                        (Map<String, Object>) httpExchange.getAttribute(Service.ParameterFilter.parametersStr);
                String systemReply = null;
                if (!parameters.containsKey(userIdParam))
                {
                    agentDataAndControl.logger.warning("no userId");
                    return;
                }
                String userId = parameters.get(userIdParam).toString();
                if (parameters.containsKey(userSaysParam))
                {
                    try
                    {
                        String userSays = parameters.get(userSaysParam).toString();
                        agentDataAndControl.logger.info("UserID:" + userId + ". User says: " + parameters.get(userSaysParam).toString());
                        systemReply = agentDataAndControl.executeSentenceForUser(userId, userSays);
                    } catch (Exception ex)
                    {
                        agentDataAndControl.logger.log(Level.SEVERE, "an exception was thrown", ex);
                        systemReply = "Sorry, but I got some error...";
                    }
                }
                else
                {
                    agentDataAndControl.logger.warning("UserID:" + userId  + ". User has no " + userSaysParam);
                    systemReply = "Hello, how can I help you?";
                }
                agentDataAndControl.logger.info("UserID:" + userId + ". System reply: " + systemReply);
                httpExchange.sendResponseHeaders(200, systemReply.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(systemReply.getBytes());
                os.close();
            }
            catch (Exception ex)
            {
                agentDataAndControl.logger.log(Level.SEVERE, "an exception was thrown", ex);
            }
        }
    }