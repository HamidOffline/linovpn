@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=C:\Program Files\Android\Android Studio\jbr\bin;%PATH%"
set "ANDROID_HOME=C:\Users\MojAzad\AppData\Local\Android\Sdk"
cd /d "C:\Users\MojAzad\.gemini\antigravity\scratch\LinoVPN_v3\LinoVPN3"
call gradlew.bat assemblePlaystoreRelease --no-daemon
