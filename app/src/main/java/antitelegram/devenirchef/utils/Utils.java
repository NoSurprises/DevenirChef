package antitelegram.devenirchef.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Utils {
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseAuth firebaseAuth;

    public static FirebaseDatabase getFirebaseDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        return firebaseDatabase;
    }

    public static FirebaseAuth getFirebaseAuth() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();

        }
        return firebaseAuth;
    }


}
