package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Arrays;
import java.util.List;

import antitelegram.devenirchef.utils.Utils;

import static com.google.android.gms.common.ConnectionResult.SUCCESS;

/**
 * Created by Nick on 1/19/2018.
 */

public abstract class DrawerBaseActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private NavigationView navigation;
    private DrawerLayout drawer;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Toolbar toolbar;
    private ChildEventListener childEventListener;
    private View navigationHeader;
    private TextView drawerUsername;
    private TextView drawerEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_drawer_layout);

        setUpNavigationDrawer();
        setUpToolbar();

        initDatabase();
        initStorage();
        initAuth();

    }

    @Override
    protected void onResume() {
        super.onResume();

        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
        removeDatabaseReadListener();
    }

    void setContentLayout(int layoutResource) {
        ViewStub viewStub = findViewById(R.id.drawer_content_stub);
        viewStub.setLayoutResource(layoutResource);
        viewStub.inflate();
    }

    void onSignedOut() {
        currentUser = null;
        removeDatabaseReadListener();
    }

    public void initDataInNavigationDrawer() {
        drawerUsername.setText(currentUser.getDisplayName());
        drawerEmail.setText(currentUser.getEmail());
    }

    private void onSignedInInitialize(FirebaseUser user) {
        currentUser = user;
        addDatabaseReadListener();
        initDataInNavigationDrawer();
    }

    abstract void addDatabaseReadListener();

    private void setUpNavigationDrawer() {
        drawer = findViewById(R.id.drawer_layout);
        navigation = findViewById(R.id.nav_view);
        navigationHeader = navigation.getHeaderView(0);
        initDrawerDataFields();
        setNavigationMenuClickListener();
        setUpEmailOptionsButton();
    }

    private void initDrawerDataFields() {
        drawerUsername = navigationHeader.findViewById(R.id.username);
        drawerEmail = navigationHeader.findViewById(R.id.user_email);
    }

    private void setNavigationMenuClickListener() {
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_recipes: {
                        if (DrawerBaseActivity.this instanceof MainActivity) {
                            break;
                        }
                        Intent recipesIntent = new Intent(DrawerBaseActivity.this, MainActivity.class);
                        startActivityWithoutHistory(recipesIntent);
                        break;
                    }
                    case R.id.nav_level_info: {
                        if (DrawerBaseActivity.this instanceof UserInfoActivity) {
                            break;
                        }
                        Intent infoIntent = new Intent(DrawerBaseActivity.this, UserInfoActivity.class);
                        startActivityWithoutHistory(infoIntent);
                        break;
                    }

                    default: {
                        Toast.makeText(DrawerBaseActivity.this,
                                item.getTitle() + " not implemented by Alex yet", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                return true;
            }

            private void startActivityWithoutHistory(Intent recipesIntent) {
                DrawerBaseActivity.this.finish();
                startActivity(recipesIntent);
            }
        });
    }

    private void setUpEmailOptionsButton() {
        ImageButton im = navigationHeader.findViewById(R.id.email_options_button);

        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu emailOptions = createPopupMenu(v);

                emailOptions.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout: {
                                logout();
                                drawer.closeDrawers();
                                return true;
                            }
                        }
                        return false;
                    }
                });

            }

            private PopupMenu createPopupMenu(View anchor) {
                PopupMenu emailOptions = new PopupMenu(DrawerBaseActivity.this, anchor, Gravity.END);
                emailOptions.inflate(R.menu.email_options_menu);
                emailOptions.show();
                return emailOptions;
            }
        });
    }

    private void setUpToolbar() {
        initToolbar();
        setUpDrawerToggle();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(DrawerBaseActivity.this, "toolbar menu item clicked", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.options_menu);
    }

    private void setUpDrawerToggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initAuth() {
        // TODO: 1/7/2018 decompose
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {

            private List<AuthUI.IdpConfig> providers;
            private int codeServicesAvailable;
            private GoogleApiAvailability apiAvailability;

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitialize(user);
                } else {
                    onSignedOut();

                    apiAvailability = GoogleApiAvailability.getInstance();
                    codeServicesAvailable = apiAvailability
                            .isGooglePlayServicesAvailable(DrawerBaseActivity.this);

                    setAuthProviders();

                    if (codeServicesAvailable == SUCCESS) {
                        startAuthenticationActivity();
                    } else {
                        apiAvailability.showErrorDialogFragment(DrawerBaseActivity.this, codeServicesAvailable, RC_SIGN_IN);
                    }
                }
            }

            private void startAuthenticationActivity() {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }

            private void setAuthProviders() {
                providers = Arrays.asList(
                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                );
            }
        };
    }

    private void initStorage() {
        storage = Utils.getFirebaseStorage();
    }


    private void initDatabase() {
        database = Utils.getFirebaseDatabase();
    }

    private void removeDatabaseReadListener() {
        if (childEventListener == null) {
            return;
        }
        database.getReference("recipes").removeEventListener(childEventListener);
        childEventListener = null;
    }

    private void logout() {
        if (currentUser != null) {
            AuthUI.getInstance().signOut(this);
        }

    }

}
