Work for BU CS591 Mobile App Development.

Android app which uses AWS Amplify, AWS Rekognition, and movies API to idenitfy the actors in an image and return the films they have in common, with the goal of identifying the original movie the picture is from. The app then displays information about this movie, including year, cast, genre, and images of the poster and cast.

Setup should be as simple as performing a gradle sync. Some machines experience issues with the sync (the reason for this is unknown).
If build errors occur following initial gradle sync, please review AWS Amplify documentation for detailed setup instructions: https://docs.amplify.aws/start/getting-started/setup/q/integration/android#prerequisites

Instructions for testing: Click upload or the image of the film frame on the first screen of the app. From here you can upload an image from your phone, then select 'Identify.'

For example images to test the app on, try using the images in drawable/examples. These images will need to be moved to the storage on your device before they can be accessed.
When testing using a picture uploaded from the camera, make sure the picture has a clear view of the faces of the celebrities.

You can also type in the name of an actor at the bottom of the screen by clicking on the search bar.

On the following screen, add or remove the names of actors as you see fit. When the list is complete, select 'Next.'

On the resulting screen, a list of movies will be displayed. Any of these movies can be clicked on to display additional information about the film.

On the toolbar, select 'Settings' to review adjustable settings such as changing the match frequency and clearing the cache.