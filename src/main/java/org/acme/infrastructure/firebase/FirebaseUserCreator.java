package org.acme.infrastructure.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FirebaseUserCreator {

    public String create(String email, String password) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password);
        UserRecord record = FirebaseAuth.getInstance().createUser(request);
        return record.getUid();
    }

    public String verifyIdToken(String token) throws FirebaseAuthException {
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
        return decoded.getUid();
    }

    public FirebaseToken verifyIdTokenFull(String token) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().verifyIdToken(token);
    }

    public void deleteUser(String firebaseUuid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(firebaseUuid);
    }

    public void disableUser(String firebaseUuid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().updateUser(
                new UserRecord.UpdateRequest(firebaseUuid).setDisabled(true));
    }

    public void enableUser(String firebaseUuid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().updateUser(
                new UserRecord.UpdateRequest(firebaseUuid).setDisabled(false));
    }
}
