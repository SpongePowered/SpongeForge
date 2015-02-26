@ECHO off
IF NOT EXIST SpongeAPI\gradlew.bat (
    echo Error locating the Gradle subprojects. You most likely forgot to run 'git submodule update --init'. This usually doesn't happen to people who read the README ;^)
    REM run it for them
    git submodule update --init --recursive
    REM also assume they didn't run the cp
    cp scripts/pre-commit .git/hooks
)
SpongeAPI\gradlew.bat %*
