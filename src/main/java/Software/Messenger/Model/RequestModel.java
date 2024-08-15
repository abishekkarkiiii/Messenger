package Software.Messenger.Model;

import Software.Messenger.Entity.Profile;
import Software.Messenger.Entity.ResponseRequest;
import Software.Messenger.Repositry.ProfileRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class RequestModel {
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    ProfileRepo profileRepo;
    @Autowired
    ProfileModel profileModel;
    public void friendRequest(Profile user, Profile sender) {
        System.out.println(user);
        System.out.println(sender);
        if(!user.getFriendList().contains(sender.getUserId())){
          String friendcode=  new BCryptPasswordEncoder().encode(user.getUserId()+sender.getUserId());
            user.getFriendcode().add(friendcode);
            sender.getFriendcode().add(friendcode);
            user.getFriendList().add(sender.getUserId());
            sender.getFriendList().add(user.getUserId());
            user.getRequestList().removeIf(id -> id.equals(sender.getUserId()));
            sender.getRequestList().removeIf(id -> id.equals(user.getUserId()));
            profileRepo.save(user); 
            profileRepo.save(sender);
            System.out.println(responseRequests(profileDetails(user)));
            simpMessagingTemplate.convertAndSend("/chat/"+sender.getUserId()+"message",responseRequests(profileDetails(sender)));
            simpMessagingTemplate.convertAndSend("/chat/"+user.getUserId()+"message",responseRequests(profileDetails(user)));
        }else{
            System.out.println("sorry already exist");
        }

    }

    public List<Profile> profileDetails(Profile profile){
        List<Profile> virtualprofile=new ArrayList<>();
        profile.getFriendList().forEach(x->virtualprofile.add(profileModel.profileFinder(new ObjectId(x))));
        return virtualprofile;

    }

    public List<ResponseRequest> responseRequests(List<Profile> profileList) {
        List<ResponseRequest> requestList = new ArrayList<>();

        for (Profile profile : profileList) {
            ResponseRequest responseRequest = new ResponseRequest();
            responseRequest.setUserId(profile.getUserId());
            responseRequest.setUsername(profile.getUsername());
            requestList.add(responseRequest);
        }

        return requestList;
    }




}
