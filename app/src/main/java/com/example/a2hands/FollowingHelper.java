package com.example.a2hands;

import androidx.annotation.NonNull;

import com.example.a2hands.home.posts.PostsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class FollowingHelper {
    String curr_uid;
    String uid;

    public FollowingHelper(String curr_uid, String uid) {
        this.curr_uid = curr_uid;
        this.uid = uid;
    }
    public void unfollow(){
        //// delete following and follower
        // followings > following > follower
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid)
                .child(uid).setValue(null);
        // followers > follower > following
        FirebaseDatabase.getInstance().getReference("followers").child(uid)
                .child(curr_uid).setValue(null);
    }

    public void follow(){
        // set new following and follower
        FirebaseDatabase.getInstance().getReference("followings").child(curr_uid)
                .child(uid).setValue(true);
        FirebaseDatabase.getInstance().getReference("followers").child(uid)
                .child(curr_uid).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final NotificationHelper  nh = new NotificationHelper();
                PostsFragment.getUser(new Callback() {
                    @Override
                    public void callbackUser(User user) {
                        nh.sendFollowedNotification(user,uid);
                    }
                },curr_uid);
            }
        });
    }
}
