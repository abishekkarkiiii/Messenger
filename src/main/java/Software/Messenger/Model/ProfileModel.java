package Software.Messenger.Model;

import Software.Messenger.Entity.Profile;
import Software.Messenger.Entity.ResponseRequest;
import Software.Messenger.Entity.UserAccount;
import Software.Messenger.Repositry.ProfileRepo;
import Software.Messenger.Repositry.UserAccountRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProfileModel {
    @Autowired
    ProfileRepo profileRepo;
    @Autowired
    UserAccountRepo userAccountRepo;

    @Autowired
    SimpMessagingTemplate messagingTemplate;
    private  Profile saveprofile(Profile profile){
       return profileRepo.save(profile);
    }

    public void createProfile(String username){
        Profile profile=new Profile();
        profile.setUsername(username);
         Profile VirtualProfile= saveprofile(profile);
         deleteProfile(VirtualProfile);
         VirtualProfile.setUserId(VirtualProfile.getId().toString());
        VirtualProfile.setUserId(profile.getId().toString());
        profileRepo.save(VirtualProfile);
        UserAccount VirtualUser= userAccountRepo.findByusername(profile.getUsername());
        userAccountRepo.delete(VirtualUser);
        VirtualUser.setProfile(profile);
        userAccountRepo.save(VirtualUser);


    }
// study topic
    public List<Profile> getAllProfile(UserAccount userAccount) {
        String currentUserId = userAccount.getProfile().getId().toString();
        List<Profile> profiles = profileRepo.findAll();
        System.out.println(currentUserId);

        // Separate the current user's profile from the rest
        List<Profile> updatedProfiles = profiles.stream()
                .filter(profile -> !profile.getUserId().equals(currentUserId))
                .collect(Collectors.toList());

        Profile currentUserProfile = profiles.stream()
                .filter(profile -> profile.getUserId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        // Clear the RequestList of each profile except the current user's profile
        updatedProfiles.forEach(profile -> profile.getRequestList().clear());
        updatedProfiles.forEach(profile ->profile.getFriendcode().clear() );
        updatedProfiles.forEach(profile ->profile.getFriendList().clear() );

        // Add the current user's profile back to the list
        if (currentUserProfile != null) {
            updatedProfiles.add(currentUserProfile);
        }

        return updatedProfiles;
    }


    public void deleteProfile(Profile profile){
        profileRepo.delete(profile);

    }

    public Profile profileFinder(ObjectId objectId) {
        Optional<Profile> optionalProfile = profileRepo.findById(objectId);
        return optionalProfile.orElse(null);
    }

    public boolean requestadd(String sender, String receiver) {
        Profile receiverProfile = profileFinder(new ObjectId(receiver));
        if (!receiverProfile.getRequestList().contains(sender)&&!receiverProfile.getFriendList().contains(sender)) {
            receiverProfile.getRequestList().add(sender);
            System.out.println("trueee");
            profileRepo.save(receiverProfile);
            return true;
        }else{
            return false;
        }
    }

    public void deletefriend(ResponseRequest request){
       Profile userprofile=profileFinder(new ObjectId(request.getReceiverUserId()));
       Profile friendprofile=profileFinder(new ObjectId(request.getUserId()));
        friendprofile.setFriendList(friendprofile.getFriendList().stream().filter(x->!x.equals(request.getReceiverUserId())).collect(Collectors.toList()));
       userprofile.setFriendList(userprofile.getFriendList().stream().filter(x->!x.equals(request.getUserId())).collect(Collectors.toList()));
       profileRepo.save(userprofile);
       profileRepo.save(friendprofile);
       messagingTemplate.convertAndSend("/chat/"+request.getUserId()+"message",profileDetails(friendprofile));
        System.out.println("hello");
    }

    public List<Profile> profileDetails(Profile profile){
        List<Profile> virtualprofile=new ArrayList<>();
        profile.getFriendList().forEach(x->virtualprofile.add(profileFinder(new ObjectId(x))));
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

    public Profile profileSaver(Profile profile)
    {
       return profileRepo.save(profile);
    }

}
