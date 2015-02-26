if [ ! -f ./SpongeAPI/gradlew ]; then
    echo "Error locating the Gradle subprojects. You most likely forgot to run 'git submodule update --init'. This usually doesn't happen to people who read the README ;)"
    exit 1
fi
./SpongeAPI/gradlew $@
