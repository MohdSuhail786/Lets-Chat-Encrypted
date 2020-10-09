package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAdapter extends FragmentPagerAdapter {
    public TabsAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatsFragment();

            case 1:
                return new ContactsFragment();

            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: ChatsFragment chatsFragment = new ChatsFragment();
                return "Chats";

            case 1: ContactsFragment contactsFragment = new ContactsFragment();
                return "Contacts";

            default: return null;
        }
    }
}
