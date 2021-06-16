package com.fadfadah.app.callback;

public interface FragmentSelectionListener {

    void selectFragment(SelectedFragment fragment);

    void selectViewComments(String postkey,String postOwnerKey);

    void search(String year);
    void search();

    void openDrawer();
}
