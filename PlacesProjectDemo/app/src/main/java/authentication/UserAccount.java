package authentication;

import com.google.firebase.database.PropertyName;

public class UserAccount {
    private String email;

    // Empty default constructor is crucial for Firebase to deserialize data
    public UserAccount() {
    }

    public UserAccount(String email) {
        this.email = email; // Correctly set the email here in your parameterized constructor
    }

    @PropertyName("Email") // Make sure this matches exactly with what's in Firebase
    public String getEmail() {
        return email;
    }

    @PropertyName("Email")
    public void setEmail(String email) {
        this.email = email;
    }

}

