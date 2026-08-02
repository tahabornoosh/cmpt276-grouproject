# Profile Bio and Avatar Manual Tests

Date tested: August 1, 2026
Branch: `parsa-it3-profile-avatar`
Environment: Local Spring Boot application with an H2 in-memory database

## Story

As a user, I want to add a bio and upload a profile avatar so other users can learn more about me and recognize me across the application.

## Manual Test Results

### 1. Default profile values

Steps:

1. Create a new account.
2. Log in.
3. Open the account-edit page.

Expected:

- The default avatar is displayed.
- The bio field is empty.
- Avatar upload and removal controls are available.

Result: Passed

### 2. Add a bio and avatar

Steps:

1. Enter a bio.
2. Select a valid PNG or JPEG image under 2 MB.
3. Submit the form.
4. Open the profile page.

Expected:

- A success message appears.
- The bio is saved and displayed.
- The uploaded avatar appears on the edit page.
- The uploaded avatar appears on the profile page.
- The uploaded avatar appears in the sidebar.

Result: Passed

### 3. Replace an avatar

Steps:

1. Upload a valid avatar.
2. Submit the form.
3. Upload a different valid avatar.
4. Submit the form again.

Expected:

- The second image replaces the first image.
- The new image appears on the edit page, profile page, and sidebar.

Result: Passed

### 4. Remove an avatar

Steps:

1. Select the remove-avatar checkbox.
2. Leave the upload field empty.
3. Submit the form.

Expected:

- The uploaded avatar is removed.
- The default avatar is displayed again.
- The saved bio remains unchanged.

Result: Passed

### 5. Reject an invalid file

Steps:

1. Select a non-image file.
2. Submit the form.

Expected:

- The file is rejected.
- The message `Avatar must be a valid PNG or JPEG image under 2 MB.` appears.
- Existing profile information remains unchanged.

Result: Passed

### 6. Empty-bio fallback

Steps:

1. Delete all text from the bio field.
2. Submit the form.
3. Open the profile page.

Expected:

- The profile displays `No bio added yet.`

Result: Passed

## Automated Tests

The automated tests cover:

- Saving and trimming a bio
- Rejecting a bio longer than 500 characters
- Uploading and serving a valid PNG
- Rejecting invalid image data
- Rejecting files larger than 2 MB
- Removing an existing avatar
- Returning 404 when an uploaded avatar does not exist
- Redirecting unauthenticated edit requests to login

Full project test result:

- Tests run: 108
- Failures: 0
- Errors: 0
- Skipped: 0
