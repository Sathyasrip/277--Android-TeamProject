# Academia
An app where researchers or their ilk can peer-review professional papers on the go.

## Building the project
Make sure to first download and install the latest version of Android Studio, found [here](https://developer.android.com/studio/?gclid=Cj0KCQiA48j9BRC-ARIsAMQu3WQyjDK8mCnRvmxIgKQhraBXtx_VKZod_fwn0J32Dpkt5-7aCE-zCNkaAojSEALw_wcB&gclsrc=aw.ds)

If this is the first time installing Android Studio, first compile a Hello World activity by making a new activity through choosing "Blank Activity", Build and Compile. You will be asked to choose the Android OS. Make sure you choose either Android 10 or Android 11, as this project is compiled with Android 11 in mind.

Once Android Studio is installed and the correct Android SDK Emulator (Android 10/11) is working, clone the repository using:
```
git clone https://github.com/Sathyasrip/277--Android-TeamProject.git
```

After that, simply go into Android Studio to open the project directory and build. Note: The first time you build may take over 10 minutes due to downloading a lot of dependencies.

After the build is successful, execute the app and you should have full access to the application!

## Installing the APK to your real android device
Once the application has finished with Alpha and entered its Beta stage, the instructions for the offical apk release will be listed here.

## Reviews
Throughout the life cycle of our application, the users of this app will create or critique version controlled documents called 'Reviews'. A Review is essentially a PDF file with a linked comments payload and annotations file which can be viewable in our application to critique a Review owner's uploaded PDF. By default on registration, only approved users are able to start a new review or delete their current existing reviews, one version at a time. Standard users (new users to this app) can only view existing reviews and make comments and highlights to the existing Review version. This is necessary to prevent trolling and unprofessional document uploads.

## Server and Authentication
This application is powered with Google Cloud & Firebase. Firebase is used for User authentication, and Firebase is also used to store the list of comments for the existing review being critiqued by users in real time, as well as act as the main storage for profile pictures, annotation files, and review PDF files whose entries are currently open on the open_reviews database.

## Last User Login
A simple MySQL database is used to store an encrypted username and password for the last user login. If the login is successful, the single entry stays in the MySQL database. If login was unsuccessful, on bootup of the application, the user will go to the Login screen directly. It is also important to note that there currently is no MySQL database called "last_login" or there is no MySQL entry in the "last_login" database, the user will be directed to the login page automatically.
