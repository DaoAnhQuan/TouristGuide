package com.android.touristguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {
    private RecyclerView rcvTopic;
    private RecyclerView rcvPosts;
    private EditText edSearch;
    private String mode;
    private String topic = "All";
    private List<Post> postList;
    private Spinner spinner;
    private final String[] modes = {"Nearby","All posts","My posts"};
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
    }

//    private Task<List<Post>> getListPosts(){
//        String query = edSearch.getText().toString().trim();
//        String mode = modes[spinner.getSelectedItemPosition()];
//
//    }

    private void getCurrentLocation(){

    }
    private void setupDropdownMenu(View view){
        spinner = view.findViewById(R.id.spinner);

        ArrayAdapter adapter = new ArrayAdapter(getContext(),R.layout.post_toolbar_list_item,modes);
        spinner.setAdapter(adapter);
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
            if (selectedPosition == -1 && position == 0){
                button.setBackgroundColor(Color.parseColor("#1964E6"));
            }
            if (selectedPosition == position){
                button.setBackgroundColor(Color.parseColor("#1964E6"));
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    topic = topicString[position];
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
