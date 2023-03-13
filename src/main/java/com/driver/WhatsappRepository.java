package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below-mentioned hashmaps or delete these and create your own.
    private HashMap<String, User> userHashMap;
    private HashMap<User, Group> groupdb;
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    //key group and value as user
    //group ----- user(admin)
    private HashSet<String> userMobile;

    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.userHashMap = new HashMap<String, User>();
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupdb = new HashMap<User, Group>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 1;
        this.messageId = 1;
    }

    public String createUser(String name, String mobile) throws Exception{
        if(userHashMap.containsKey(mobile)){
            throw new Exception("User already exists");
        }
        User user = new User(name, mobile);
        userHashMap.put(mobile, user);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        if(users.size() == 2){
            groupUserMap.put(new Group(users.get(1).getName(), users.size()), users);
            Group group = new Group(users.get(0).getName(), users.size());
            adminMap.put(group, users.get(0));
            return group;
        }
        Group group = new Group("Group "+customGroupCount, users.size());
        groupUserMap.put(group, users);
        adminMap.put(group, users.get(0));
        customGroupCount++;
        for(User user : users){
            groupdb.put(user, group);
        }
        return group;
    }

    public int createMessage(String content){
        Message message = new Message(messageId, content, new Date());
        messageId++;
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        boolean isMember = false;
        List<User> members = groupUserMap.get(group);
        for(User user : members){
            if (user == sender) {
                isMember = true;
                break;
            }
        }
        if(!isMember){
            throw new Exception("You are not allowed to send message");
        }
        senderMap.put(message, sender);
        List<Message> messages = groupMessageMap.get(group);
        messages.add(message);
        groupMessageMap.put(group, messages);
        return messages.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(user != adminMap.get(group)){
            throw new Exception("Approver does not have rights");
        }
        List<User> members = groupUserMap.get(group);
        boolean isMember = false;
        for(User user1 : members){
            if(user == user1){
                isMember = true;
                break;
            }
        }
        if(!isMember){
            throw new Exception("User is not a participant");
        }
        adminMap.put(group, approver);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        if(!groupdb.containsKey(user)){
            throw new Exception("User not found");
        }
        boolean ifAdmin = false;
        for(Map.Entry<Group, User> userEntry : adminMap.entrySet()){
            if(user == userEntry.getValue()){
                ifAdmin = true;
                break;
            }
        }
        if(ifAdmin){
            throw new Exception("Cannot remove admin");
        }
        Group group = groupdb.get(user);
        List<User> members = groupUserMap.get(group);
        group.setNumberOfParticipants(members.size()-1);
        members.remove(user);
        groupUserMap.put(group, members);
        groupdb.remove(user);
        List<Message> messages = groupMessageMap.get(group);
        for(Map.Entry<Message, User> userEntry : senderMap.entrySet()){
            if(userEntry.getValue() == user){
                messages.remove(userEntry.getKey());
            }
        }
        groupMessageMap.put(group, messages);
        userHashMap.remove(user.getMobile());
        for(Map.Entry<Message, User> userEntry : senderMap.entrySet()){
            if(userEntry.getValue() == user){
                senderMap.remove(userEntry.getKey());
            }
        }
        int total = 0;
        total = members.size() + messages.size() + senderMap.size();
        return total;
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        List<String> messages = new ArrayList<>();
        for(Message message : senderMap.keySet()){
            if(start.after(message.getTimestamp()) && end.before(message.getTimestamp())){
                messages.add(message.getContent());
            }
        }
        return messages.get(messages.size()-K);
    }
}
