package antitelegram.devenirchef;

import android.os.Bundle;

public class UserInfoActivity extends DrawerBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_user_info);
    }

    @Override
    void addDatabaseReadListener() {

    }
}
