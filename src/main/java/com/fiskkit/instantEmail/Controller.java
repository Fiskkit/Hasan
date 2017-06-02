package com.fiskkit.instantEmail;

import com.fiskkit.instantEmail.models.User;
import com.jobhive.sakimonkey.MandrillAsyncClient;
import com.jobhive.sakimonkey.api.async.callback.ObjectResponseCallback;
import com.jobhive.sakimonkey.data.Result;
import com.jobhive.sakimonkey.data.request.Message;
import com.jobhive.sakimonkey.data.request.Message.Recipient;
import com.jobhive.sakimonkey.data.request.Message.Var;
import com.jobhive.sakimonkey.data.response.MessageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Component
public class Controller {
  @Autowired CrudRepository<User, Integer> repository;

  private MandrillAsyncClient mandrillAsyncClient = null; // lazy instantiation

  @RequestMapping(value="/notify", method=RequestMethod.HEAD)
  public Boolean notifyMe(@RequestParam(value="user")String userIdAsString) {
    Integer userId = new Integer(userIdAsString);
    User user = repository.findOne(userId);
    String apiKey = System.getProperty("mandrill.apiKey");
    if (apiKey != null) {
      mandrillAsyncClient = new MandrillAsyncClient(apiKey, null);
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
    } else {
      System.setProperty("instantEmailSuccess", "false");
    }
    return new Boolean(System.getProperty("instantEmailSuccess"));
  }
}
