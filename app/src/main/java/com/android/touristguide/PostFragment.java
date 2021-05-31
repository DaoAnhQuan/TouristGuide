package com.android.touristguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostFragment extends Fragment {
    private RecyclerView rcvTopic;
    private RecyclerView rcvPosts;
    private EditText edSearch;
    private String mode;
    private String topic = "All";
    private List<Post> postList;
    private Spinner spinner;
    private FirebaseFunctions mFunctions;
    private final String[] modes = {"All posts","Nearby","My posts"};
    private final String TAG = "PostFragmentTAG";
    private SkeletonScreen skeletonScreen;
    private TextView tvNoPost;
    private PostAdapter currentPostAdapter;
    private List<Post> currentListPost;
    private PostFragment postFragment = this;
    public PostFragment(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_post,container,false);
        postList = new ArrayList<>();
        edSearch = view.findViewById(R.id.ed_search_post);
        tvNoPost = view.findViewById(R.id.tv_no_post);
        mFunctions = Helper.initFirebaseFunctions();
        Button btnNewPost = view.findViewById(R.id.btn_new_post);
        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),NewPostActivity.class);
                startActivity(intent);
            }
        });
        setupDropdownMenu(view);
        setupRcvTopic(view);
        setupRcvPosts(view);
        edSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP
                        && edSearch.getText().toString().trim().length()>0){
                    showListPost(postFragment);
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    private void setupRcvTopic(View parent){
        rcvTopic = parent.findViewById(R.id.rcv_topic);
        TopicAdapter adapter = new TopicAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        rcvTopic.setLayoutManager(layoutManager);
        rcvTopic.setAdapter(adapter);
    }

    private void setupRcvPosts(View parent){
        rcvPosts = parent.findViewById(R.id.rcv_posts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rcvPosts.setLayoutManager(layoutManager);
        showListPost(postFragment);
    }

    private void showListPost(PostFragment fragment){
        tvNoPost.setVisibility(View.GONE);
        rcvPosts.setVisibility(View.VISIBLE);
        skeletonScreen = Skeleton.bind(rcvPosts).show();
        getListPosts().addOnCompleteListener(new OnCompleteListener<List<Post>>() {
            @Override
            public void onComplete(@NonNull Task<List<Post>> task) {
                skeletonScreen.hide();
                if (task.isSuccessful()){
                    List<Post> result = task.getResult();
                    currentListPost = result;
                    if (result.size() == 0){
                        tvNoPost.setVisibility(View.VISIBLE);
                        rcvPosts.setVisibility(View.GONE);
                    }else {
                        rcvPosts.setVisibility(View.VISIBLE);
                        tvNoPost.setVisibility(View.GONE);
                        if (getActivity() != null){
                            currentPostAdapter = new PostAdapter(getActivity(),result,fragment);
                            rcvPosts.setAdapter(currentPostAdapter);
                        }
                    }
                }else{
                    Log.d(TAG,task.getException().toString());
                }
            }
        });
    }

    private Task<List<Post>> getListPosts(){
        String query = edSearch.getText().toString().trim();
        String mode = modes[spinner.getSelectedItemPosition()];
        Map<String,String> data = new HashMap<>();
        data.put("mode",mode);
        data.put("topic",topic);
        data.put("query",query);
        return mFunctions.getHttpsCallable("getPost").call(data).continueWith(new Continuation<HttpsCallableResult, List<Post>>() {
            @Override
            public List<Post> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                Map<String,Object> result = (HashMap<String,Object>)task.getResult().getData();
                List<Post> postList = new ArrayList<>();
                for (Map.Entry<String,Object> postData : result.entrySet()){
                    Map<String,Object> postMap = (HashMap<String,Object>)postData.getValue();
                    Post post = new Post(postMap.get("postID").toString(),postMap.get("ownerName").toString(),
                            postMap.get("time").toString(),postMap.get("title").toString(),
                            postMap.get("ownerAvatar").toString(),postMap.get("photo").toString(),
                            (Integer) postMap.get("noLike"),(Integer) postMap.get("noComment"),(Integer) postMap.get("noShare"));
                    postList.add(post);
                };
                Collections.reverse(postList);
                return postList;
            }
        });
    }
    private void setupDropdownMenu(View view){
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter(getContext(),R.layout.post_toolbar_list_item,modes);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edSearch.setText("");
                showListPost(postFragment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20){
            if (resultCode == 0 && data != null){
                String postID = data.getStringExtra("postID");
                for (int i = 0; i<currentListPost.size();i++){
                    if (currentListPost.get(i).postID.equals(postID)){
                        currentListPost.remove(i);
                        currentPostAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
        private String[] topicString = {getString(R.string.all),getString(R.string.photography),getString(R.string.food_and_drink),
            getString(R.string.travel),getString(R.string.activity)};
        private Drawable[] topicIcon = {ContextCompat.getDrawable(getContext(),R.drawable.ic_home_icon),
                ContextCompat.getDrawable(getContext(),R.drawable.ic_photography_post_icon),
                ContextCompat.getDrawable(getContext(),R.drawable.ic_food_drink_post_icon),
                ContextCompat.getDrawable(getContext(),R.drawable.ic_travel_post_icon),
                ContextCompat.getDrawable(getContext(),R.drawable.ic_local_stories_post_icon)};
        private int selectedPosition = -1;
        public TopicAdapter(){

        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.topic_button,parent,false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MaterialButton button = holder.button;
            button.setText(topicString[position]);
            button.setIcon(topicIcon[position]);
            button.setBackgroundColor(Color.parseColor("#FFFFFF"));
            button.setTextColor(Color.parseColor("#000000"));
            if (selectedPosition == -1 && position == 0){
                button.setBackgroundColor(Color.parseColor("#9C27B0"));
                button.setTextColor(Color.parseColor("#FFFFFF"));
            }
            if (selectedPosition == position){
                button.setBackgroundColor(Color.parseColor("#9C27B0"));
                button.setTextColor(Color.parseColor("#FFFFFF"));
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String oldTopic = topic;
                    topic = topicString[position];
                    if (!topic.equals(oldTopic)){
                        edSearch.setText("");
                        showListPost(postFragment);
                    }
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return 5;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            public MaterialButton button;
            public ViewHolder (View itemView){
                super(itemView);
                button = itemView.findViewById(R.id.btn_topic);
            }
        }
    }
}
