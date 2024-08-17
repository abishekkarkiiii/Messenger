package Software.Messenger.Model;

import Software.Messenger.Entity.Message;
import Software.Messenger.Entity.Profile;
import Software.Messenger.Entity.UserAccount;
import Software.Messenger.Repositry.MessageRepo;
import Software.Messenger.Repositry.UserAccountRepo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
public class ChatModel {
private String username;
@Autowired
    UserModel userModel;
@Autowired
    MessageRepo messageRepo;


     private UserAccount user(){
         System.out.println("hello");
        return userModel.checker(username);
    }

    public ObjectId Idgiver(){
         return user().getObjectId();
    }


    public String usernamefinder(ObjectId id){
         return userModel.finduserAccount(id).getUsername();
     }

     public Message messagesSave(Message message){
         if(isBase64Data(message.getContent())){
            PicintoBlob(message.getContent()); ;
         }
         return messageRepo.save(message);
     }
    private boolean isBase64Data(String input) {
        String [] basefilter =input.split(",");
        System.out.println(basefilter[0]);
//        System.out.println(basefilter[1]);
          if( basefilter[0].contains("data:image")&&basefilter[1].length()>150){
              System.out.println("base64");
              return true;
          }

       return false;
    }
     public List<Message> getMessage(String friendcode){
         return messageRepo.findByfriendcode(friendcode);
     }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public Optional<String> usercode(Profile userIdOfSender, Profile userIdOfReceiver) {
        String checker = userIdOfSender.getUserId() + userIdOfReceiver.getUserId();
        String reverseChecker = userIdOfReceiver.getUserId() + userIdOfSender.getUserId();

        return userIdOfReceiver.getFriendcode().stream()
                .filter(encodedFriendcode -> passwordEncoder.matches(checker, encodedFriendcode) ||
                        passwordEncoder.matches(reverseChecker, encodedFriendcode))
                .findFirst();
    }

    public byte [] PicintoBlob(String imgaeurl){
        System.out.println("started");
        String [] basefilter =imgaeurl.split(",");
        byte [] image= Base64.getDecoder().decode(basefilter[1]);
        System.out.println("base");
        return image;
    }


}
