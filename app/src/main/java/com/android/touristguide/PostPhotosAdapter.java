package com.android.touristguide;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;

import java.util.List;

public class PostPhotosAdapter extends RecyclerView.Adapter<PostPhotosAdapter.ViewHolder>{
    private List<Uri> listPhotoUri;
    private List<Boolean> isFirebasePhoto;
    private AppCompatActivity activity;
    private final int ADD_PHOTO_VIEW_TYPE = 0;
    private final int PHOTO_VIEW_TYPE = 1;
    public  PostPhotosAdapter(AppCompatActivity activity, List<Uri> listPhotoUri, List<Boolean> isFirebasePhoto){
        this.listPhotoUri = listPhotoUri;
        this.activity = activity;
        this.isFirebasePhoto = isFirebasePhoto;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View itemView = inflater.inflate(R.layout.new_post_photo_item,parent,false);
        if (viewType == ADD_PHOTO_VIEW_TYPE){
            itemView = inflater.inflate(R.layout.button_add_photo,parent,false);
        }
        return new PostPhotosAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position<listPhotoUri.size()){
            Uri photoUri = listPhotoUri.get(position);
            ImageView imvPhoto = holder.imvPhoto;
            ImageView btnRemove = holder.btnRemove;
            Glide.with(activity).load(photoUri).into(imvPhoto);
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listPhotoUri.remove(position);
                    isFirebasePhoto.remove(position);
                    notifyDataSetChanged();
                }
            });
        }else{
            View itemView = holder.itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage();
                }
            });
        }
    }

    private void selectImage(){
        BSImagePicker singleSelectionPicker = new BSImagePicker.Builder("com.android.touristguide.fileprovider")
                .setMaximumDisplayingImages(200) //Default: Integer.MAX_VALUE. Don't worry about performance :)
                .setSpanCount(3) //Default: 3. This is the number of columns
                .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
                .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
                .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum displaying images to Integer.MAX_VALUE.
                .setTag("A request ID") //Default: null. Set this if you need to identify which picker is calling back your fragment / activity.
                .useFrontCamera() //Default: false. Launching camera by intent has no reliable way to open front camera so this does not always work.
                .build();
        singleSelectionPicker.show(activity.getSupportFragmentManager(),"picker");
    }

    @Override
    public int getItemCount() {
        return listPhotoUri.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == listPhotoUri.size()){
            return ADD_PHOTO_VIEW_TYPE;
        }
        return PHOTO_VIEW_TYPE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView btnRemove;
        public ImageView imvPhoto;
        public View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnRemove = itemView.findViewById(R.id.imv_remove);
            imvPhoto = itemView.findViewById(R.id.imv_photo);
            this.itemView = itemView;
        }
    }
}
