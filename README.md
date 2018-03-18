# more-speech
Twitter censorship checker: find out if Twitter is shadow-banning, ghosting, or hiding your tweets from other users.

Introduction
------
If you tweet a reply to someone and then look at the tweet you replied to, you'll see your tweet right at the top of the list.

However, if you then log out and view the same page, your tweet might be pages down the list or even hidden behind a
"Show more replies" button (which few probably click).

If you then log back in as another user and view the same page, your tweet might not even be visible depending on
that user's settings on Twitter's Settings:Notifications screen (the restrictive settings used to be marked "recommended"
but that appears to have changed recently).

The tweets hidden behind the "Show more replies" button have the CSS class "LowQuality", even if they're higher quality
than the tweets that Twitter has elevated on the same page (such as tweets from verified users).

Making people think others can see their tweets when some or many can't is a deceptive practice that a former engineer
described as "unethical in some way" (1). Twitter denies shadow banning accounts, but marking some tweets as "LowQuality"
or hiding them altogether amounts to the same thing.

See the "Can they do that?" section below for more.

What this application does
------
This application loads a Twitter timeline looking for replies. It then visits those reply pages looking for each of the
replies and shows an HTML report with how each of the replies ranked: was it elevated, suppressed, marked as "LowQuality",
or hidden altogether.

This application can log in as a different user that has the Twitter default settings so you can see where tweets rank
as other users would.

You can optionally upload the results to our server for further research and choose to make the result data public
(you can also publish the HTML report on your own site if you wish).

Installation (Windows)
------
1. Download the latest Windows version from the Releases link above and unzip it to a location on your harddrive.
That's it! The Windows release is large but it contains everything you need.

2. Start the application by double clicking *morespeech.exe* (if Windows doesn't show file extensions, that's the small
file not the large file). That should create a few new directories inside the application directory.

3. Click the *Preferences* button and put in the handle you want to check.

4. Skip to the *Using the app* section below for more information on running the app.

Installation (Linux, Mac, or manual on Windows)
------
The first version requires some setup, but future versions will make the process easier. This version has also only been tested
on Linux. For now, the requirements are:

* Java version 8
* An older version of Firefox

