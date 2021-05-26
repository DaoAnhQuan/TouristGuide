package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainAdapter extends FragmentStateAdapter {

    public MainAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new PostFragment();
            case 2: return new NotificationFragment();
            case 3: return new AccountFragment();
            default: return new MapFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
