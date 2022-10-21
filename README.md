# more-speech

[![CodeFactor](https://www.codefactor.io/repository/github/TolstoyDotCom/more-speech/badge)](https://www.codefactor.io/repository/github/TolstoyDotCom/more-speech)

## Table of Contents

- [Features](#features)
- [Examples](#examples)
- [2.0 Notes](#notes20)
- [Installation (Windows)](#installation-windows)
- [Installation (Linux, Mac, or manual on Windows)](#installothers)
- [Using the application](#using-the-application)
- [Support the project](#support-the-project)
- [How to build it](#how-to-build-it)
- [Options](#options)
- [Troubleshooting](#troubleshooting)
- [Privacy](#privacy)
- [Licensing](#licensing)
- [What Twitter Does](#what-twitter-does)
- [Is this a partisan issue?](#is-this-a-partisan-issue)

More Speech is a desktop application that shows how Twitter censors many of its users. Twitter deceives those users into thinking their replies are visible, but to other users those replies are suppressed or hidden. This deceptive practice is called "shadowbanning" or "ghosting". *(See the What Twitter Does section below for a detailed explanation.)*

It's important to note that Twitter does this to a wide variety of users regardless of the topic or their ideology: this deceptive practice isn't limited to just one group. Twitter also seems to do this regardless of the content of the tweet itself.

The 2.0 version of this app works with "New Twitter" (around July 2019, Twitter greatly changed the technical details of their pages; they used to put their low opinions of many of their users right in the HTML). New Twitter now hides away things like "LowQuality" (described below) in AJAX; the 2.0 version of this app uses a proxy to capture that AJAX.

Features
------
* The application checks both **replies from** and **replies to** a specific user.

* You can check your replies to see how they rank. You can find out if Twitter is in effect censoring you. You can also find out if Twitter is elevating low-quality replies from verified users.

* You can check how Twitter ranks replies to a newsmaker, a politician, etc. Depending on their settings, they might not see suppressed or hidden replies. That means Twitter may be in effect protecting them from dissent.

* The application produces HTML reports showing tweets that were elevated, suppressed, or hidden, how tweets were ranked, and more.

* A built-in formula re-ranks tweets. That formula downranks short tweets (especially if they have a picture) and upranks longer tweets using longer words that have something in common with the tweet that was replied to. However, you can try a different formula to see what it does. You can do that in Java or Javascript. In the case of Javascript, you can quickly test changes to your formula.

* Twitter data is obtained exactly as a regular user would, using a real web browser. Pages can be viewed as an anonymous visitor or logged in using a testing Twitter account. You can change the settings in the testing account to see how that changes what Twitter does.

* You can optionally upload the results to our server for further research and choose to make the result data public. You can also publish the HTML reports on your own site if you wish.

* The Windows download is large but no setup is required: just download, unzip, and run.

2.1 Notes (ignore the other instructions for now)
------
This has only been tested with Firefox 105 and OpenJDK 17 on Ubuntu 18. JDK 17 is required. A windows version and a pre-built jar file will be released after more testing.

The Installation sections below should be ignored for now.

To run the 2.1 version:

1. Create a new Firefox profile just for use by this application. Set it to remember cookies. You'll need the name of the profile; choose something all lowercase without spaces like "twtr_censorship".
2. In the new profile, log in to the testing Twitter account. (See below for the details on the testing account. The previous version could log in automatically but that part hasn't been updated yet).
3. In the new profile, install [this extenion](https://addons.mozilla.org/en-US/firefox/addon/har-export-trigger/). (The previous version of this application used a proxy to capture the AJAX messages from Twitter but the proxy [isn't working](https://github.com/lightbody/browsermob-proxy/issues/897). So, the new version gets the AJAX directly from the browser and the extension is used to accomplish that.)
4. Open a console window in the top-level directory (where `pom.xml` is) and type this:

`JDK_JAVA_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED" mvn clean compile exec:java -D{webdriver.http.factory}={jdk-http-client} -D"exec.mainClass"="com.tolstoy.censorship.twitter.checker.app.Start"`

5. When the application opens, in the Preferences dialog fill out the handle, check the 'Skip the login page' checkbox, and fill out the 'Firefox profile name' (or path). Then, click the 'Check timeline' button and wait until it says it's created the report.

You could also first enter the required data into quickstart.prefs.json and then replace `Start` with `QuickStart` in the command above, but that hasn't been tested.

suededenim.java.js in the stockscripts directory is from the [SuedeDenim](https://github.com/TolstoyDotCom/SuedeDenim) project. You should be able to replace that with newer versions of the script.

Examples
------
Here are several [sample Twitter censorship reports](http://tolstoy.com/twittercensorship/) showing replies to a variety of sources: politicians, pundits, NASA, sports figures, musicians, etc. A couple of major politicians have no replies censored; others are higher or much higher. More will be added occasionally. (Almost all were as a logged-in user, and we aren't affiliated with/endorsing them/unendorsing them, they're only presented as examples.)

Installation (Windows)
------
1. Download the latest Windows version from the Releases link above and unzip it to a location on your harddrive. That's it! The Windows release is large but it contains everything you need.

2. Start the application by double clicking *morespeech.exe* (if Windows doesn't show file extensions, that's the small file not the large file). That should create a few new directories inside the application directory.

3. Click the *Preferences* button and put in the handle you want to check.

4. Skip to the *Using the app* section below for more information on running the app.

<a name="installothers"></a>Installation (Linux, Mac, or manual on Windows)
------
The first version requires some setup, but future versions will make the process easier. This version has also only been tested on Linux. For now, the requirements are:

* Java version 8
* An older version of Firefox

1. If you don't already have Java 8 installed, you can get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

2. Download Firefox 44.0b9 from [here](https://ftp.mozilla.org/pub/firefox/releases/44.0b9/). (Other versions around the same date should also work). You don't have to uninstall Firefox if you have a later version, you can have multiple versions.

3. Extract the Firefox download and create a profile just for this application. That's described [here](https://support.mozilla.org/en-US/kb/profile-manager-create-and-remove-firefox-profiles).

4. In the new profile's Preferences, set it to ignore software updates. That's important otherwise Firefox will update itself and the application will stop working. It's also highly recommended to set that profile to clear all data when Firefox closes.

5. Download and extract the latest release of this application on the *Releases* link above.

6. In the application directory, modify firefox.sh to match your platform and the location of the Firefox you downloaded.

7. You'll need two file paths: the path of the Firefox profile you created, and the path of your version of firefox.sh (or the firefox.bat you created).

8. Start the application by double clicking the *morespeech.jar* (or *morespeech* on Windows) file. That should create a few new directories inside the application directory.

9. Click the *Preferences* button and put in the handle you want to check and the two file paths from #7.

Using the application
------
1. (Optional) You can create a throwaway Twitter account just for use by this application. If provided, the app will first login to Twitter using that account. This is the better way to check since most people viewing Twitter replies are probably logged in. As discussed below, the tweets that you see when logged out might be different from those you see when logged in. **Only use a throwaway Twitter account, not one you want to keep. Nothing bad should happen but you never know.** To set up the throwaway account, use a new email address and fill out the Twitter account's name, bio, etc. Follow a few people (preferably not the ones Twitter suggests) and tweet once or twice. You can leave Twitter's Settings:Notifications screen with the defaults, or change those settings to see what happens. You can also create multiple testing accounts with different settings and following different users to see if that changes what Twitter does.

2. To check replies **from** a user (i.e, is that user shadowbanned), first visit [this page](https://tolstoy.com/get-my-replies). On that page, enter the handle of the account you want to check and download that file. Then, in the application, click the *Check itinerary* button and select the file you downloaded. Firefox should open and navigate to the timeline for the handle you entered. It should be located just off the screen. Try not to interact with that window since doing so will interfere with what the application is doing. Currently it will take a long time to load the pages but future versions will increase the speed. You might be able to speed things up by installing a flash blocker plugin in the browser that's being used (don't install NoScript since the application requires Javascript to be enabled).

3. An alternative way to check replies **from** a user is to just enter enter the handle to check in the preferences screen and click the *Check replies* button. This only works if your replies are visible in your timeline and you see "Replying to" or similar in the tweet.

4. To check replies **to** a user (i.e., to see how heavily Twitter censors replies to newsmakers), enter the user's handle in the preferences screen and click the *Check timeline* button. The same process as above will occur.

4. When the app finishes, the Firefox window should close and the app will indicate the name of the report that it produced. The reports are stored in the `reports` directory and are self-contained HTML files that can be opened with any browser. They also contain debug data showing the raw tweets and computations that were performed.

5. The *Rewrite last report* button is discussed in the Options section.

Support the project
------
Pull requests are welcome. The primary issues are improving performance, fixing an encoding issue on Windows, dealing better with self-replies, dealing with reply pages that have around 200 or more replies, and others. An enhancement would be to perform multiple searches in one session to avoid logging in each time. Another would be obtaining follower counts, etc. for each account listed in a report, either using the Twitter API or by scraping the user hover popup.

If you'd like to help fund past and future development as well as future promotion, [please click here to donate now](https://paypal.me/twittercensorship).


How to build it
------
If you don't have it already, install Maven from [here](https://maven.apache.org/install.html). Then, open a command line window, change to the directory with the source code (where the pom.xml file is) and type `mvn compile package`, `mvn compile exec:java`, or similar. Only two tests have been provided; others will be uploaded later.

**NOTE:** If you use the `exec:java` version, the database and reports will be stored in the `target` directory. If you want to save the database and the reports, don't do a `mvn clean`.

Options
------
* If you want to change the formula used for ranking tweets **to** a specific user, you can copy and edit the file in the userscripts directory. See the notes in that file for instructions. After editing your formula, press the *Rewrite last report* button to write out a new report that uses that formula. You can also change the formula in Java but that would be more complex. Feel free to submit your formula (or any other enhancements) as a pull request.

* If you launch the app from the command line or your own script, you can override the locations of the Firefox profile and/or binary as follows:
`java -Dprefs.firefox_path_profile="/path/to/profile/directory" -Dprefs.firefox_path_app="/path/to/a/firefox/executable" -jar morespeech.jar`

* The app looks for a firefox profile directory in these locations: `firefox/ffprof` and `firefox/Data/profile`. If it finds a directory there, that is used as the Firefox profile. The `firefox` directory should be next to the app (on the same level as `reports`). The `ffprof` or `profile` directory should contain the standard profile files like `prefs.js`.

* The app looks for a Firefox executable in the following locations: `firefox/ffbin/firefox.exe`, `firefox/ffbin/firefox.bat`, `firefox/ffbin/firefox.sh`, `firefox/ffbin/firefox`, and `firefox/FirefoxPortable.exe`. As above, he `firefox` directory should be next to the app.

* To see what the application does, set the *Firefox screen location* values in the preferences screen to 0. Otherwise, you can set them to large positive or negative values to try to hide the Firefox window offscreen.

Troubleshooting
------
1. If double-clicking the .jar file opens it in a .zip program, right-click. Is there an option to run it using Java?

2. If double-clicking the .jar file does nothing, open a command line window and navigate to the directory with the application. Then type `java -jar morespeech.jar`. If it says something about Java not being installed, see the instructions above. If there's some sort of other error listing, post that as a bug report using the link above.

3. If the application loads but says it can't find Firefox, try to run firefox.sh/firefox.bat from a command line window and correct it as needed.

Privacy
------
You can choose to upload the results of the processing (the raw results, not just the HTML file) to our server if you want. That will not be made public unless you also choose that. The upload data contains the handle you searched but it does not contain the handle or password of the testing account. We store the time you uploaded the data and your IP address. We don't upload any data from your computer other than the raw results. We could generate the same data by running the application using the same handle at the same time, so the only "new" information is the handle you searched. If you don't want to reveal you searched a specific handle, then don't choose to upload the data.

The app writes a log file in the `logs` directory. If filing a bug report, please provide the relevant parts of the log file and sanitize it beforehand such as by replacing paths, the testing account handle/password, etc.

Licensing
------
The source code is licensed under the The Apache Software License, Version 2.0, see LICENSE. The application includes many components from others and those are covered under their licenses (see the licenses directory in the application download). The Java Native Access library is dual licensed and is used in this application under The Apache Software License, Version 2.0.

The Windows version bundles the JRE from Oracle Corp and is redistributed under the Oracle Binary Code License Agreement for the Java SE Platform Products and JavaFX (see [this](http://www.oracle.com/technetwork/java/javase/terms/license/index.html) ). No JavaFX components are included.

The Windows version bundles a version of Firefox from PortableApps. That was installed and run once to create a profile and change some settings. No other modifications were made.

The Windows version was built using and uses the launcher from [packr](https://github.com/libgdx/packr) which is licensed under The Apache Software License, Version 2.0.

Two files from the [dkpro-core](https://github.com/dkpro/dkpro-core) project have been included in this project as source files, to avoid large dependencies. Those files are licensed under The Apache Software License, Version 2.0.

The [cue.language](https://github.com/vcl-xx/cue.language) project is used from [this fork](https://github.com/codycoggins/cue.language) . That project is licensed under The Apache Software License, Version 2.0.

What Twitter Does
------
If you tweet a reply to someone and then look at the tweet you replied to, you'll see your tweet right at the top of the list.

However, if you then log out and view the same page, your tweet might be pages down the list or even hidden behind a "Show more replies" button (which few probably click).

If you then log back in as another user and view the same page, your tweet might not even be visible depending on that user's settings on Twitter's Settings:Notifications screen (the restrictive settings used to be marked "recommended" but that appears to have changed recently).

The tweets hidden behind the "Show more replies" button have the CSS class "LowQuality", even if they're higher quality than the tweets that Twitter has elevated on the same page (such as tweets from verified users).

Making people think others can see their tweets when some or many can't is a deceptive practice that a former engineer described as "unethical in some way" (1). Twitter denies shadow banning accounts, but marking some tweets as "LowQuality" or hiding them altogether amounts to the same thing.

Not only that, Twitter has an additional "AbusiveQuality" section below the "LowQuality" section and hidden behind another link. In the HTML that's referred to as the "tombstone" section.

Twitter certainly has a right to deceive their users in any way the want to as long as they abide by the law and their Terms of Service. Deceiving your users is an unethical practice, but Twitter can do it.

Fans of a celebrity have no inherent rightfor that celebrity to see their tweets. It gets more serious when it comes to policy matters and similar.

Imagine a reporter tweets *"Rep. So-and-so told me he intends to introduce a bill to build an escalator to Mars. I asked him how soon the construction could begin."* The replies are split between off-topic jeers at the politician and support for the plan from the politician's fans. The only reply that details how the plan is unfeasible and calls out the reporter for buying into it is hidden in the "LowQuality" section. Few see it, increasing the chances the reporter will keep buying into unfeasible plans and that the politician will keep pushing unfeasible plans.

The question of whether a politician who uses Twitter for official business should be able to filter out replies and whether Twitter should censor replies to that politician while elevating replies from verified users is a gray area, at least from a public policy perspective. Twitter is in effect deciding who gets to contact a politician using their service.

Is this a partisan issue?
------
No. Some of those who try to make it a partisan issue seem to support censorship, they're just upset they aren't the ones doing the censoring. In order to oppose censorship you have to do it across the board (with exceptions for violent threats, "doxing", and so on). Making the opposition to what Twitter does a partisan issue helps Twitter by dividing the possible opposition.

-----
(1) arstechnica.com/tech-policy/2018/01/activist-says-twitter-shadow-bans-conservatives-dont-believe-it/2/