1. If you don't already have Java 8 installed, you can get it
[here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

2. Download Firefox 44.0b9 from [here](https://ftp.mozilla.org/pub/firefox/releases/44.0b9/). (Other versions around the
same date should also work). You don't have to uninstall Firefox if you have a later version, you can have multiple versions.

3. Extract the Firefox download and create a profile just for this application. That's described
[here](https://support.mozilla.org/en-US/kb/profile-manager-create-and-remove-firefox-profiles).

4. In the new profile's Preferences, set it to ignore software updates. That's important otherwise Firefox will update itself
and the application will stop working. It's also highly recommended to set that profile to clear all data when Firefox closes.

5. Download and extract the latest release of this application on the *Releases* link above.

6. In the application directory, modify firefox.sh to match your platform and the location of the Firefox you downloaded.

7. You'll need two file paths: the path of the Firefox profile you created, and the path of your version of firefox.sh
(or the firefox.bat you created).

8. Start the application by double clicking the *morespeech.jar* (or *morespeech* on Windows) file. That should create
a few new directories inside the application directory.

9. Click the *Preferences* button and put in the handle you want to check and the two file paths from #7.

Using the app
------
1. (Optional) You can create a throwaway Twitter account just for use by this application. If provided, the app will first
login to Twitter using that account. This is the better way to check since most people viewing Twitter replies are
probably logged in. As discussed above, the tweets that you see when logged out might be different from those you see
when logged in.

**Only use a throwaway Twitter account, not one you want to keep. Nothing bad should happen but you never know.**

To set up the throwaway account, use a new email address and fill out the Twitter account's name, bio, etc.
Follow a few people (preferably not the ones Twitter suggests) and tweet once or twice.
You can leave Twitter's Settings:Notifications screen with the defaults, or change those settings to see what happens.

2. Click the *Run checker* button. Firefox should open and navigate to the timeline for the handle you entered. It should
be located just off the screen. Try not to interact with that window since doing so will interfere with what the application
is doing. Currently it will take a long time to load the pages but future versions will increase the speed. You might
be able to speed things up by installing a flash blocker plugin (don't install NoScript since the application requires
Javascript to be enabled).

3. When the app finishes, the Firefox window should close and the app will indicate the name of the report that it
produced. The reports are stored in the `reports` directory and are self-contained HTML files that can be opened
with any browser. They also contain debug data showing the raw tweets and computations that were performed.

How to build it
------
If you don't have it already, install Maven from [here](https://maven.apache.org/install.html). Then, open a command line
window, change to the directory with the source code (where the pom.xml file is) and type `mvn compile package`,
`mvn compile exec:java`, or similar. Only two tests have been provided; others will be uploaded later.

**NOTE:** If you use the `exec:java` version, the database and reports will be stored in the `target` directory. If you want
to save the database and the reports, don't do a `mvn clean`.

Options
------
* If you launch the app from the command line or your own script, you can override the locations of the Firefox profile and/or
binary as follows:
`java -Dprefs.firefox_path_profile="/path/to/profile/directory" -Dprefs.firefox_path_app="/path/to/a/firefox/executable" -jar morespeech.jar`

* The app looks for a firefox profile directory in these locations: `firefox/ffprof` and `firefox/Data/profile`. If it
finds a directory there, that is used as the Firefox profile. The `firefox` directory should be next to the app
(on the same level as `reports`). The `ffprof` or `profile` directory should contain the standard profile files like
`prefs.js`.

* The app looks for a Firefox executable in the following locations: `firefox/ffbin/firefox.exe`, `firefox/ffbin/firefox.bat`,
`firefox/ffbin/firefox.sh`, `firefox/ffbin/firefox`, and `firefox/FirefoxPortable.exe`. As above, he `firefox` directory should
be next to the app.

Troubleshooting
------
1. If double-clicking the .jar file opens it in a .zip program, right-click. Is there an option to run it using Java?
2. If double-clicking the .jar file does nothing, open a command line window and navigate to the directory with the application.
Then type `java -jar morespeech.jar`. If it says something about Java not being installed, see the instructions above.
If there's some sort of other error listing, post that as a bug report using the link above.
3. If the application loads but says it can't find Firefox, try to run firefox.sh/firefox.bat from a command line window
and correct it as needed.

Privacy
------
You can choose to upload the results of the processing (the raw results, not just the HTML file) to our server if you want.
That will not be made public unless you also choose that. The upload data contains the handle you searched but it does
not contain the handle or password of the testing account. We store the time you uploaded the data and your IP address.
We don't upload any data from your computer other than the raw results. We could generate the same data by running the
application using the same handle at the same time, so the only "new" information is the handle you searched. If you
don't want to reveal you searched a specific handle, then don't choose to upload the data.

The app writes a log file in the `logs` directory. If filing a bug report, please provide the relevant parts of the log
file and sanitize it beforehand such as by replacing paths, the testing account handle/password, etc.

Licensing
------
The source code is licensed under the The Apache Software License, Version 2.0, see LICENSE. The application includes
many components from others and those are covered under their licenses (see the licenses directory in the application
download). The Java Native Access library is dual licensed and is used in this application under The Apache Software
License, Version 2.0.

The Windows version bundles the JRE from Oracle Corp and is redistributed under the Oracle Binary Code License Agreement
for the Java SE Platform Products and JavaFX (see [this](http://www.oracle.com/technetwork/java/javase/terms/license/index.html) ).
No JavaFX components are included.

The Windows version bundles a version of Firefox from PortableApps. That was installed and run once to create a profile
and change some settings. No other modifications were made.

Can they do that?
------
Twitter certainly has a right to deceive their users in any way the want to as long as they abide by the law and their Terms
of Service. Deceiving your users is an unethical practice, but Twitter can do it.

Fans of a celebrity have no inherent rightfor that celebrity to see their tweets. It gets more serious when it comes to policy
matters and similar.

Imagine a reporter tweets *"Rep. So-and-so told me he intends to introduce a bill to build an escalator to Mars. I asked him
how soon the construction could begin."* The replies are split between off-topic jeers at the politician and support for
the plan from the politician's fans. The only reply that details how the plan is unfeasible and calls out the reporter for
buying into it is hidden in the "LowQuality" section. Few see it, increasing the chances the reporter will keep buying into
unfeasible plans and that the politician will keep pushing unfeasible plans.

The question of whether a politician who uses Twitter for official business should be able to filter out replies
and whether Twitter should censor replies to that politician while elevating replies from verified users is a gray area.

Is this a partisan issue?
------
No. Some of those who try to make it a partisan issue seem to support censorship, they're just upset they aren't the ones
doing the censoring. In order to oppose censorship you have to do it across the board (with exceptions for violent
threats, "doxing", and so on). Making the opposition to what Twitter does a partisan issue helps Twitter by dividing
the possible opposition.

-----
(1) arstechnica.com/tech-policy/2018/01/activist-says-twitter-shadow-bans-conservatives-dont-believe-it/2/
