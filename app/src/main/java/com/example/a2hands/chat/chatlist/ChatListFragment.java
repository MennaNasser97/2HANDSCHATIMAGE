package com.example.a2hands.chat.chatlist;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.chat.Chat;
import com.example.a2hands.chat.chatlist.AdapterChatList;
import com.example.a2hands.chat.chatlist.ChatList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ChatList> chatListList;
    List<User> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    CollectionReference userReference ;
    AdapterChatList adapterChatList;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        firebaseAuth =FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("users");
        currentUser =FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recyclerView);
        chatListList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ChatList chatList=ds.getValue(ChatList.class);
                    chatListList.add(chatList);
                }
                loadChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loadChat() {
        userList = new ArrayList<>();
        db.collection("users/").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    User user=documentSnapshot.toObject(User.class);
                    user.user_id =documentSnapshot.getId();
                    for (ChatList chatList: chatListList){
                        if (user.user_id !=null && user.user_id.equals(chatList.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                }
                adapterChatList = new AdapterChatList(getContext(),userList);
                for (int i=0; i<userList.size();i++){
                    lastMessage(userList.get(i).user_id);
                }
                chatListList.clear();
                recyclerView.setAdapter(adapterChatList);
            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentUser.toString())
                .child(userId)
                .child("Message");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String thelastMessage =" ";
                String seen =" ";
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    Chat chat=ds.getValue(Chat.class);
                    if (chat==null){
                        continue;
                    }
                    String sender =chat.getSender();
                    String reciever =chat.getReceiver();
                    if (sender == null || reciever == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())){
                        String messagImage=chat.getMessageImage();
                        if (!TextUtils.isEmpty(messagImage) &&!chat.getMessage().equals("This message was Deleted...") && TextUtils.isEmpty(chat.getMessage())) {
                            thelastMessage="sent a photo";
                        }else {
                            thelastMessage = chat.getMessage();
                        }
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId)){
                        if (chat.getIsSeen()){
                            seen="noNewMessage";
                        }else {
                            seen="NewMessage";
                        }
                    }
                }
                adapterChatList.setLastMessageAndSeenMap(userId,thelastMessage,seen);
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
