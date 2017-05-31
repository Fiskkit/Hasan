package com.fiskkit.instantEmail;

import com.jobhive.sakimonkey.MandrillAsyncClient;
import com.jobhive.sakimonkey.api.async.callback.ObjectResponseCallback;
import com.jobhive.sakimonkey.data.Result;
import com.jobhive.sakimonkey.data.request.Message;
import com.jobhive.sakimonkey.data.request.Message.Recipient;
import com.jobhive.sakimonkey.data.request.Message.Var;
import com.jobhive.sakimonkey.data.response.MessageStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class Controller {
  private MandrillAsyncClient mandrillAsyncClient = null; // lazy instantiation

  @Value("${apiKey}")
  private String apiKey;
  @RequestMapping("/notify")
  public Boolean notifyMe(@RequestParam(value="user")String userIdAsString) {
    try {
      Integer userId = new Integer(userIdAsString);
      User user = MySQLAccess.getUser(userId);
      if (mandrillAsyncClient == null){
        mandrillAsyncClient = new MandrillAsyncClient(apiKey, null);
      }
      Message message = new Message();
      message.setFromEmail("contact@fiskkit.com");
      message.setSubject("{{user.first_name}}, {{subject_end}}");
      String templateName = "instant-wip-template";
      mandrillAsyncClient.api().messages().sendTemplate(templateName, message, new ObjectResponseCallback<MessageStatus[]>() {
        @Override
        public void onSuccess (Result<MessageStatus[]> result) {
          System.setProperty("instantEmailSuccess", "true");
        }
      });
    } catch (Throwable catchall) {
      System.setProperty("instantEmailSuccess", "false");
    }
    return System.getProperty("instantEmailSuccess") == "true";
  }
}
