package com.nullpointexecutioners.buzzfilms;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsSpinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * User's Profile for their account
 */
public class ProfileActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.currentName)
    TextView profileName;
    @Bind(R.id.currentEmail)
    TextView profileEmail;
    @Bind(R.id.currentMajor)
    TextView profileMajor;
    @Bind(R.id.currentInterests)
    TextView profileInterests;
    @BindDrawable(R.drawable.ic_arrow_back)
    Drawable backArrow;
    @BindString(R.string.edit_profile_dialog_title)
    String editProfileDialogTitle;
    @BindString(R.string.edit_password_dialog_title)
    String editPasswordDialogTitle;
    @BindString(R.string.save)
    String save;
    @BindString(R.string.cancel)
    String cancel;
    @BindString(R.string.major_not_specified)
    String majorNotSpecified;
    @BindString(R.string.new_password_mismatch)
    String passwordMismatch;

    private SessionManager mSession;

    String mUsername;
    String mName;
    String mEmail;
    User.Major mMajor;
    String mInterests;

    final Firebase mRef = new Firebase("https://buzz-films.firebaseio.com/users");

    /**
     * Creates this activity
     * @param savedInstanceState no idea what this is
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        this.mSession = SessionManager.getInstance(getApplicationContext());

        initToolbar();
        setupProfile();
    }

    /**
     * Helper method to setup and display the user's information in the Profile view
     */
    private void setupProfile() {
        /*Get the user's info*/
        HashMap<String, String> user = mSession.getUserDetails();
        mUsername = user.get(SessionManager.KEY_USERNAME);
        mName = user.get(SessionManager.KEY_NAME);
        mEmail = user.get(SessionManager.KEY_EMAIL);

        //Set the current user's attributes
        profileName.setText(mName);
        profileEmail.setText(mEmail);

        /*Get Major from Firebase*/
        mRef.child(mUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mMajor = (User.Major) dataSnapshot.child("major").getValue();
                    profileMajor.setText(mMajor.toString());
                } else {
                    profileMajor.setText(majorNotSpecified);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        /*Get Interests from Firebase*/
        mRef.child(mUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mInterests = dataSnapshot.child("interests").getValue().toString();
                    profileInterests.setText(mInterests);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Handles this activity once it is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * When a user clicks the edit floating action button, a dialog will be shown where the user can edit their current name, email, major, and interests.
     * Once the user is done editing the information, the user will press the "Save" button in the dialog to commit their changes to Firebase.
     */
    @OnClick(R.id.profile_fab)
    public void editProfile() {
        final MaterialDialog editProfileDialog = new MaterialDialog.Builder(ProfileActivity.this)
                .title(editProfileDialogTitle)
                .customView(R.layout.edit_profile_dialog, true)
                .theme(Theme.DARK)
                .positiveText(save)
                .negativeText(cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog editProfileDialog, @NonNull DialogAction which) {
                        final EditText editName = ButterKnife.findById(editProfileDialog, R.id.edit_name);
                        final EditText editEmail = ButterKnife.findById(editProfileDialog, R.id.edit_email);
                        final Spinner majorDropdown = ButterKnife.findById(editProfileDialog, R.id.major_dropdown);
                        final EditText editInterests = ButterKnife.findById(editProfileDialog, R.id.edit_interests);

                        final String NEW_NAME = editName.getText().toString();
                        final String NEW_EMAIL = editEmail.getText().toString();
                        final String NEW_INTERESTS = editInterests.getText().toString();
                        final User.Major NEW_MAJOR = (User.Major) majorDropdown.getSelectedItem();

                        Firebase userRef = mRef.child(SessionManager.KEY_USERNAME);
                        HashMap<String, Object> updateValues = new HashMap<>();
                        updateValues.put("name", NEW_NAME);
                        updateValues.put("email", NEW_EMAIL);
                        updateValues.put("interests", NEW_INTERESTS);
                        updateValues.put("major", NEW_MAJOR);
                        userRef.updateChildren(updateValues); //Update Firebase with new values

                        /*Update the name and email that's stored in this Session*/
                        mName = NEW_NAME;
                        mEmail = NEW_EMAIL;
                        mSession.updateSession(mName, mEmail);

                        (ProfileActivity.this).passThrough(editName, editEmail, editInterests);
                        (ProfileActivity.this).profileMajor.setText(mMajor.toString());
                    }
                }).build();

        if (editProfileDialog.getCustomView() != null) {
            final EditText editName = ButterKnife.findById(editProfileDialog, R.id.edit_name);
            final EditText editEmail = ButterKnife.findById(editProfileDialog, R.id.edit_email);
            final EditText editInterests = ButterKnife.findById(editProfileDialog, R.id.edit_interests);

            /*Need to override isEnabled so the user can't select the hint text in the spinner*/
            ArrayAdapter<User.Major> adapter = new ArrayAdapter<User.Major>(this, android.R.layout.simple_spinner_dropdown_item, User.Major.values()) {
                @Override
                public boolean isEnabled(int position) {
                    return position != 0; //Disabled "Select a major" item
                }
                @Override
                public boolean areAllItemsEnabled() {
                    return false;
                }
            };
            final AbsSpinner majorDropdown = ButterKnife.findById(editProfileDialog, R.id.major_dropdown);
            majorDropdown.setAdapter(adapter);
            editName.setText(mName);
            editEmail.setText(mEmail);
            if (mMajor != null) {
                majorDropdown.setSelection(((ArrayAdapter<User.Major>) majorDropdown.getAdapter()).getPosition(mMajor));
            }
            if (mInterests != null) {
                editInterests.setText(mInterests);
            }
        }
        editProfileDialog.show();
    }

    /**
     * When a user clicks the menu overflow icon and selects "Change Password", the user will be presented with a dialog asking for a new password and for them to confirm the new password.
     * Once the user is done changing the password, the user will press the "Save" button in the dialog to commit their changes to Firebase.
     */
    private void changePassword() {
        final MaterialDialog editPasswordDialog = new MaterialDialog.Builder(ProfileActivity.this)
                .title(editPasswordDialogTitle)
                .customView(R.layout.edit_password_dialog, true)
                .theme(Theme.DARK)
                .positiveText(save)
                .negativeText(cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog editPasswordDialog, @NonNull DialogAction which) {
                        final EditText editPasswordOld = ButterKnife.findById(editPasswordDialog, R.id.edit_password_old);
                        final EditText editPassword = ButterKnife.findById(editPasswordDialog, R.id.edit_password);
                        final EditText editPasswordConfirm = ButterKnife.findById(editPasswordDialog, R.id.edit_password_confirm);

                        final String editPasswordOldText = editPasswordOld.getText().toString();
                        final String editPasswordText = editPassword.getText().toString();
                        final String editPasswordConfirmText = editPasswordConfirm.getText().toString();

                        String oldPasswordAuth = mRef.child(mUsername).getAuth().toString();
                        Log.v("oldPasswordAuth", oldPasswordAuth);

                        if (passwordMatch(editPassword, editPasswordConfirm)
                                && editPasswordText.length() != 0
                                && editPasswordConfirmText.length() != 0) {
                            mRef.changePassword(mUsername, null, editPasswordConfirmText, new Firebase.ResultHandler() {
                                @Override
                                public void onSuccess() {
                                }
                                @Override
                                public void onError(FirebaseError firebaseError) {
                                }
                            });
                        }
                    }
                }).build();

        final View saveAction = editPasswordDialog.getActionButton(DialogAction.POSITIVE);

        if (editPasswordDialog.getCustomView() != null) {
            final EditText editPassword = ButterKnife.findById(editPasswordDialog, R.id.edit_password);
            final EditText editPasswordConfirm = ButterKnife.findById(editPasswordDialog, R.id.edit_password_confirm);

            final TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (passwordMatch(editPassword, editPasswordConfirm)
                            && editPassword.getText().toString().length() != 0
                            && editPasswordConfirm.getText().toString().length() != 0) {
                        editPasswordConfirm.setError(null); //Clears the error
                        saveAction.setEnabled(true);
                    } else {
                        editPasswordConfirm.setError(passwordMismatch);
                        saveAction.setEnabled(false);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            };
            editPassword.addTextChangedListener(watcher);
            editPasswordConfirm.addTextChangedListener(watcher);
        }
        editPasswordDialog.show();
        saveAction.setEnabled(false); //Disabled by default
    }

    /**
     * Helper method for making sure the new password and the confirm new password fields hold the same password.
     * @param newPassword is the new password EditText field
     * @param newPasswordConfirm is the EditText field that confirms the new password
     * @return true or false depending on the equality of the password fields
     */
    private boolean passwordMatch(EditText newPassword, EditText newPasswordConfirm) {
        return (newPassword.getText().toString().matches(newPasswordConfirm.getText().toString()));
    }

    /**
     * I wish I could tell you. If I put this up in the method where it is called, the text doesn't update. Moving it to it's own method works, however.
     * Allows us to update/refresh the ProfileActivity's data once a user edits the information.
     * @param editName is the field where the user edits their name
     * @param editEmail is the field where the user edits their email
     * @param editInterests is the field where the user edits their interests
     */
    private void passThrough(EditText editName, EditText editEmail, EditText editInterests) {
        profileName.setText(editName.getText().toString());
        profileEmail.setText(editEmail.getText().toString());
        profileInterests.setText(editInterests.getText().toString());
    }

    /**
     * Helper method that inits all of the Toolbar stuff.
     * Specifically:
     * sets Toolbar title, enables the visibility of the overflow menu, shows a back arrow for navigation, and handles what to do if a user presses the back button in the Toolbar.
     */
    private void initToolbar() {
        toolbar.setTitle(mUsername);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(backArrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //Simulate a system's "Back" button functionality.
            }
        });
    }

    /**
     * Creates the options in the overflow menu
     * @param  menu to create options for
     * @return true or false depending on whether or not inflation was successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_overflow, menu);
        return true;
    }

    /**
     * Handles what to do when a particular item is selected from the overflow menu
     * @param  item in the overflow menu
     * @return true or false if we handled the selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_password) {
            changePassword();
        }

        return super.onOptionsItemSelected(item);
    }
}
