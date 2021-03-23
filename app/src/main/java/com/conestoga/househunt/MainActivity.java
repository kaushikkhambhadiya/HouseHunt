package com.conestoga.househunt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.conestoga.househunt.Fragments.FragmentTabsFavorite;
import com.conestoga.househunt.Fragments.FragmentTabsListing;
import com.conestoga.househunt.MainFragments.AboutFragment;
import com.conestoga.househunt.MainFragments.HomeFragment;
import com.conestoga.househunt.MainFragments.MyMessagesFragment;
import com.conestoga.househunt.MainFragments.MyProfileFragment;
import com.conestoga.househunt.utils.Tools;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ActionBar actionBar;
    private Toolbar toolbar;
    FirebaseAuth firebaseAuth;
    DrawerLayout drawer_layout;
    NavigationView nav_view;
    //creating fragment object
    Fragment fragment = null;
    boolean doubleBackToExitPressedOnce = false;


    //On Back Pressed the navigation drawer closes
    @Override
    public void onBackPressed() {

        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        }
        else {
            if (checkNavigationMenuItem() != 0)
            {
                nav_view.setCheckedItem(R.id.nav_home);
                fragment = new HomeFragment();
                toolbar.setTitle(getResources().getString(R.string.app_name));
                //replacing the fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
            else {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
        }
    }

    private int checkNavigationMenuItem() {
        Menu menu = nav_view.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).isChecked())
                return i;
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nav_view = (NavigationView) findViewById(R.id.nav_view);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);

        initToolbar();
        firebaseAuth = FirebaseAuth.getInstance();

        //Navigation Drawer Menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();
        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);
        //set data to Navigation Header

        View header = nav_view.getHeaderView(0);
        TextView tvname = (TextView) header.findViewById(R.id.tvname);
        TextView tvemail = (TextView) header.findViewById(R.id.tvemail);
        final ImageView ivavatar = (ImageView) header.findViewById(R.id.avatar);

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user!=null){
            tvname.setText(user.getDisplayName());
            tvemail.setText(user.getEmail());
            Glide.with(MainActivity.this)
                    .load(user.getPhotoUrl())
                    .into(ivavatar);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new HomeFragment());
        ft.commit();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.app_name));
        Tools.setSystemBarColor(this);
    }



    //Navigate to selected menu item
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                toolbar.setTitle(getResources().getString(R.string.app_name));
                break;
            case R.id.nav_listing:
                fragment = new FragmentTabsListing();
                toolbar.setTitle(menuItem.getTitle());
                break;
            case R.id.nav_favorites:
                fragment = new FragmentTabsFavorite();
                toolbar.setTitle(menuItem.getTitle());
                break;
            case R.id.nav_chat:
                fragment = new MyMessagesFragment();
                toolbar.setTitle(menuItem.getTitle());
                break;
            case R.id.nav_profile:
                fragment = new MyProfileFragment();
                toolbar.setTitle(menuItem.getTitle());
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_about:
                fragment = new AboutFragment();
                toolbar.setTitle(menuItem.getTitle());
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_post) {
            Intent intent = new Intent(MainActivity.this,UploadProperty.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}